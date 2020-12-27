package io.rocktest;

import org.apache.commons.text.lookup.StringLookup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamQuoter implements StringLookup {

    @Override
    public String lookup(String s) {

        // De we have expression like
        // ${module(p1,p2).path}
        Pattern p = Pattern.compile("\\$([^(]+)\\(((?:[^,]+)?(?:,[^,]+)*)\\)(?:\\.(.+))?",Pattern.DOTALL);
        Matcher m = p.matcher(s);

        if(m.find()) {

            String module = m.group(1);
            String params = m.group(2);
            String path = m.group(3);

            if(params.startsWith("<<[") || params.isEmpty())
                return null;

            StringBuilder sb=new StringBuilder();
            sb.append("${$");
            sb.append(module);
            sb.append("(");

            String[] paramArray = params.split(",");


            for (int i = 0; i < paramArray.length; i++) {
                sb.append("<<[");
                sb.append(paramArray[i]);
                sb.append("]>>");
                if(i != paramArray.length -1) {
                    sb.append(",");
                }
            }

            sb.append(")");

            if(path != null && !path.isEmpty()) {
                sb.append(".");
                sb.append(path);
            }

            sb.append("}");

            return sb.toString();
        }

        return null;
    }
}
