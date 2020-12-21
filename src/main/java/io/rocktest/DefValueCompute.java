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

        // On regarde si on a une expression du genre
        // ${variable?value if set::value if not set}
        Pattern p = Pattern.compile("([^?]+)\\?(.+)::(.+)");
        Matcher m = p.matcher(tmp);

        if(!m.find()) {

            // On regarde si on a
            // ${variable::default value}
            Pattern p2 = Pattern.compile("(.+)::(.+)");
            Matcher m2 = p2.matcher(tmp);

            if(!m2.find()) {
                return null;
            }

            String var=m2.group(1).trim();
            String valIfNotSet=m2.group(2);

            String ret=System.getenv(var);
            if(ret != null)
                return ret;

            ret=context.get(var);
            if( ret == null) {
                return valIfNotSet;
            } else {
                return ret;
            }

        }

        String var=m.group(1).trim();
        String valIfSet=m.group(2);
        String valIfNotSet=m.group(3);

        if(context.get(var) == null) {
            return valIfNotSet;
        } else {
            return valIfSet;
        }

    }
}
