package io.rocktest.integration;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.*;

@SpringBootTest
public class Compact extends RockTest {
    private static Logger LOG = LoggerFactory.getLogger(Compact.class);

    @Test
    public void compact() throws IOException, InterruptedException {
        String ret=run("/scen/compactstep.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void compactBad1() throws IOException, InterruptedException {
        String ret=run("/scen/compactbad1.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void compactBad2() throws IOException, InterruptedException {
        String ret=run("/scen/compactbad2.yaml");
        assertNotNull("Scenario should fail",ret);
    }

    @Test
    public void compacter() throws IOException, InterruptedException {
        String ret=run("/scen/compacterstep.yaml");
        assertNull("Scenario should succeed",ret);
    }

}
