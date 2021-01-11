package io.rocktest.integration;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.assertNull;

@SpringBootTest
public class Compact extends RockTest {
    private static Logger LOG = LoggerFactory.getLogger(Compact.class);

    @Test
    public void compact() throws IOException, InterruptedException {
        String ret=run("/scen/compactstep.yaml");
        assertNull("Scenario should succeed",ret);
    }

}
