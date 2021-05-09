package io.rocktest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.rocktest.modules.Http;
import io.rocktest.modules.RockModule;
import io.rocktest.modules.Sql;
import io.rocktest.modules.annotations.NoExpand;
import io.rocktest.modules.annotations.RockWord;
import io.rocktest.modules.meta.ModuleInfo;
import io.rocktest.modules.meta.Modules;
import lombok.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;


@Component
@NoArgsConstructor
@Setter
@Getter
public class Scenario {


    @Getter
    @Setter
    @AllArgsConstructor
    public class Variable {
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
    private Map<String, Map<String, Object>> context;
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

    // A sub context, just for info (used for Gherkin scenarios)
    private String subContext=null;

    public void pushStack(String s) {
        stack.add(s);
    }

    public void popStack() {
        stack.remove(stack.size()-1);
    }

    public String getStackDesc() {

        StringBuilder b = new StringBuilder();

        boolean first = true;
        for (String curr : stack) {
            if (first)
                first = false;
            else
                b.append("/");

            b.append(curr);
        }
        if(subContext!=null) {
            b.append("/");
            b.append(subContext);
        }
        return b.toString();
    }

    // Retourne le nom du script en cours (dernier élément de la stack d'appel)
    private String getCurrentName() {
        return stack.get(stack.size() - 1);
    }

    // Retourne la map des variables du scenario en cours
    public Map<String, Object> getLocalContext() {
        return context.get(getCurrentName());
    }

    private String getVar(String var) {
        Object ret = getLocalContext().get(var);
        return (ret == null ? "" : String.valueOf(ret));
    }


    // Initialise la hashmap pour les variable de ce script
    public void initLocalContext() {

        initContext(getCurrentName());

        subCond = new StringSubstitutor(new DefValueCompute(this));
        subCond.setEnableSubstitutionInVariables(true);

        subQuoter = new StringSubstitutor(new ParamQuoter());
        subQuoter.setEnableSubstitutionInVariables(true);

        setVar("module",getCurrentName());

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


    public Object expandElement(Object o) {
        if (o instanceof String) {
            String expanded = expand((String) o);
            if (StringUtils.isNumeric(expanded)) {
                return Long.parseLong(expanded);
            } else {
                return expanded;
            }
        } else if (o instanceof Map) {
            return (expand((Map) o));
        } else if (o instanceof List) {
            return (expand((List) o));
        } else if (o instanceof Number) {
            return ((Number) o);
        } else if (o instanceof Boolean) {
            return ((Boolean) o);
        } else {
            throw new RockException("Error expanding node. Type " + o.getClass().getName() + " unexpected");
        }

    }


    public List expand(List val) {
        if(val==null)
            return null;

        ArrayList<Object> ret = new ArrayList<>();

        for (int i = 0; i < val.size(); i++) {
            Object o = val.get(i);
            ret.add(expandElement(o));
        }
        return ret;
    }

    public Map<String, Object> expand(Map<String, Object> in) {

        if(in==null)
            return null;

        HashMap<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : in.entrySet()) {

            if(entry.getValue()==null) {
                ret.put(entry.getKey(),null);
            } else {
                ret.put(entry.getKey(),expandElement(entry.getValue()));
            }

        }
        return ret;
    }

    private void setContext(String name, Map<String, Object> vars) {
        context.put(name, vars);
    }

    private void putCallerContext(String var, String value) {
        if (stack.size() == 1) {
            LOG.warn("Cannot return value in main scenario");
        } else {

            LOG.debug("Put variable {}={} in context {}",var,value,stack.get(stack.size() - 2));

            Map<String, Object> callerContext = context.get(stack.get(stack.size() - 2));
            callerContext.put(var, value);
        }
    }

    private void checkParams(List<String> p) {
        for (String curr : p) {
            if (getLocalContext().get(curr) == null) {
                throw new RockException("Parameter " + curr + " is mandatory for module " + getCurrentName());
            }
        }
    }

    /**
     * Search for root cause as a RockException
     * @param t
     * @return
     */
    private RockException findRockCause(Throwable t) {
        Throwable etmp=t;

        if(t instanceof RockException) {
            return (RockException) t;
        }

        while(! (etmp instanceof RockException)) {
            if(etmp.getCause()==null) {
                break;
            }

            if(etmp.getCause() instanceof RockException) {
                return (RockException) etmp.getCause();
            }

            etmp=etmp.getCause();
        }

        return null;
    }


    public Map exec(String function, Map<String, Object> params) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        try {
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
            boolean noExpand = setNameMethod.isAnnotationPresent(NoExpand.class);

            if (!noExpand) {
                params = expand(params);
            }

            Map<String, Object> ret = (Map<String, Object>) setNameMethod.invoke(module, params);

            if (ret != null) {
                for (String k : ret.keySet()) {
                    if (ret.get(k) == null) {
                        getLocalContext().remove(methodName + "." + k);
                    } else {
                        getLocalContext().put(methodName + "." + k, String.valueOf(ret.get(k)));
                    }
                }
            }

            return ret;
        } catch(InvocationTargetException e) {

            RockException erock=findRockCause(e);
            if(erock != null) {
                erock.setModule(function);
                throw erock;
            }

            if(e.getCause()!=null) {
                erock = new RockException("Error invoking module "+function,e.getCause());
                LOG.error("Exception {} {} while calling module {}",e.getClass().getName(),(e.getMessage()!=null?e.getMessage():""),function,e.getCause());
            } else {
                erock = new RockException("Error invoking module "+function,e);
                LOG.error("Exception {} {} while calling module {}",e.getClass().getName(),(e.getMessage()!=null?e.getMessage():""),function,e);
            }

            erock.setModule(function);

            throw erock;

        } catch(ClassNotFoundException e) {

            RockException erock=new RockException("Cannot load module "+e.getMessage(),e);
            erock.setScenario(function);
            throw erock;

        } catch(NoSuchMethodException e) {

            RockException erock=new RockException("Cannot find method in module "+e.getMessage(),e);
            erock.setScenario(function);
            throw erock;

        }
    }


    public void cleanupModules() {
        moduleInstances.forEach((key, mod) -> {
            ((RockModule)mod).cleanup();
        });
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

        if(functions.get(function)==null) {
            throw new RockException("Function "+function+" does not exist");
        }

        stack.add(function);

        if (params != null)
            setContext(function, expand(params));

        run((List<Map>) functions.get(function), dir, context, stack,glob);

        // Pop context
        deleteContext(function);
        stack.remove(stack.size() - 1);

        // Recreates the string substitors with the local context
        initLocalContext();

    }


    /**
     * Expands the variables in the map, and add the variable from the context
     * if there is a special param context: all or context:var
     * @param params
     * @return
     */
    Map expandAndComplete(Map params) {

        if(params==null)
            return null;

        Object context=params.get("context");

        // Checks whether we have the special param "context" with value "all"
        if(context instanceof String && ((String) context).equalsIgnoreCase("all")) {

            Map<String,Object> ret = new HashMap<>(params);
            ret.remove("context");
            ret=expand(ret);

            // Put all the variables of the local context as parameters
            // But keep the parameters if they exist.
            // The passed parameters are priority on the context
            Map<String,Object> localContext=getLocalContext();
            for (Map.Entry<String, Object> entry : localContext.entrySet()) {
                if(ret.get(entry.getKey())==null)
                    ret.put(entry.getKey(),entry.getValue());
            }

            return ret;
        }

        // Do we have a list of variables to pass ?
        if(context instanceof List) {

            Map<String,Object> ret = new HashMap<>(params);
            ret.remove("context");
            ret=expand(ret);

            List<Object> vars = (List<Object>)params.get("context");

            Map<String,Object> localContext=getLocalContext();
            for(Object var : vars) {
                String varname=String.valueOf(var);
                Object inContext=localContext.get(varname);
                if(inContext!=null && ret.get(varname)==null) {
                    ret.put(varname,inContext);
                }
            }

            return ret;
        }

        // Nothing to pass from the context as parameters.
        // Just expand the params

        return expand(params);
    }


    public void callExternal(String mod, Map params,String function) throws IOException, InterruptedException {

        Scenario module = new Scenario();
        module.env=this.env;
        module.setModuleInstances(this.moduleInstances);

        // Ugly. The new scenario is not managed by Spring...
        // So we need to to the job ourselves.
        // To be fixed.
        module.checkDelay=checkDelay;
        module.checkRetry=checkRetry;
        module.datasourceUrl=datasourceUrl;
        module.datasourceUser=datasourceUser;
        module.datasourcePassword=datasourcePassword;

        String file = dir + "/" + mod;

        if (!file.endsWith(".yaml")) {
            file = file.concat(".yaml");
        }

        String moduleName = new File(file).getName().replace(".yaml", "");
        if(function!=null) {
            moduleName = moduleName.concat(".").concat(function);
        }

        // Expand params BEFORE push the name of the module
        // else, there will be a context mismatch
        Map paramsExpanded = expandAndComplete(params);

        // Push context for submodule
        stack.add(moduleName);

        if (params != null)
            setContext(moduleName, paramsExpanded);

        String err = module.run(file, dir, context, stack,function,glob);

        // Pop context
        deleteContext(moduleName);
        stack.remove(stack.size() - 1);

        if (err != null) {
            LOG.error("Error : {}", err);
            throw new RockException(err);
        }

    }


    private void extractFunctions(List<Map> steps) {
        int i=0;
        Step step=null;

        try {
            for (i = 0; i < steps.size(); i++) {
                step = null;
                step = new Step(steps.get(i));

                if (step.getType().equals("function")) {
                    List stepsFunction = step.getSteps();
                    String name = step.getName();
                    functions.put(name, stepsFunction);
                    LOG.debug("Function {} declared", name);
                }
            }
        } catch(RockException e) {
            String msg = "Error parsing scenario ";

            if(step==null) {
                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setPrettyFlow(true);
                Yaml yaml = new Yaml(options);
                msg+=yaml.dump(steps.get(i));
            }

            RockException erock=new RockException(msg,e);

            erock.setStepNumber(i+1);

            if(step!=null) {
                erock.setStep(step);
            }
            throw erock;

        } catch (Exception e) {

            LOG.error("Error parsing scenario ",e);

            String msg = "Error parsing scenario ";

            if(step==null) {
                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setPrettyFlow(true);
                Yaml yaml = new Yaml(options);
                msg+=yaml.dump(steps.get(i));
            }

            RockException erock=new RockException(msg,e);
            erock.setStepNumber(i+1);

            if(step!=null) {
                erock.setStep(step);
            }

            throw erock;
        }
    }

    /**
     * Finds all the class extending RockModule, and extract the module information
     */
    private void loadModules() {
        Reflections reflections = new Reflections("io.rocktest.modules");
        Set<Class<? extends RockModule>> classes = reflections.getSubTypesOf(RockModule.class);
        for (Class<? extends RockModule> aClass : classes) {

            for (Method method : aClass.getDeclaredMethods()) {

                if(method.isAnnotationPresent(RockWord.class)) {
                    RockWord w=(RockWord) method.getAnnotation(RockWord.class);
                    String v=w.keyword();

                    ModuleInfo info=new ModuleInfo();
                    info.setClassName(aClass.getName());
                    info.setClassType(aClass);
                    info.setMethod(method.getName());
                    info.setResult(w.result());
                    info.setExtension(w.extension());
                    info.setParams(w.params());

                    Modules.addModule(v,info);

                    LOG.debug("KeyWord {} => {}.{}",v,aClass.getName(),method.getName());
                }

            }

        }
    }



    public String main(String name, String dir, Map<String, Map<String, Object>> context, List stack, Map<String,Object> glob) throws IOException, InterruptedException {
        try {
            this.glob=glob;
            this.dir = dir;
            this.context = context;
            this.stack = stack;
            this.dir = dir;

            loadModules();
            initLocalContext();

            if(new File(dir+"/"+"setup.yaml").exists()) {
                callExternal("setup",null,null);
            }

            String result=run(name, dir, context, stack, null, glob);

            if(result==null) {
                LINE.info("========================================");
                LINE.info("=     Scenario Success ! It Rocks      =");
                LINE.info("========================================");
            } else {
                LINE.error("=======================================");
                LINE.error("          Scenario failure             ");
                LINE.error("");
                LINE.error(result);
                LINE.error("");
                LINE.error("=======================================");
            }

            return result;
        } finally {
            cleanupModules();
        }
    }



    public String run(String name, String dir, Map<String, Map<String, Object>> context, List stack, String function,Map<String,Object> glob) throws IOException, InterruptedException {
        LOG.info("Load scenario. name={}, dir={}", name, dir);
        this.glob=glob;
        String basename = FilenameUtils.getBaseName(name);

        try {

            Object mapper = new ObjectMapper(new YAMLFactory());
            List<Map> steps = ((ObjectMapper) mapper).readValue(new File(name), new TypeReference<List<Map>>() {});
            extractFunctions(steps);

            if(function==null)
                run(steps, dir, context, stack,glob);
            else {
                List<Map> stepsFunction=functions.get(function);
                if(stepsFunction==null) {
                    throw new RockException("Function "+function+" not declared in module "+name);
                }
                run(stepsFunction,dir, context, stack,glob);
            }


        } catch(FileNotFoundException e) {

            LOG.error("Scen {} {}, Step #{} {} - Scenario FAILURE", basename, title, currentStep, currentDesc);
            if(e.getCause()!=null)
                LOG.error(e.getCause().getMessage());

            RockException erock=new RockException("Scenario not found",e);
            erock.setScenario(basename);

            return erock.getDescription();

        } catch (RockException e) {

            LOG.error("Scen {} {}, Step #{} {} - Scenario FAILURE", basename, title, currentStep, currentDesc);
            return e.getDescription();

        } catch (MismatchedInputException e) {

            LOG.error("Scen {} - Scenario FAILURE", basename);
            LOG.error("Parse error: {}",e.getMessage());

            RockException erock=new RockException("Scenario not found",e);
            erock.setScenario(basename);

            return erock.getDescription();

        } catch(JsonMappingException e) {

            LOG.error("Scen {} - Scenario FAILURE", basename);
            LOG.error("Syntax error in YAML");

            RockException erock=new RockException("Syntax error in yaml",e);
            erock.setScenario(basename);

            return erock.getDescription();

        } catch (Exception e) {

            LOG.error("Exception", e);
            LOG.error("Scen {} {}, Step #{} {} - Scenario FAILURE", basename, title, currentStep, currentDesc);
            if(e.getCause()!=null)
                LOG.error(e.getCause().getMessage());

            RockException erock=new RockException("Scenario not found",e);
            erock.setScenario(basename);

            return erock.getDescription();

        } finally {
            MDC.remove("position");
        }

        return null;
    }



    public void run(List<Map> steps) throws NoSuchMethodException, InterruptedException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        run(steps,dir,context,stack,glob);
    }


    public void run(List<Map> steps, String dir,Map<String, Map<String, Object>> context, List stack,Map<String,Object> glob) throws IOException, InterruptedException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        this.context = context;
        this.stack = stack;
        this.dir = dir;

        initLocalContext();

        boolean skiped=false;

        LINE.info("----------------------------------------");

        for (int i = 0; i < steps.size(); i++) {

            Step step=null;
            try {

                step = new Step(steps.get(i));

                switch (step.getType()) {
                    // Do not execute a function until it is called
                    case "function":
                        continue;
                    case "skip":
                        skiped = true;
                        break;
                    case "resume":
                        skiped = false;
                        break;
                }

                // Skip steps if necessary
                if (skiped || step.getType().equals("resume"))
                    continue;

                if (step.getType().trim().startsWith("--"))
                    continue;

                currentStep = i + 1;
                currentDesc = (step.getDesc() != null ? "(" + step.getDesc() + ") " : "");

                String currentValue;
                String valueDetail;

                MDC.put("stack", getStackDesc());
                MDC.put("step", "" + (i + 1));
                MDC.put("position", "[" + getStackDesc() + "] Step#" + (i + 1));

                // Set builtin var "step"
                getLocalContext().put("step", currentStep);

                if (step.getValue() == null) {
                    currentValue = "";
                    valueDetail = "";
                } else {
                    currentValue = expand(step.getValue());
                    valueDetail = (currentValue.equals(step.getValue()) ? currentValue : step.getValue() + " => " + currentValue);
                }

                LOG.info("{}{}{}{}",
                        currentDesc,
                        step.getType(),
                        (valueDetail.isEmpty() ? "" : ","),
                        valueDetail);

                LOG.trace("\n{}", step.toYaml());

                switch (step.getType()) {
                    case "exec":
                        exec(step.getValue(), step.getParams());
                        break;
                    case "checkParams":
                        checkParams(step.getValues());
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
                    case "pause":
                        if(step.getValue().equals("forever")) {
                            for(;;) {
                                Thread.sleep(1000000000);
                            }
                        } else {
                            try {
                                Thread.sleep(Integer.parseInt(step.getValue()) * 1000);
                            } catch(NumberFormatException e) {
                                throw new RockException("Pause parameter must be numeric, but is "+step.getValue());
                            }
                        }
                        break;

                    case "call":
                        call(step.getValue(), step.getParams());
                        break;

                    // Those steps are handled by the first switch, at the top of the function
                    case "function":
                    case "skip":
                    case "resume":
                        break;
                    default:

                        ModuleInfo info=Modules.getModule(step.getType());
                        if (info == null)
                            throw new RockException("Type " + step.getType() + " unknown");

                        String method=info.getClassName()+"."+info.getMethod();

                        exec(method, step.getParams());
                }

                LINE.info("----------------------------------------");

            } catch(Exception e) {

                // Find if a root cause is a RockException
                RockException erock = findRockCause(e);

                if(erock == null) {
                    LOG.error("Exception: ",e);
                    erock=new RockException("Exception "+e.getClass().getName(),e);
                }

                erock.setStep(step);
                erock.setStepNumber(currentStep);
                erock.setScenario(getCurrentName());
                erock.setStack(this.stack);
                throw erock;

            }
        }

        MDC.remove("position");

    }


    public Variable extractVariable(String exp) {
        Pattern p = Pattern.compile("[ ]*([^ ]+)[ ]*=[ ]*(.+)[ ]*",Pattern.DOTALL);
        Matcher m = p.matcher(exp);

        if (!m.find()) {
            throw new RockException("Syntax error. Declaration \"" + exp + "\" must be formed \"<VAR>=<VALUE>\".");
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

        if(var.trim().startsWith(".")) {
            putCallerContext(var.substring(1), value);
        } else {
            putCallerContext(getCurrentName() + "." + var, value);
        }
    }

    public void setVar(String exp) {
        Variable v = extractVariable(exp);
        setVar(v.var,v.value);
    }

    private void setVar(String var, String value) {
        LOG.info("Set variable {} = {}", var, value);
        getLocalContext().put(var, value);
    }

}
