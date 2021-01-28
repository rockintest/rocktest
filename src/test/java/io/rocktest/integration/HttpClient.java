package io.rocktest.integration;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.assertNull;

@SpringBootTest
public class HttpClient extends RockTest {
    private static Logger LOG = LoggerFactory.getLogger(HttpClient.class);

    @Test
    public void json() throws IOException, InterruptedException {
        String ret=run("/scen/http-json.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void xml() throws IOException, InterruptedException {
        String ret=run("/scen/http-xml.yaml");
        assertNull("Scenario should succeed",ret);
    }


}
