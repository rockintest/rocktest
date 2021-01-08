package io.rocktest.integration;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.*;

@SpringBootTest
public class Module extends RockTest {
    private static Logger LOG = LoggerFactory.getLogger(Module.class);

    @Test
    public void noexist() throws IOException, InterruptedException {
        String ret=run("/scen/noexist.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void modulenoexist() throws IOException, InterruptedException {
        String ret=run("/scen/modulenoexist.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void modulenoexist2() throws IOException, InterruptedException {
        String ret=run("/scen/modulenoexist2.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void methodnoexist() throws IOException, InterruptedException {
        String ret=run("/scen/methodnoexist.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void missingParam() throws IOException, InterruptedException {
        String ret=run("/scen/libchecktest.yaml");
        assertNotNull("Scenario should fail",ret);
    }


}
