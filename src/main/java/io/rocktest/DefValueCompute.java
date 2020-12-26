package io.rocktest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class DefValueCompute  implements StringLookup {

    private Map<String,String> context;
    private StringSubstitutor subContext;

    public DefValueCompute(Map<String,String> context) {
        this.context=context;
        subContext=new StringSubstitutor(context);
    }

    @Override
    public String lookup(String s) {

        String tmp=System.getenv().get(s);
        if(tmp!=null)
            return tmp;

        tmp=context.get(s);
        if(tmp!=null)
            return tmp;

        tmp= subContext.replace(s);

        // De we have expression like
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

                ret=context.get(var);
                if( ret == null) {
                    return valIfNotSet;
                } else {
                    return ret;
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
            p = Pattern.compile("([^(]+)\\(((?:[^,]+)?(?:,[^,]+)*)\\)(?:\\.(.+))?",Pattern.DOTALL);
            m = p.matcher(tmp);

            if(m.find()) {

                String module=m.group(1);
                String params=m.group(2);
                String path=m.group(3);

            }

        }

        return null;

    }
}
