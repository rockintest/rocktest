package io.rocktest.modules;

import io.rocktest.Scenario;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Setter
public class RockModule {

    protected Scenario scenario;

    private static Logger LOG = LoggerFactory.getLogger(Http.class);

    public Map expand(Map notExpanded) {
        return scenario.expand(notExpanded);
    }


    public String getSetting(String label) {
        return scenario.getEnv().getProperty(label);
    }


    public String getStringParam(Map params, String key) {
        String ret=getStringParam(params,key,null);
        if(ret==null)
            throw new RuntimeException(key + " param mandatory");

        return ret;
    }

    public String getStringParam(Map params, String key, String def) {

        if(params==null)
            return def;

        Object o = params.get(key);

        if(o == null) {
            return def;
        }

        if (!(o instanceof String || o instanceof Number)) {
            throw new RuntimeException(key + " param must be a string but is \""+String.valueOf(o)+"\"");
        }

        return String.valueOf(o);

    }


    public Integer getIntParam(Map params, String key) {

        Integer ret=getIntParam(params,key,null);
        if(ret==null)
            throw new RuntimeException(key + " param mandatory");

        return ret;
    }


    public Integer getIntParam(Map params, String key, Integer def) {
        if(params==null)
            return def;

        Object o = params.get(key);

        if(o == null) {
            return def;
        }

        if (!(o instanceof Number)) {
            throw new RuntimeException(key + " param must be a integer but is \""+String.valueOf(o)+"\"");
        }

        return (Integer)o;

    }

    public Boolean getBooleanParam(Map params, String key, Boolean def) {
        if(params==null)
            return def;

        Object o = params.get(key);

        if(o == null) {
            return def;
        }

        if (!(o instanceof Boolean)) {
            throw new RuntimeException(key + " param must be a boolean but is \""+String.valueOf(o)+"\"");
        }

        return (Boolean)o;

    }

    public List getArrayParam(Map params, String key) {
        List ret=getArrayParam(params,key,null);
        if(ret==null)
            throw new RuntimeException(key + " param mandatory");

        return ret;
    }


    public List getArrayParam(Map params, String key, List def) {
        if(params == null)
            return def;

        Object o = params.get(key);

        if(o == null) {
            return def;
        }

        if (!(o instanceof List)) {
            throw new RuntimeException(key + " param must be an array of string but is \""+String.valueOf(o)+"\"");
        }

        return (List)o;

    }

    public void logJson(String message,String content) {

        try {
            if (content == null) {
                LOG.info("{} : null", message);
            } else if (content.isEmpty()) {
                LOG.info("{} : <empty>", message);
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

    // Default implementation : nothing to cleanup
    public void cleanup() {
    }

}
