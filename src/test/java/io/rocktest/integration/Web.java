package io.rocktest.integration;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.*;

@SpringBootTest
public class Web extends RockTest {

    private static Logger LOG = LoggerFactory.getLogger(Web.class);

    @Test
    public void get() throws IOException, InterruptedException {
        String ret=run("/scen/web.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void error() throws IOException, InterruptedException {
        String ret=run("/scen/webError.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void angular() throws IOException, InterruptedException {
        String ret=run("/scen/angular.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void css() throws IOException, InterruptedException {
        String ret=run("/scen/css.yaml");
        assertNull("Scenario should succeed",ret);
    }

}
