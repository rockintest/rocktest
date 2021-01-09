package io.rocktest.integration;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.assertNull;

@SpringBootTest
public class Id extends RockTest {

    private static Logger LOG = LoggerFactory.getLogger(Id.class);

    @Test
    public void seq() throws IOException, InterruptedException {
        String ret=run("/scen/id.yaml");
        assertNull("Scenario should succeed",ret);
    }


}
