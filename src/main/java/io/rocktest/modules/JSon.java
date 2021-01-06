package io.rocktest.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class JSon extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(JSon.class);

    public Map<String,Object> parse(Map<String,Object> params) throws JsonProcessingException {

        Map<String,Object> ret=new HashMap<>();

        String json=getStringParam(params,"json");
        String path=getStringParam(params,"path");

        // If the path is empty, avoid an exception and return a blank string
        if(json.isEmpty()) {
            LOG.debug("No value found");
            ret.put("result",null);
            return ret;
        }

        path=path.trim();
        if(!path.startsWith("["))
            path="."+path;

        try {
            Object item = JsonPath.parse(json).read("$" + path);

            String itemAsString;

            if(item instanceof String) {
                itemAsString = (String)item;
            } else {
                itemAsString = new ObjectMapper().writeValueAsString(item);
            }

            LOG.debug("Value : {}",itemAsString);
            ret.put("result",itemAsString);
        } catch(PathNotFoundException e) {
            LOG.debug("No value found");
            ret.put("result",null);
            // Not found. OK, result will be null.
        }

        return ret;
    }

}
