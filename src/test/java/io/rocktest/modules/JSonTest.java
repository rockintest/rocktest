package io.rocktest.modules;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.rocktest.Scenario;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JSonTest {

    private Scenario scenario;

    @Before
    public void initScenario() {
        scenario=new Scenario();
        Map<String, Map<String, String>> context=new HashMap<>();
        scenario.setContext(context);
        ArrayList<String> stack=new ArrayList<>();
        stack.add("junit");
        scenario.setStack(stack);
        scenario.initLocalContext();
        // HashMap for each instance of modules
        scenario.setModuleInstances(new HashMap<>());
    }

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
