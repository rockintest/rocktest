package io.rocktest.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.rocktest.RockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSon extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(JSon.class);


    private String extractPath(String json, String ppath) throws JsonProcessingException {
        try {
            String path=ppath.trim();
            if(!path.startsWith("["))
                path="."+path;

            Object item = JsonPath.parse(json).read("$" + path);

            String itemAsString;

            if(item instanceof String) {
                itemAsString = (String)item;
            } else {
                itemAsString = new ObjectMapper().writeValueAsString(item);
            }

            LOG.debug("Value : {}",itemAsString);
            return itemAsString;
        } catch(PathNotFoundException e) {
            LOG.debug("No value found");
            return null;
            // Not found. OK, result will be null.
        }

    }


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


        ret.put("result",extractPath(json,path));

        return ret;
    }


    public Map<String,Object> check(Map<String,Object> params) throws JsonProcessingException {

        Map<String, Object> ret = new HashMap<>();

        String json=getStringParam(params,"json");
        Map<String,Object> equals=getMapParam(params,"equals",null);
        Map<String,Object> match=getMapParam(params,"match",null);

        if(equals != null) {

            for (Map.Entry<String, Object>  entry : equals.entrySet()) {
                LOG.debug("Check JSON path {} = {}",entry.getKey(),entry.getValue());
                String actual=extractPath(json,entry.getKey());
                LOG.debug("Actual value: {}",actual);

                String expected = String.valueOf(entry.getValue());

                if(! actual.equals(expected)) {
                    fail("JSON content at path "+entry.getKey()+" does not match. Expected "+expected+" but was "+actual);
                }
            }

        }

        if(match != null) {

            for (Map.Entry<String, Object>  entry : match.entrySet()) {
                LOG.debug("Check JSON path {} matches {}",entry.getKey(),entry.getValue());
                String actual=extractPath(json,entry.getKey());
                LOG.debug("Actual value: {}",actual);

                String expected = String.valueOf(entry.getValue());

                Pattern p = Pattern.compile(expected,Pattern.DOTALL);
                Matcher m = p.matcher(actual);

                if (!m.find()) {
                    fail("JSON content at path "+entry.getKey()+" does not match REGEX. Expected "+expected+" but was "+actual);
                }

            }

        }


        return ret;

    }

}
