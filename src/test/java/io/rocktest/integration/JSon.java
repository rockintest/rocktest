package io.rocktest.integration;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.*;

@SpringBootTest
public class JSon extends RockTest {

    private static Logger LOG = LoggerFactory.getLogger(JSon.class);

    @Test
    public void parse() throws IOException, InterruptedException {
        String ret=run("/scen/json.yaml");
        assertNull("Scenario should succeed",ret);
    }


    @Test
    public void check() throws IOException, InterruptedException {
        String ret=run("/scen/jsoncheck.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void checkFailEquals() throws IOException, InterruptedException {
        String ret=run("/scen/jsoncheck-failequals.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void checkFailMatch() throws IOException, InterruptedException {
        String ret=run("/scen/jsoncheck-failmatch.yaml");
        assertNotNull("Scenario should fail",ret);
    }

}
