package io.rocktest.integration;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.assertNull;

@SpringBootTest
public class Date extends RockTest {
    private static Logger LOG = LoggerFactory.getLogger(Date.class);

    @Test
    public void dateSimple() throws IOException, InterruptedException {
        String ret=run("/scen/dateSimple.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void date() throws IOException, InterruptedException {
        String ret=run("/scen/date.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void time() throws IOException, InterruptedException {
        String ret=run("/scen/time.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void minus() throws IOException, InterruptedException {
        String ret=run("/scen/dateMinus.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void plus() throws IOException, InterruptedException {
        String ret=run("/scen/datePlus.yaml");
        assertNull("Scenario should succeed",ret);
    }

}
