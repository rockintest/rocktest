package io.rocktest.modules;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RockModule {

    private static Logger LOG = LoggerFactory.getLogger(Http.class);

    public String getStringParam(Map params, String key, boolean mandatory) {

        Object o = params.get(key);

        if(o == null) {
            if (mandatory) {
                throw new RuntimeException(key + " param mandatory");
            } else {
                return null;
            }
        }

        if (!(o instanceof String)) {
            throw new RuntimeException(key + " param must be a string");
        }

        return (String)o;

    }


    public Integer getIntParam(Map params, String key, boolean mandatory) {

        Object o = params.get(key);

        if(o == null) {
            if (mandatory) {
                throw new RuntimeException(key + " param mandatory");
            } else {
                return null;
            }
        }

        if (!(o instanceof Number)) {
            throw new RuntimeException(key + " param must be a integer");
        }

        return (Integer)o;

    }


    public List getArrayParam(Map params, String key, boolean mandatory) {

        Object o = params.get(key);

        if(o == null) {
            if (mandatory) {
                throw new RuntimeException(key + " param mandatory");
            } else {
                return null;
            }
        }

        if (!(o instanceof List)) {
            throw new RuntimeException(key + " param must be an array of string");
        }

        return (List)o;

    }

    public void logJson(String message,String content) {

        try {
            if (content == null) {
                LOG.info("{} : null", message);
            } else if (content.trim().startsWith("{")) {
                JSONObject jsonObj = new JSONObject(content);
                LOG.info("{}\n{}", message, jsonObj.toString(4));
            } else if (content.trim().startsWith("[")) {
                JSONArray jsonArray = new JSONArray(content);
                LOG.info("{}\n{}", message, jsonArray.toString(4));
            } else {
                LOG.info("{}\n{}", message, content);
            }
        } catch(JSONException e) {
            LOG.warn("Cannot parse JSON : {}",e.getMessage());
            LOG.info("{}\n{}", message, content);
        }

    }

}
