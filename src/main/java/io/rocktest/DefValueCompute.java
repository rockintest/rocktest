package io.rocktest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@Getter
@Setter
public class DefValueCompute  implements StringLookup {

    private Map<String,Object> context;
    private StringSubstitutor subContext;
    private Scenario scenario;

    public DefValueCompute(Scenario s) {
        this.scenario=s;
        this.context=s.getLocalContext();
        subContext=new StringSubstitutor(context);
    }

    @SneakyThrows
    @Override
    public String lookup(String s) {

        String tmp=System.getenv().get(s);
        if(tmp!=null)
            return tmp;

        Object otmp=context.get(s);
        if(otmp!=null)
            return String.valueOf(otmp);

        tmp = subContext.replace(s);

        // Do we have expression like
        // ${variable?value if set::value if not set} or
        // ${variable::value if not set}
        Pattern p = Pattern.compile("([^?]+)(?:\\?(.*))?::(.*)",Pattern.DOTALL);
        Matcher m = p.matcher(tmp);

        if(m.find()) {
            String var=m.group(1).trim();
            String valIfSet=m.group(2);
            String valIfNotSet=m.group(3);

            // On n'a pas de "value if set"
            if(valIfSet==null) {
                String ret=System.getenv(var);
                if(ret != null)
                    return ret;

                Object oret=context.get(var);
                if( oret == null) {
                    return valIfNotSet;
                } else {
                    return String.valueOf(oret);
                }

            } else {

                if(context.get(var) == null) {
                    return valIfNotSet;
                } else {
                    return valIfSet;
                }

            }

        } else {

            // De we have expression like
            // ${module(p1,p2).path}
            p = Pattern.compile("\\$([^(]+)\\(((?:[^,]+)?(?:,[^,]+)*)\\)(?:\\.(.+))?",Pattern.DOTALL);
            m = p.matcher(tmp);

            if(m.find()) {

                String module=m.group(1);
                String params=m.group(2);
                String extension=m.group(3);

                String method = scenario.getEnv().getProperty("modules." + module+".function");
                if (method == null)
                    throw new RuntimeException("Module " + module + " unknown");

                String result = scenario.getEnv().getProperty("modules." + module+".result");
                if (result == null)
                    throw new RuntimeException("Result not available for module " + module);

                HashMap<String,Object> paramsMap=null;
                if(params != null && !params.isEmpty()) {

                    // Extract params (p1,p2,...) or (param1:=value1, param2:=value2...)
                    // and put them according to their values in the hash table

                    paramsMap=new HashMap<>();

                    if(extension!=null && !extension.isEmpty()) {
                        String paramExtension = scenario.getEnv().getProperty("modules." + module+".extension");
                        if (paramExtension == null)
                            throw new RuntimeException("Param extension not available for module " + module);

                        paramsMap.put(paramExtension,extension);
                    }

                    String[] paramArray = params.split("\\]>>,<<\\[");
                    for (int i = 0; i < paramArray.length; i++) {

                        String current=paramArray[i];

                        p = Pattern.compile(" *<<\\[(.+)",Pattern.DOTALL);
                        m = p.matcher(current);
                        if(m.matches()) {
                            current=m.group(1);
                        }

                        p = Pattern.compile("(.+)\\]>>",Pattern.DOTALL);
                        m = p.matcher(current);
                        if(m.matches()) {
                            current=m.group(1);
                        }


                        p = Pattern.compile(" *(.+) *:= *(.+) *",Pattern.DOTALL);
                        m = p.matcher(current);

                        if(m.matches()) {

                            String paramName=m.group(1);
                            String paramValue=m.group(2);
                            paramsMap.put(paramName, paramValue);

                        } else {
                            String paramName = scenario.getEnv().getProperty("modules." + module + ".params." + (i + 1));
                            if (paramName == null) {
                                throw new RuntimeException("Param #" + (i+1) + " undefined for module " + module);
                            }
                            paramsMap.put(paramName, current);
                        }
                    }
                }

                Map ret = scenario.exec(method,paramsMap);

                Object o=ret.get(result);
                if(o!=null)
                    return String.valueOf(o);
            }

        }

        return null;

    }
}
