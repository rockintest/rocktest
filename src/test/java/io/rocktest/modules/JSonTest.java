package io.rocktest.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.rocktest.RockTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JSonTest extends RockTest {

    @Test
    public void parse() throws JsonProcessingException {

        JSon module=new JSon();
        module.setScenario(scenario);

        String json="{ \"singer\" : \"Springsteen\",  \"album\" : \"The River\" }";

        HashMap params=new HashMap();
        params.put("json",json);
        params.put("path","album");

        Map ret=module.parse(params);

        String item=(String)ret.get("result");
        Assert.assertEquals("The River",item);

    }

    @Test
    public void parseNotFound() throws JsonProcessingException {

        JSon module=new JSon();
        module.setScenario(scenario);

        String json="{ \"singer\" : \"Springsteen\",  \"album\" : \"The River\" }";

        HashMap params=new HashMap();
        params.put("json",json);
        params.put("path","playlist");

        Map ret=module.parse(params);

        String item=(String)ret.get("result");
        Assert.assertNull(item);

    }


}
