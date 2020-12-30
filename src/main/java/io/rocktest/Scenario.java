package io.rocktest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.JsonPath;

import io.rocktest.modules.Http;
import io.rocktest.modules.RockModule;
import io.rocktest.modules.Sql;
import lombok.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;


@Component
@NoArgsConstructor
@Setter
@Getter
public class Scenario {


    @Getter
    @Setter
    @AllArgsConstructor
    private class Variable {
        private String var;
        private String value;
    }

    @Autowired
    private Environment env;

    private String currentDesc = "";
    private int currentStep = 0;
    private String title = "";

    // Variables.
    // Key = scenario name (top of the stack)
    // Value = variables
    private Map<String, Map<String, String>> context;
    private HashMap<String, String> last = new HashMap<>();

    private StringSubstitutor subLast = new StringSubstitutor(last);
    private StringSubstitutor subCond;

    // Quote the parameters for inline syntax
    private StringSubstitutor subQuoter;

    private static final StringSubstitutor subEnv = new StringSubstitutor(System.getenv());

    // Call stack
    private List<String> stack;

    // Script directory
    private String dir;

    // Local functions
    private Map<String, List<Map>> functions=new HashMap<>();

    // Global objects (params, ...)
    // 1 instance, injected by the 1st call to the run method
    private Map<String,Object> glob;

    private static Logger LOG = LoggerFactory.getLogger(Scenario.class);
    public static Logger LINE = LoggerFactory.getLogger("noprefix");

    // HashMap for each instance of modules
    private HashMap<String, Object> moduleInstances;

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

        StringBuilder b = new StringBuilder();

        boolean first = true;
        for (String curr : stack) {
            if (first)
                first = false;
            else
                b.append("/");

            b.append(curr);
        }
        return b.toString();
    }

    // Retourne le nom du script en cours (dernier élément de la stack d'appel)
    private String getCurrentName() {
        return stack.get(stack.size() - 1);
    }

    // Retourne la map des variables du scenario en cours
    public Map<String, String> getLocalContext() {
        return context.get(getCurrentName());
    }

    private String getVar(String var) {
        String ret = getLocalContext().get(var);
        return (ret == null ? "" : ret);
    }


    // Initialise la hashmap pour les variable de ce script
    public void initLocalContext() {

        initContext(getCurrentName());

        subCond = new StringSubstitutor(new DefValueCompute(this));
        subCond.setEnableSubstitutionInVariables(true);

        subQuoter = new StringSubstitutor(new ParamQuoter());
        subQuoter.setEnableSubstitutionInVariables(true);

    }

    void initContext(String name) {
        if (context.get(name) == null) {
            context.put(name, new HashMap<>());
        }
    }

    void deleteContext(String name) {
        context.remove(name);
    }

    public String expand(String val) {

        // First, quote the params for the inline syntax
        String ret = subQuoter.replace(val);

        ret = subLast.replace(ret);
        ret = subEnv.replace(ret);
        ret = subCond.replace(ret);

        return ret;
    }

    public List expand(List val) {
        ArrayList<Object> ret = new ArrayList<>();

        for (int i = 0; i < val.size(); i++) {
            Object o = val.get(i);

            if (o instanceof String) {
                ret.add(expand((String) val.get(i)));
            } else if (o instanceof Map) {
                ret.add(expand((Map) val.get(i)));
            } else if (o instanceof List) {
                ret.add(expand((List) val.get(i)));
            }

        }
        return ret;
    }

    public Map<String, Object> expand(Map<String, Object> in) {

        if(in==null)
            return null;

        HashMap<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : in.entrySet()) {

            if (entry.getValue() instanceof String) {
                String expanded = expand((String) entry.getValue());
                if (StringUtils.isNumeric(expanded)) {
                    ret.put(entry.getKey(), Integer.parseInt(expanded));
                } else {
                    ret.put(entry.getKey(), expanded);
                }
            } else if (entry.getValue() instanceof Map) {
                ret.put(entry.getKey(), expand((Map) entry.getValue()));
            } else if (entry.getValue() instanceof List) {
                ret.put(entry.getKey(), expand((List) entry.getValue()));
            } else if (entry.getValue() instanceof Number) {
                ret.put(entry.getKey(), (Number) entry.getValue());
            } else {
                throw new RuntimeException("Error expanding node. Type " + entry.getValue().getClass().getName() + " unexpected");
            }
        }
        return ret;
    }

    private void setContext(String name, Map<String, String> vars) {
        context.put(name, vars);
    }

    private void putCallerContext(String var, String value) {
        if (stack.size() == 1) {
            LOG.warn("Cannot return value in main scenario");
        } else {

            LOG.debug("Put variable {}={} in context {}",var,value,stack.get(stack.size() - 2));

            Map<String, String> callerContext = context.get(stack.get(stack.size() - 2));
            callerContext.put(var, value);
        }
    }

    private void doAssert(String assertType, Map<String, String> params) {

        switch (assertType) {
            case "equals":
                String actual = String.valueOf(params.get("actual"));
                if (actual == null) {
                    throw new RuntimeException("\"actual\" param is required");
                }

                String expected = String.valueOf(params.get("expected"));
                if (expected == null) {
                    throw new RuntimeException("\"expected\" param is required");
                }

                String msg = String.valueOf(params.get("message"));
                if (msg == null) msg = "";

                if (!actual.equals(expected)) {
                    throw new RuntimeException("Assert fail: " + msg + " - expected \"" + expected + "\" but was \"" + actual + "\"");
                }

                break;
            default:
                throw new RuntimeException("Bad assertion type :" + assertType);
        }
    }

    private void checkParams(List<String> p) {
        for (String curr : p) {
            if (getLocalContext().get(curr) == null) {
                throw new RuntimeException("Parameter " + curr + " is mandatory for module " + getCurrentName());
            }
        }
    }


    public Map exec(String function, Map<String, Object> params) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        String cls = function.substring(0, function.lastIndexOf('.'));
        Class<?> moduleClass = Class.forName(cls);

        Object module = moduleInstances.get(cls);
        if (module == null) {
            module = moduleClass.getDeclaredConstructor().newInstance();
            moduleInstances.put(cls, module);
        }

        ((RockModule) module).setScenario(this);
        String methodName = function.substring(function.lastIndexOf('.') + 1, function.length());

        Class<?>[] paramTypes = {Map.class};
        Method setNameMethod = module.getClass().getMethod(methodName, paramTypes);

        // Do we need to expand the parameters ?
        boolean expand;
        try {
            Field f = module.getClass().getDeclaredField("noExpand");
            f.setAccessible(true);
            String[] noExpand = (String[]) f.get(module);

            expand= !(Arrays.asList(noExpand).contains(methodName));

        } catch(NoSuchFieldException e) {
            expand=true;
        }

        if(expand) {
            params = expand(params);
        }

        Map<String, Object> ret = (Map<String, Object>) setNameMethod.invoke(module, params);

        if (ret != null) {
            for (String k : ret.keySet()) {
                if(ret.get(k)==null) {
                    getLocalContext().remove(methodName + "." + k);
                } else {
                    getLocalContext().put(methodName + "." + k, String.valueOf(ret.get(k)));
                }
            }
        }

        return ret;
    }


    public void cleanupModules() {
        moduleInstances.forEach((key, mod) -> {
            ((RockModule)mod).cleanup();
        });
    }


    private void checkSqlConnection() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        // If the SQL connection it not open, open it with the default params

        Object module = moduleInstances.get("io.rocktest.modules.Sql");
        if (module == null || ((Sql) module).getConnections().get("default") == null) {

            HashMap<String, Object> params = new HashMap<>();
            params.put("url", datasourceUrl);
            params.put("user", datasourceUser);
            params.put("password", datasourceUser);
            params.put("delay", checkDelay);
            params.put("retry", checkRetry);
            params.put("name", "default");

            exec("io.rocktest.modules.Sql.connect", params);
        }
    }

    private void execSql(String req, List expect) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        checkSqlConnection();
        HashMap<String, Object> params = new HashMap<>();
        params.put("request", req);
        params.put("expect", expect);

        Map ret = exec("io.rocktest.modules.Sql.request", params);

        // Put $0 ... $n variables
        if (ret != null) {
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

        String code = String.valueOf(retexec.get("code"));
        String body = String.valueOf(retexec.get("body"));

        Http.HttpResp ret = new Http.HttpResp(Integer.valueOf(code), body);
        return ret;
    }


    private Http.HttpResp httpRequest(String method, String url, String bodyin) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("body", bodyin);

        Map retexec = exec("io.rocktest.modules.Http." + method, params);

        String code = String.valueOf(retexec.get("code"));
        String body = String.valueOf(retexec.get("body"));

        Http.HttpResp ret = new Http.HttpResp(Integer.valueOf(code), body);
        return ret;
    }


    private Http.HttpResp httpDelete(String url) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("url", url);

        Map retexec = exec("io.rocktest.modules.Http.delete", params);

        String code = String.valueOf(retexec.get("code"));
        String body = String.valueOf(retexec.get("body"));

        Http.HttpResp ret = new Http.HttpResp(Integer.valueOf(code), body);
        return ret;
    }

    private Http.HttpResp httpPost(String url, String bodyin) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("body", bodyin);

        Map retexec = exec("io.rocktest.modules.Http.post", params);

        String code = String.valueOf(retexec.get("code"));
        String body = String.valueOf(retexec.get("body"));

        Http.HttpResp ret = new Http.HttpResp(Integer.valueOf(code), body);
        return ret;
    }


    private Http.HttpResp httpPut(String url, String bodyin) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("body", bodyin);

        Map retexec = exec("io.rocktest.modules.Http.put", params);

        String code = String.valueOf(retexec.get("code"));
        String body = String.valueOf(retexec.get("body"));

        Http.HttpResp ret = new Http.HttpResp(Integer.valueOf(code), body);
        return ret;
    }


    public void call(String mod, Map params) throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        // If we have the syntax module->function or ->function
        Pattern p = Pattern.compile("(.*) *-> *(.*)");
        Matcher m = p.matcher(mod);

        if (!m.matches()) {

            if(functions.get(mod) != null) {
                callInternal(mod,params);
            } else {
                callExternal(mod, params,null);
            }

        } else {
            if (m.group(1).equals("")) {
                callInternal(m.group(2), params);
            } else {
                callExternal(m.group(1), params, m.group(2));
            }
        }
    }

    public void callInternal(String function, Map params) throws NoSuchMethodException, InterruptedException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        LOG.debug("Call function {}",function);

        stack.add(function);

        if (params != null)
            setContext(function, expand(params));

        String err = run((List<Map>) functions.get(function), dir, context, stack,glob);

        // Pop context
        deleteContext(function);
        stack.remove(stack.size() - 1);

        // Recreates the string substitors with the local context
        initLocalContext();

        if (err != null) {
            LOG.error("Error : {}", err);
            System.exit(1);
        }

    }


    public void callExternal(String mod, Map params,String function) throws IOException, InterruptedException {

        Scenario module = new Scenario();
        module.env=this.env;
        module.setModuleInstances(this.moduleInstances);

        String file = dir + "/" + mod;

        if (!file.endsWith(".yaml")) {
            file = file.concat(".yaml");
        }

        String moduleName = new File(file).getName().replace(".yaml", "");
        if(function!=null) {
            moduleName = moduleName.concat(".").concat(function);
        }

        // Push context for submodule
        stack.add(moduleName);

        if (params != null)
            setContext(moduleName, expand(params));

        String err = module.run(file, dir, context, stack,function,glob);

        // Pop context
        deleteContext(moduleName);
        stack.remove(stack.size() - 1);

        if (err != null) {
            LOG.error("Error : {}", err);
            System.exit(1);
        }

    }


    private void extractFunctions(List<Map> steps) {
        for (int i = 0; i < steps.size(); i++) {
            Step step = new Step(steps.get(i));

            if (step.getType().equals("function")) {
                List stepsFunction = step.getSteps();
                String name = step.getName();
                functions.put(name, stepsFunction);
                LOG.debug("Function {} declared", name);
            }
        }
    }


    public String main(String name, String dir, Map<String, Map<String, String>> context, List stack, Map<String,Object> glob) throws IOException, InterruptedException {
        try {
            return run(name, dir, context, stack, null, glob);
        } finally {
            cleanupModules();
        }
    }



    public String run(String name, String dir, Map<String, Map<String, String>> context, List stack, String function,Map<String,Object> glob) throws IOException, InterruptedException {
        LOG.info("Load scenario. name={}, dir={}", name, dir);
        this.glob=glob;

        try {
            this.dir = dir;
            Object mapper = new ObjectMapper(new YAMLFactory());
            List<Map> steps = ((ObjectMapper) mapper).readValue(new File(name), new TypeReference<List<Map>>() {});
            extractFunctions(steps);

            if(function==null)
                return run(steps, dir, context, stack,glob);
            else {
                List<Map> stepsFunction=functions.get(function);
                if(stepsFunction==null) {
                    throw new RuntimeException("Function "+function+" not declared in module "+name);
                }
                return run(stepsFunction,dir, context, stack,glob);
            }


        } catch (Exception e) {

            String basename = FilenameUtils.getBaseName(name);
            MDC.remove("position");

            LOG.error("Scen {} {}, Step #{} {} - Scenario FAILURE", basename, title, currentStep, currentDesc);
            LOG.error("Exception", e);
            return "Scen " + basename + " [" + title + "] step #" + currentStep + " " + currentDesc + " " + e.getMessage();
        }
    }


    public String run(List<Map> steps, String dir,Map<String, Map<String, String>> context, List stack,Map<String,Object> glob) throws IOException, InterruptedException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        this.context = context;
        this.stack = stack;
        this.dir = dir;

        initLocalContext();

        boolean skiped=false;

        LINE.info("----------------------------------------");

        for (int i = 0; i < steps.size(); i++) {
            Step step = new Step(steps.get(i));

            switch (step.getType()) {
                // Do not execute a function until it is called
                case "function":
                    continue;
                case "skip" :
                    skiped=true;
                    break;
                case "resume" :
                    skiped=false;
                    break;
            }

            // Skip steps if necessary
            if(skiped || step.getType().equals("resume"))
                continue;

            if(step.getType().trim().startsWith("#"))
                continue;

            currentStep = i + 1;
            currentDesc = (step.getDesc() != null ? "(" + step.getDesc() + ") " : "");

            String currentValue;
            String valueDetail;

            if (step.getValue() == null) {
                currentValue = "";
                valueDetail = "";
            } else {
                currentValue = expand(step.getValue());
                valueDetail = (currentValue.equals(step.getValue()) ? currentValue : step.getValue() + " => " + currentValue);
            }

            MDC.put("stack", getStack());
            MDC.put("step", "" + (i + 1));
            MDC.put("position", "[" + getStack() + "] Step#" + (i + 1));

            LOG.info("{}{}{}{}",
                    currentDesc,
                    step.getType(),
                    (valueDetail.isEmpty()?"":","),
                    valueDetail);

            switch (step.getType()) {
                case "exec":
                    exec(step.getValue(), step.getParams());
                    break;
                case "checkParams":
                    checkParams(step.getValues());
                    break;
                case "assert":
                    doAssert(currentValue, expand(step.getParams()));
                    break;
                case "return":
                    if (step.getName() == null)
                        returnVar(expand(step.getValue()));
                    else
                        returnVar(expand(step.getName()), currentValue);
                    break;
                case "var":
                    if (step.getName() == null)
                        setVar(currentValue);
                    else
                        setVar(expand(step.getName()), currentValue);
                    break;
                case "exit":
                    LOG.info("Exit");
                    i = steps.size();
                    break;
                case "title":
                    title = currentValue;
                    break;
                case "display":
                    LOG.info(currentValue);
                    break;
                case "request":
                    execSql(currentValue, null);
                    break;
                case "pause":
                    Thread.sleep(Integer.parseInt(step.getValue()) * 1000);
                    break;
                case "http-get": {
                    Http.HttpResp resp = httpRequest("get", currentValue, null);
                    httpCheck(step.getExpect(), resp);
                }
                break;
                case "http-post": {
                    Http.HttpResp resp = httpRequest("post", currentValue, step.getBody());
                    httpCheck(step.getExpect(), resp);
                }
                break;
                case "http-put": {
                    Http.HttpResp resp = httpRequest("put", currentValue, step.getBody());
                    httpCheck(step.getExpect(), resp);
                }
                break;
                case "http-delete": {
                    Http.HttpResp resp = httpRequest("delete", currentValue, null);
                    httpCheck(step.getExpect(), resp);
                }
                break;
                case "call":
                    call(step.getValue(), step.getParams());
                    break;
                case "check":
                    execSql(currentValue, step.getExpect());
                    break;

                // Those steps are handled by the first switch, at the top of the function
                case "function":
                case "skip":
                case "resume":
                    break;
                default:

                    String method = env.getProperty("modules." + step.getType()+ ".function");
                    if (method == null)
                        throw new RuntimeException("Type " + step.getType() + " unknown");

                    exec(method, step.getParams());
            }

            LINE.info("----------------------------------------");

        }

        MDC.remove("position");

        return null;
    }


    public Variable extractVariable(String exp) {
        Pattern p = Pattern.compile("[ ]*([^ ]+)[ ]*=[ ]*(.+)[ ]*",Pattern.DOTALL);
        Matcher m = p.matcher(exp);

        if (!m.find()) {
            throw new RuntimeException("Syntax error. Declaration \"" + exp + "\" must be formed \"<VAR>=<VALUE>\".");
        }

        String var = m.group(1);
        String val = m.group(2);

        return new Variable(var, val);
    }


    public void returnVar(String exp) {
        Variable v = extractVariable(exp);
        returnVar(v.var,v.value);
    }

    public void returnVar(String var, String value) {
        LOG.info("Return variable {} = {}", var, value);
        putCallerContext(getCurrentName() + "." + var, value);
    }

    public void setVar(String exp) {
        Variable v = extractVariable(exp);
        setVar(v.var,v.value);
    }

    private void setVar(String var, String value) {
        LOG.info("Set variable {} = {}", var, value);
        getLocalContext().put(var, value);
    }


    // Return false or throws an exception if a condition is false
    private boolean isConditionTrue(String var, String val, Http.HttpResp response, boolean throwErrorIfNotTrue) {
        if (var.equals("code")) {
            LOG.info("\tResponse code = {}", response.getCode());

            String status = "" + response.getCode();

            if (!val.equals(status)) {
                if (throwErrorIfNotTrue) {
                    throw new RuntimeException("Status code does not match. Expected " + val + " but was " + status);
                }
                return false;
            }
            LOG.info("OK");

        } else if (var.startsWith("response.json")) {

            String path = var.replaceFirst("response.json", "");

            Object actualObject = JsonPath.parse(response.getBody()).read("$" + path);

            if (actualObject == null) {
                LOG.info("\tJSON body{} = NULL", path);

                if (!val.equals("null")) {
                    if (throwErrorIfNotTrue) {
                        throw new RuntimeException("Value JSON" + path + " does not match. Expected " + val + " but was NULL");
                    }
                    return false;
                }

            } else {

                String actual = actualObject.toString();

                LOG.info("\tJSON body{} = {}", path, actual);

                if (!val.equals(actual)) {
                    if (throwErrorIfNotTrue) {
                        throw new RuntimeException("Value JSON" + path + " does not match. Expected " + val + " but was " + actual);
                    }
                    return false;
                }
            }
        } else {
            throw new RuntimeException("Syntax error. Expect in HTTP clause \"" + var + " = " + val + "\".");
        }

        return true;
    }

    // TODO: multiple or in or does not work
    private boolean isSubConditionTrue(String curr, Http.HttpResp response) {
        curr = curr.substring(1, curr.length() - 1);
        if (curr.startsWith("or=")) {
            curr = curr.substring(4, curr.length() - 1);

            String subCondition = null;

            // Check if contains another sub condition and remove it from curr
            if (curr.contains("{")) {
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

    public void httpCheck(List<Object> expect, Http.HttpResp response) {
        if (expect == null) {
            return;
        }

        for (int i = 0; i < expect.size(); i++) {
            String curr = expect.get(i).toString();

            if (curr.startsWith("{")) {
                if (!isSubConditionTrue(curr, response)) {
                    throw new RuntimeException("Sub condition returns false");
                }
            } else {
                Variable v = extractVariable(curr);
                String var = v.var;
                String val = v.value;

                LOG.info("Checks whether {} = {}", var, val);

                isConditionTrue(var, val, response, true);
            }
        }
    }

}
