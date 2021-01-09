package io.rocktest.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(Regex.class);

    public Map<String,Object> match(Map<String,Object> params) {

        Map<String,Object> ret=new HashMap<>();

        String pattern=getStringParam(params,"pattern");
        String str=getStringParam(params,"string");
        Integer group=getIntParam(params,"group",null);

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);

        if (!m.find()) {
            ret.put("match","false");
            return ret;
        }

        for(int i=1;i<=m.groupCount();i++) {
            ret.put(""+i,m.group(i));
        }
        ret.put("match","true");

        // If there is an extension, feed the "result" variable
        if(group != null) {
            ret.put("result",m.group(group));
        }

        return ret;
    }

}
