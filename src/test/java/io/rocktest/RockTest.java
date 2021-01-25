package io.rocktest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class RockTest {

    @Autowired
    Environment env;

    protected Scenario scenario;

    @Before
    public void initScenario() {
        scenario=new Scenario();
        Map<String, Map<String, Object>> context=new HashMap<>();
        scenario.setContext(context);
        ArrayList<String> stack=new ArrayList<>();
        stack.add("junit");
        scenario.setStack(stack);
        scenario.initLocalContext();
        // HashMap for each instance of modules
        scenario.setModuleInstances(new HashMap<>());
        scenario.setEnv(env);

    }


    public String run(String file) throws IOException, InterruptedException {

        URL f = this.getClass().getResource(file);
        if(f==null) {
            throw new RuntimeException("Scenario not found in classpath : "+file);
        }

        String filePath = f.getPath();

        File parent =new File(filePath).getParentFile();
        String dir=".";

        if(parent != null) {
            dir=parent.getAbsolutePath();
        }

        ArrayList<String> stack=new ArrayList<>();
        stack.add(new File(filePath).getName().replace(".yaml",""));
        Map<String,Object> glob=new HashMap<>();
        return scenario.main(filePath,dir,new HashMap<String, Map<String,Object>>(),stack,glob);
    }


}
