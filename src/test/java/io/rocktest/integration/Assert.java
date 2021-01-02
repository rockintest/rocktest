package io.rocktest.integration;


import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.*;

@SpringBootTest
public class Assert extends RockTest {
    private static Logger LOG = LoggerFactory.getLogger(Assert.class);

    @Test
    public void assert1() throws IOException, InterruptedException {
        String ret=run("/scen/assert.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void assertFail1() throws IOException, InterruptedException {
        String ret=run("/scen/assert-fail1.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void assertFail2() throws IOException, InterruptedException {
        String ret=run("/scen/assert-fail2.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void assertSyntax1() throws IOException, InterruptedException {
        String ret=run("/scen/assert-syntax1.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void assertSyntax2() throws IOException, InterruptedException {
        String ret=run("/scen/assert-syntax2.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void assertSyntax3() throws IOException, InterruptedException {
        String ret=run("/scen/assert-syntax3.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void assertModule() throws IOException, InterruptedException {
        String ret=run("/scen/assert-module.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void assertModuleFailed() throws IOException, InterruptedException {
        String ret=run("/scen/assert-module-fail.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void assertRegex() throws IOException, InterruptedException {
        String ret=run("/scen/assert-regex.yaml");
        assertNull("Scenario should succeed",ret);
    }


}
