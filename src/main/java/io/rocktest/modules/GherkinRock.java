package io.rocktest.modules;

import io.cucumber.gherkin.Gherkin;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild;
import io.cucumber.messages.IdGenerator;

import io.rocktest.RockException;
import io.rocktest.modules.annotations.RockWord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.cucumber.gherkin.Gherkin.makeSourceEnvelope;
import static java.util.Collections.singletonList;

public class GherkinRock extends RockModule {

    private final IdGenerator idGenerator = new IdGenerator.Incrementing();

    private static Logger LOG = LoggerFactory.getLogger(GherkinRock.class);

    private HashMap<String,HashMap<String, List<Map>>> gherkinSteps=new HashMap<>();

    private void load(Map<String, Object> params,String type) {

        HashMap<String,List<Map>> dest = gherkinSteps.get(type);
        if(dest==null) {
            dest=new HashMap<>();
            gherkinSteps.put(type,dest);
        }

        List<Map> l=getArrayParam(params,type,null);

        if(l==null) {
            return;
        }

        for (Map m:l) {
            String expr=getStringParam(m,"expr");
            LOG.debug("expr={}",expr);
            List<Map> steps=getArrayParam(m,"steps",null);
            if(steps!=null) {
                dest.put(expr,steps);
            }
        }
        
    }
    
    @RockWord(keyword="gherkin")
    public Map<String, Object> gherkin(Map<String, Object> params) throws IOException, NoSuchMethodException, InterruptedException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {

        String dir= scenario.getDir();
        String featureFile = getStringParam(params,"feature");

        load(params,"given");
        load(params,"when");
        load(params,"then");

        String gherkin=new String(Files.readAllBytes(Paths.get(dir+"/"+featureFile)));

        Messages.Envelope envelope = makeSourceEnvelope(gherkin, featureFile);
        List<Messages.Envelope> envelopes = Gherkin.fromSources(singletonList(envelope), false, true, false, idGenerator).collect(Collectors.toList());

        GherkinDocument gherkinDocument = envelopes.get(0).getGherkinDocument();
        Feature feature = gherkinDocument.getFeature();
        String featureName = feature.getName();

        int nb = feature.getChildrenCount();

        String currentKeyword="given";

        Feature.Scenario gherkinScenario=null;
        Step step=null;

        try {

            for (int i = 0; i < nb; i++) {

                FeatureChild ch = feature.getChildren(i);
                gherkinScenario = ch.getScenario();

                LOG.info("Run scenario: {}", gherkinScenario.getName());

                int nbSteps = gherkinScenario.getStepsCount();

                for (int j = 0; j < nbSteps; j++) {
                    step = gherkinScenario.getSteps(j);

                    LOG.info("Step: {} {}", step.getKeyword(), step.getText());

                    // Identify the keyword
                    String keyWord = step.getKeyword();

                    switch (keyWord.toLowerCase().trim()) {
                        case "*":
                        case "but":
                        case "and":
                            keyWord = currentKeyword;
                            break;
                        case "example":
                            keyWord = "Scenario";
                            break;
                    }

                    currentKeyword = keyWord;

                    // Find a matching step
                    HashMap<String, List<Map>> steps = gherkinSteps.get(keyWord.trim().toLowerCase());

                    for (String regex : steps.keySet()) {
                        Pattern p = Pattern.compile(regex);
                        Matcher m = p.matcher(step.getText());
                        if (m.matches()) {
                            LOG.debug("Match => {}", regex);

                            // Clean previously defined variables starting by "gherkin."
                            ArrayList<String> tokill = new ArrayList<>();
                            scenario.getLocalContext().keySet().forEach((k) -> {
                                if (k.startsWith("gherkin.")) tokill.add(k);
                            });
                            for (String s : tokill) {
                                scenario.getLocalContext().remove(s);
                            }

                            for (int k = 1; k <= m.groupCount(); k++) {
                                String gr = m.group(k);
                                if (gr != null)
                                    scenario.getLocalContext().put("gherkin." + k, gr);
                            }

                            scenario.getLocalContext().put("gherkin.feature", featureName);
                            scenario.getLocalContext().put("gherkin.scenario", gherkinScenario.getName());
                            scenario.getLocalContext().put("gherkin.step", step.getText());
                            scenario.getLocalContext().put("gherkin.keyword", step.getKeyword());
                            scenario.getLocalContext().put("gherkin.line", step.getLocation().getLine());
                            scenario.getLocalContext().put("gherkin.column", step.getLocation().getColumn());

                            scenario.setSubContext(step.getKeyword()+step.getText());
                            scenario.run(steps.get(regex));
                            scenario.setSubContext(null);
                        }
                    }

                }

            }

        } catch(Exception e) {

            String gherkinMsg="Error in Gherkin module.\ngherkinStep:\n  feature: "+featureName+"\n";

            if(gherkinScenario!=null) {
                gherkinMsg+="  scenario: "+gherkinScenario.getName()+"\n";

                if(step != null) {
                    gherkinMsg+="  step: "+step.getKeyword()+" "+step.getText()+"\n";
                    gherkinMsg+="  location:\n    file:"+featureFile+"\n    line:"+step.getLocation().getLine();
                }

            }

            throw new RockException(gherkinMsg,e);

        }

        return null;
    }

}
