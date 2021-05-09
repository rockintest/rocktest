package io.rocktest.gherkin;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.assertNull;

@SpringBootTest
public class gherkinTest extends RockTest {

    private static Logger LOG = LoggerFactory.getLogger(gherkinTest.class);

    @Test
    public void newRocker() throws IOException, InterruptedException {
        String ret=run("/scen/gherkin/newrocker.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void minimal() throws IOException, InterruptedException {
        String ret=run("/scen/gherkin/minimal.yaml");
        assertNull("Scenario should succeed",ret);
    }

}
