package io.rocktest.integration;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

@SpringBootTest
public class Syntax extends RockTest {
    private static Logger LOG = LoggerFactory.getLogger(Syntax.class);

    @Test
    public void badyaml() throws IOException, InterruptedException {
        String ret=run("/scen/badyaml.yaml");
        assertNotNull("Scenario should fail",ret);
    }


}
