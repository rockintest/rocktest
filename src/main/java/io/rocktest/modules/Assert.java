package io.rocktest.modules;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Assert extends RockModule {

    private static Logger LOG = LoggerFactory.getLogger(Assert.class);

    public Map<String,Object> equals(Map<String,Object> params) {

        Object actual = params.get("actual");
        if (actual == null) {
            fail("\"actual\" param is required");
        }

        Object expected = params.get("expected");
        if (expected == null) {
            fail("\"expected\" param is required");
        }

        String msg = getStringParam(params,"message","");

        LOG.debug("Actual value: {}",String.valueOf(actual));

        if (!String.valueOf(actual).equals(String.valueOf(expected))) {
            fail("Assert fail: " + msg + " - expected \"" + String.valueOf(expected) + "\" but was \"" + String.valueOf(actual) + "\"");
        }

        return null;

    }

    public Map<String,Object> match(Map<String,Object> params) {

        String actual=getStringParam(params,"actual");
        String expected=getStringParam(params,"expected");
        String msg = getStringParam(params,"message","");

        LOG.debug("Actual value: {}",String.valueOf(actual));

        Pattern p = Pattern.compile(expected,Pattern.DOTALL);
        Matcher m = p.matcher(actual);

        if(!m.find()) {
            fail("Assert fail: " + msg + " - expected \"" + actual + "\" does not match \"" + expected + "\"");
        }

        return null;
    }

}
