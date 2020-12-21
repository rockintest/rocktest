package io.rocktest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.JsonPath;

import io.rocktest.modules.Http;
import io.rocktest.modules.Sql;
import lombok.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;


@Component
@NoArgsConstructor
public class Scenario {


    @Getter
    @Setter
    @AllArgsConstructor
    private class Variable {
        private String var;
        private String value;
    }

    private String currentDesc="";
    private int currentStep=0;
    private String title="";

    // Variables.
    // Key = scenario name (top of the stack)
    // Value = variables
    private Map<String,Map<String,String>> context;
    private HashMap<String,String> last=new HashMap<>();

    private StringSubstitutor subLast = new StringSubstitutor(last);
    private StringSubstitutor subCond;
    private static final StringSubstitutor subEnv = new StringSubstitutor(System.getenv());
    private StringSubstitutor subContext;

    // Call stack
    private List<String> stack;

    private static Logger LOG = LoggerFactory.getLogger(Scenario.class);

    // HashMap for each instance of modules
    private HashMap<String,Object> moduleInstances=new HashMap<>();

    @Value("${default.check.delay}")
    private int checkDelay;

    @Value("${default.check.retry}")
    private int checkRetry;

    @Value("${default.datasource.url}")
    private String datasourceUrl;

    @Value("${default.datasource.user}")
    private String datasourceUser;

    @Value("${default.datasource.password}")
    private String datasourcePassword;

    private String getStack() {
        StringBuilder b=new StringBuilder();

        boolean first=true;
        for (String curr : stack) {
            if(first)
                first=false;
            else
                b.append("/");

            b.append(curr);
        }
        return b.toString();
    }

    // Retourne le nom du script en cours (dernier élément de la stack d'appel)
    private String getCurrentName() {
        return stack.get(stack.size()-1);
    }

    // Retourne la map des variables du scenario en cours
    private Map<String,String> getLocalContext() {
        return context.get(getCurrentName());
    }

    private String getVar(String var) {
        String ret=getLocalContext().get(var);
        return (ret==null?"":ret);
    }

    private void setVar(String var,String value) {
        getLocalContext().put(var,value);
    }

    // Initialise la hashmap pour les variable de ce script
    void initLocalContext() {

        initContext(getCurrentName());
        subContext = new StringSubstitutor(getLocalContext());
        subCond = new StringSubstitutor(new DefValueCompute(getLocalContext()));

    }

    void initContext(String name) {
        if(context.get(name)==null) {
            context.put(name,new HashMap<>());
        }
    }

    void deleteContext(String name) {
        context.remove(name);
    }

    private String expand(String val) {

        String ret=subLast.replace(val);
        ret=subEnv.replace(ret);
        ret=subCond.replace(ret);

        return ret;
    }

    private List expand(List val) {
        ArrayList<Object> ret=new ArrayList<>();

        for (int i = 0; i < val.size(); i++) {
            Object o=val.get(i);

            if(o instanceof String) {
                ret.add(expand((String)val.get(i)));
            } else if(o instanceof Map) {
                ret.add(expand((Map)val.get(i)));
            } else if(o instanceof List) {
                ret.add(expand((List)val.get(i)));
            }

        }
        return ret;
    }

    private Map<String,Object> expand(Map<String,Object> in) {
        HashMap<String,Object> ret=new HashMap<>();
        for (Map.Entry<String,Object> entry : in.entrySet()) {

            if(entry.getValue() instanceof String) {
                ret.put(entry.getKey(),expand((String)entry.getValue()));
            } else if(entry.getValue() instanceof Map) {
                ret.put(entry.getKey(),expand((Map)entry.getValue()));
            } else if(entry.getValue() instanceof List) {
                ret.put(entry.getKey(),expand((List)entry.getValue()));
            } else if(entry.getValue() instanceof Number) {
                ret.put(entry.getKey(),(Number)entry.getValue());
            } else {
                throw new RuntimeException("Error expanding node. Type "+entry.getValue().getClass().getName()+" unexpected");
            }
        }
        return ret;
    }

    private void setContext(String name,Map<String,String> vars) {
        context.put(name,vars);
    }

    private void putCallerContext(String var, String value) {
        if(stack.size()==1) {
            LOG.warn("Cannot return value in main scenario");
        } else {
            Map<String,String> callerContext=context.get(stack.get(stack.size()-2));
            callerContext.put(var,value);
        }
    }

    private void doAssert(String assertType,Map<String,String> params) {

        switch(assertType) {
            case "equals":
                String actual=params.get("actual");
                if(actual==null) {
                    throw new RuntimeException("\"actual\" param is required");
                }

                String expected=params.get("expected");
                if(expected==null) {
                    throw new RuntimeException("\"expected\" param is required");
                }

                String msg=params.get("message");
                if(msg==null) msg="";

                if(!actual.equals(expected)) {
                    throw new RuntimeException("Assert fail: "+msg+" - expected \""+expected+"\" but was \""+actual+"\"");
                }

                break;
            default:
                throw new RuntimeException("Bad assertion type :"+assertType);
        }
    }

    private void checkParams(String[] p) {
        for(String curr:p) {
            if(getLocalContext().get(curr)==null) {
                throw new RuntimeException("Parameter "+curr+" is mandatory for module "+getCurrentName());
            }
        }
    }


    private Map exec(String function,Map<String,Object> params) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        String cls = function.substring(0, function.lastIndexOf('.'));
        Class<?> moduleClass = Class.forName(cls);

        Object module = moduleInstances.get(cls);
        if(module==null) {
            module = moduleClass.getDeclaredConstructor().newInstance();
            moduleInstances.put(cls,module);
        }

        String methodName = function.substring(function.lastIndexOf('.')+1,function.length());

        Class<?>[] paramTypes = {Map.class};
        Method setNameMethod = module.getClass().getMethod(methodName, paramTypes);
        Map<String,Object> ret = (Map<String,Object>) setNameMethod.invoke(module, params);

        if(ret != null) {
            for (String k : ret.keySet()) {
                getLocalContext().put(methodName + "." + k, String.valueOf(ret.get(k)));
            }
        }

        return ret;
    }


    private void checkSqlConnection() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        // If the SQL connection it not open, open it with the default params

        Object module = moduleInstances.get("io.rocktest.modules.Sql");
        if(module==null || ((Sql) module).getJdbcTemplate()==null) {

            HashMap<String,Object> params=new HashMap<>();
            params.put("url",datasourceUrl);
            params.put("user",datasourceUser);
            params.put("password",datasourceUser);
            params.put("delay",checkDelay);
            params.put("retry",checkRetry);

            exec("io.rocktest.modules.Sql.connect",params);
        }
    }

    private void execSql(String req,List expect) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        checkSqlConnection();
        HashMap<String, Object> params = new HashMap<>();
        params.put("request", req);
        params.put("expect", expect);

        Map ret = exec("io.rocktest.modules.Sql.request", params);

        // Put $0 ... $n variables
        if(ret!=null) {
            for (int iMap = 0; ; iMap++) {
                String val = (String) ret.get("" + iMap);
                if (val == null)
                    break;
                getLocalContext().put("" + iMap, val);
            }
        }
    }

    private Http.HttpResp httpGet(String url) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("url", url);

        Map retexec = exec("io.rocktest.modules.Http.get", params);

        String code=String.valueOf(retexec.get("code"));
        String body=String.valueOf(retexec.get("body"));

        Http.HttpResp ret = new Http.HttpResp(Integer.valueOf(code),body);
        return ret;
    }


    private Http.HttpResp httpRequest(String method,String url,String bodyin) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("body", bodyin);

        Map retexec = exec("io.rocktest.modules.Http."+method, params);

        String code=String.valueOf(retexec.get("code"));
        String body=String.valueOf(retexec.get("body"));

        Http.HttpResp ret = new Http.HttpResp(Integer.valueOf(code),body);
        return ret;
    }


    private Http.HttpResp httpDelete(String url) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("url", url);

        Map retexec = exec("io.rocktest.modules.Http.delete", params);

        String code=String.valueOf(retexec.get("code"));
        String body=String.valueOf(retexec.get("body"));

        Http.HttpResp ret = new Http.HttpResp(Integer.valueOf(code),body);
        return ret;
    }

    private Http.HttpResp httpPost(String url,String bodyin) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("body", bodyin);

        Map retexec = exec("io.rocktest.modules.Http.post", params);

        String code=String.valueOf(retexec.get("code"));
        String body=String.valueOf(retexec.get("body"));

        Http.HttpResp ret = new Http.HttpResp(Integer.valueOf(code),body);
        return ret;
    }


    private Http.HttpResp httpPut(String url,String bodyin) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("body", bodyin);

        Map retexec = exec("io.rocktest.modules.Http.put", params);

        String code=String.valueOf(retexec.get("code"));
        String body=String.valueOf(retexec.get("body"));

        Http.HttpResp ret = new Http.HttpResp(Integer.valueOf(code),body);
        return ret;
    }


    public String run(String name, String dir, Map<String,Map<String,String>> context,List stack) throws IOException, InterruptedException {

        LOG.info("Start scenario. name={}, dir={}",name,dir);

        this.context=context;
        this.stack=stack;

        initLocalContext();
        subContext.setEnableSubstitutionInVariables(true);
        subCond.setEnableSubstitutionInVariables(true);

        try {

            Object mapper = new ObjectMapper(new YAMLFactory());

            Step[] steps = ((ObjectMapper) mapper).readValue(new File(name), new TypeReference<Step[]>() {});

            for (int i = 0; i < steps.length; i++) {
                Step step = steps[i];

                currentStep=i+1;
                currentDesc=(step.getDesc()!=null?"("+step.getDesc()+")":"");

                String currentValue;
                String valueDetail;

                if(step.getValue()==null) {
                    currentValue = "";
                    valueDetail = "";
                } else {
                    currentValue = expand(step.getValue());
                    valueDetail = (currentValue.equals(step.getValue())?currentValue:step.getValue()+" => "+currentValue);
                }

                LOG.info("[{}] Step #{} {} : {},{}",
                        getStack(),
                        i + 1,
                        currentDesc,
                        step.getType(),
                        valueDetail);

                switch (step.getType()) {
                    case "exec":
                        exec(step.getValue(),expand(step.getParams()));
                        break;
                    case "checkParams":
                        checkParams(step.getValues());
                        break;
                    case "assert" :
                        doAssert(currentValue,expand(step.getParams()));
                        break;
                    case "return" :
                        returnVar(currentValue);
                        break;
                    case "var" :
                        setVar(currentValue);
                        break;
                    case "exit" :
                        LOG.info("Exit");
                        i=steps.length;
                        break;
                    case "title":
                        title=currentValue;
                        break;
                    case "display":
                        LOG.info(currentValue);
                        break;
                    case "request":
                        execSql(currentValue,null);
                        break;
                    case "pause":
                        Thread.sleep(Integer.parseInt(step.getValue()) * 1000);
                        break;
                    case "http.get": {
                        Http.HttpResp resp = httpRequest("get",currentValue,null);
                        httpCheck(step.getExpect(), resp);
                        }
                        break;
                    case "http.post": {
                        Http.HttpResp resp = httpRequest("post",currentValue,step.getBody());
                        httpCheck(step.getExpect(), resp);
                        }
                        break;
                    case "http.put": {
                        Http.HttpResp resp = httpRequest("put",currentValue,step.getBody());
                        httpCheck(step.getExpect(), resp);
                        }
                        break;
                    case "http.delete": {
                        Http.HttpResp resp = httpRequest("delete",currentValue,null);
                        httpCheck(step.getExpect(), resp);
                        }
                        break;
                    case "call":
                        Scenario module=new Scenario();
                        String file=dir+"/"+step.getValue();

                        if(!file.endsWith(".yaml")) {
                            file=file.concat(".yaml");
                        }

                        // Push context for submodule
                        String moduleName=new File(file).getName().replace(".yaml","");
                        stack.add(moduleName);

                        if(step.getParams()!=null)
                            setContext(moduleName,expand(step.getParams()));

                        String err=module.run(file,dir,context,stack);

                        // Pop context
                        deleteContext(moduleName);
                        stack.remove(stack.size()-1);

                        if(err!=null) {
                            LOG.error("Error : {}",err);
                            System.exit(1);
                        }

                        break;
                    case "check":
                        execSql(currentValue,step.getExpect());
                        break;
                    default:
                        throw new RuntimeException("Type " + step.getType() + " unknown");
                }
            }

        } catch (Exception e) {

            String basename = FilenameUtils.getBaseName(name);

            LOG.error("Scen {} {}, Step #{} {} - Scenario FAILURE",basename,title,currentStep,currentDesc);
            LOG.error("Exception",e);
            return "Scen "+basename+" ["+title+"] step #"+currentStep+" "+currentDesc+" "+e.getMessage();
        }

        return null;
    }


    public Variable extractVariable(String exp) {
        Pattern p = Pattern.compile("[ ]*([^ ]+)[ ]*=[ ]*(.+)[ ]*");
        Matcher m = p.matcher(exp);

        if(!m.find()) {
            throw new RuntimeException("Syntax error. Declaration \""+exp+"\" must be formed \"<VAR>=<VALUE>\".");
        }

        String var=m.group(1);
        String val=m.group(2);

        return new Variable(var,val);
    }


    public void returnVar(String exp) {
        Variable v=extractVariable(exp);
        LOG.info("Return variable {} = {}",v.var,v.value);
        putCallerContext(getCurrentName()+"."+v.var,v.value);
    }


    public void setVar(String exp) {
        Variable v=extractVariable(exp);
        LOG.info("Set variable {} = {}",v.var,v.value);
        getLocalContext().put(v.var,v.value);

    }


    // Return false or throws an exception if a condition is false
    private boolean isConditionTrue(String var, String val, Http.HttpResp response, boolean throwErrorIfNotTrue){
        if (var.equals("code")) {
            LOG.info("\tResponse code = {}", response.getCode());

             String status=""+response.getCode();

            if(!val.equals(status)) {
                if (throwErrorIfNotTrue){
                    throw new RuntimeException("Status code does not match. Expected " + val + " but was " + status);
                }
                return false;
            }
            LOG.info("OK");

        } else if (var.startsWith("response.json")) {

            String path=var.replaceFirst("response.json","");

            Object actualObject = JsonPath.parse(response.getBody()).read("$"+path);

            if(actualObject == null) {
                LOG.info("\tJSON body{} = NULL", path);

                if(!val.equals("null")) {
                    if (throwErrorIfNotTrue){
                        throw new RuntimeException("Value JSON" + path + " does not match. Expected " + val + " but was NULL");
                    }
                    return false;
                }

            } else {

                String actual = actualObject.toString();

                LOG.info("\tJSON body{} = {}", path, actual);

                if (!val.equals(actual)) {
                    if (throwErrorIfNotTrue){
                        throw new RuntimeException("Value JSON" + path + " does not match. Expected " + val + " but was " + actual);
                    }
                    return false;
                }
            }
        }
        else {
            throw new RuntimeException("Syntax error. Expect in HTTP clause \"" + var + " = " + val +"\".");
        }

        return true;
    }

    // TODO: multiple or in or does not work
    private boolean isSubConditionTrue(String curr, Http.HttpResp response){
        curr = curr.substring(1, curr.length()-1);
        if (curr.startsWith("or=")) {
            curr = curr.substring(4, curr.length() - 1);

            String subCondition = null;

            // Check if contains another sub condition and remove it from curr
            if (curr.contains("{")){
                int startArray = curr.indexOf("{");
                int endArray = curr.lastIndexOf("}");

                subCondition = curr.substring(startArray, endArray + 1);
                curr = curr.replace(" " + subCondition + ",", "");
            }

            String[] orLinesString = curr.split(",");
            Map<String, List<String>> orLines = new HashMap<>();

            // Fold every val (that are not sub conditions) by the var checked
            for (String s : orLinesString) {
                Variable v = extractVariable(s);
                String var = v.var;
                String val = v.value;

                if (!orLines.keySet().contains(var)) {
                    orLines.put(var, new ArrayList<>());
                }
                orLines.get(var).add(val);
            }

            // Check if one of the conditions (that are not sub conditions) is true
            boolean isOrTrue = false;
            for (String k : orLines.keySet()) {
                for (String s : orLines.get(k)) {
                    LOG.info("OR sub condition, checks whether {} = {}", k, s);
                    if (isConditionTrue(k, s, response, false)) {
                        isOrTrue = true;
                        break;
                    }
                }
                if (isOrTrue) {
                    break;
                }
            }
            // Check if we find a condition or a sub condition true
            if (!isOrTrue) {
                if (subCondition != null && isSubConditionTrue(subCondition, response)) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    public void httpCheck(List<Object> expect, Http.HttpResp response){
        if (expect==null) {
            return;
        }

        for (int i = 0; i < expect.size(); i++) {
            String curr = expect.get(i).toString();

            if (curr.startsWith("{")){
                if (!isSubConditionTrue(curr, response)){
                    throw new RuntimeException("Sub condition returns false");
                }
            }
            else{
                Variable v = extractVariable(curr);
                String var = v.var;
                String val = v.value;

                LOG.info("Checks whether {} = {}", var, val);

                isConditionTrue(var, val, response, true);
            }
        }
    }

}
