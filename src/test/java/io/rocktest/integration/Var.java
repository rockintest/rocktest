package io.rocktest.integration;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.assertNull;

@SpringBootTest
public class Var extends RockTest {

    private static Logger LOG = LoggerFactory.getLogger(Var.class);

    @Test
    public void var1() throws IOException, InterruptedException {
        String ret=run("/scen/vartest.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void var2() throws IOException, InterruptedException {
        String ret=run("/scen/vartest2.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void varSubst() throws IOException, InterruptedException {
        String ret=run("/scen/varSubstTest.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void varBuiltin() throws IOException, InterruptedException {
        String ret=run("/scen/varBuiltin.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void varConcat() throws IOException, InterruptedException {
        String ret=run("/scen/varConcat.yaml");
        assertNull("Scenario should succeed",ret);
    }

}
