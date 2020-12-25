package io.rocktest.integration;

import io.rocktest.Scenario;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class JSon {

    @Autowired
    Environment env;

    private static Logger LOG = LoggerFactory.getLogger(JSon.class);
    private Scenario scenario = new Scenario();

    public String run(String file) throws IOException, InterruptedException {
        String filePath = this.getClass().getResource("/scen/"+file).getPath();

        File parent =new File(filePath).getParentFile();
        String dir=".";

        if(parent != null) {
            dir=parent.getName();
        }

        ArrayList<String> stack=new ArrayList<>();
        stack.add(new File(filePath).getName().replace(".yaml",""));

        Map<String, List<Map<String,Object>>> functions = new HashMap<>();

        // HashMap for each instance of modules
        scenario.setModuleInstances(new HashMap<>());
        scenario.setEnv(env);

        return scenario.run(filePath,dir,new HashMap<String, Map<String,String>>(),stack,null);
    }

    @Test
    public void parse() throws IOException, InterruptedException {
        String ret=run("json.yaml");
        assertNull("Scenario should succeed",ret);
    }


}
