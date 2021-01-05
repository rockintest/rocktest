package io.rocktest.integration;


import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SpringBootTest
public class Lib extends RockTest {
    private static Logger LOG = LoggerFactory.getLogger(Lib.class);

    @Test
    public void lib1() throws IOException, InterruptedException {
        String ret=run("/scen/libtest.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void context1() throws IOException, InterruptedException {
        String ret=run("/scen/context.yaml");
        assertNull("Scenario should succeed",ret);
    }

}
