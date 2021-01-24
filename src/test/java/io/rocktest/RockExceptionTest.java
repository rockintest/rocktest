package io.rocktest;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

public class RockExceptionTest {

    @Test
    public void testMessage() {

        HashMap<String,Object> m=new HashMap<>();

        HashMap<String,String> params=new HashMap<>();
        params.put("param1","value1");
        params.put("param2","value2");
        params.put("param2","value2");

        ArrayList<String> expect=new ArrayList<>();
        expect.add("expect1");
        expect.add("expect2");

        m.put("type","type");
        m.put("value","value");
        m.put("expect",expect);
        m.put("params",params);

        Step s=new Step(m);

        System.out.println(s.toYaml());
        System.out.println(s.toYaml("step"));

        RuntimeException from=new RuntimeException("root cause");
        RockException e=new RockException("rock message",from);

        e.setModule("module");
        e.setScenario("scenario");
        e.setStepNumber(100);
        e.setStep(s);

        ArrayList<String> stack=new ArrayList<>();
        stack.add("stack1");
        stack.add("stack2");
        e.setStack(stack);

        System.out.println(e.getDescription());

    }

}
