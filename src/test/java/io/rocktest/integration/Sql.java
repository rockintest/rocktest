package io.rocktest.integration;

import io.rocktest.RockTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.assertNull;

@SpringBootTest
public class Sql extends RockTest {

    private static Logger LOG = LoggerFactory.getLogger(Sql.class);

    @Test
    public void oneConnection() throws IOException, InterruptedException {
        String ret=run("sql.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void multipleConnections() throws IOException, InterruptedException {
        String ret=run("sqlMulti.yaml");
        assertNull("Scenario should succeed",ret);
    }

}
