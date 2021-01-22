package io.rocktest.integration;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.assertNull;

@SpringBootTest
public class HttpMock extends RockTest {
    private static Logger LOG = LoggerFactory.getLogger(HttpMock.class);

    @Test
    public void simpleMock() throws IOException, InterruptedException {
        String ret=run("/scen/simplehttpmock.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void mockCall() throws IOException, InterruptedException {
        String ret=run("/scen/http.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void mockDB() throws IOException, InterruptedException {
        String ret=run("/scen/httpmockdb.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void mockCallCompact() throws IOException, InterruptedException {
        String ret=run("/scen/httpcompact.yaml");
        assertNull("Scenario should succeed",ret);
    }


}
