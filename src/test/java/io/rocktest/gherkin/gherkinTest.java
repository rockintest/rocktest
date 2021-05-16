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
    public void scen() throws IOException, InterruptedException {
        String ret=run("/scen/gherkin/scen.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void rule() throws IOException, InterruptedException {
        String ret=run("/scen/gherkin/rule.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void minimal() throws IOException, InterruptedException {
        String ret=run("/scen/gherkin/minimal.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void expr() throws IOException, InterruptedException {
        String ret=run("/scen/gherkin/expr.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void backgound() throws IOException, InterruptedException {
        String ret=run("/scen/gherkin/background.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void backgoundRule() throws IOException, InterruptedException {
        String ret=run("/scen/gherkin/backgroundRule.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void outline() throws IOException, InterruptedException {
        String ret=run("/scen/gherkin/outline.yaml");
        assertNull("Scenario should succeed",ret);
    }

    @Test
    public void french() throws IOException, InterruptedException {
        String ret=run("/scen/gherkin/french.yaml");
        assertNull("Scenario should succeed",ret);
    }

}
