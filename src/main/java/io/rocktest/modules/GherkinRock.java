package io.rocktest.modules;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.gherkin.Gherkin;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow.TableCell;
import io.cucumber.messages.IdGenerator;

import io.rocktest.RockException;
import io.rocktest.modules.annotations.NoExpand;
import io.rocktest.modules.annotations.RockWord;
import org.apache.commons.io.IOUtils;
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

    private String featureFile = null;
    private String featureName = null;
    private Step step = null;

    HashMap<String,String> translations=new HashMap<String,String>();

    public GherkinRock() {
        Object mapper = new ObjectMapper(new JsonFactory());

        try {

            String json = new String(IOUtils.resourceToByteArray("/gherkin-languages.json"));
            Map<String,Object> langs = ((ObjectMapper) mapper).readValue(json, new TypeReference<Map<String,Object>>() {});

            for (String lang : langs.keySet()) {
                Map<String,Object> entry = (Map<String,Object>) langs.get(lang);
                for(String word: entry.keySet()) {
                    if(word.equals("name") || word.equals("native"))
                        continue;

                    ArrayList<String> tr = (ArrayList<String>)entry.get(word);
                    for(String translated:tr) {
                        translations.put(translated.toLowerCase().trim(),word);
                    }
                }
            }

        } catch (IOException e) {
            LOG.warn("Unable to find gherkin-languages.json in classpath. Localization will not be available");
        }


    }

    private HashMap<String,List<Map>> getDestMap(String type) {
        HashMap<String,List<Map>> dest = gherkinSteps.get(type);
        if(dest==null) {
            dest=new HashMap<>();
            gherkinSteps.put(type,dest);
        }
        return dest;
    }

    private void load(Map<String, Object> params,String type) {

        List<Map> l=getArrayParam(params,type,null);

        if(l==null) {
            return;
        }

        for (Map m:l) {
            String expr=getStringParam(m,"text");
            LOG.debug("text={}",expr);
            List<Map> steps=getArrayParam(m,"steps",null);
            if(steps!=null) {

                if(type.equals("expr")) {
                    Object keyword = m.get("keyword");
                    if(keyword != null) {

                        if(keyword instanceof List) {
                            for(String k:(List<String>)keyword) {
                                getDestMap(k).put(expr,steps);
                            }
                        } else if(keyword instanceof String) {
                            getDestMap((String)keyword).put(expr,steps);
                        } else {
                            throw new RockException("keyword attribute must me a list or a string, not a "+keyword.getClass().getTypeName());
                        }

                    } else {
                        // If no keyword is specified, it works for all of them
                        getDestMap("given").put(expr,steps);
                        getDestMap("when").put(expr,steps);
                        getDestMap("then").put(expr,steps);
                    }
                } else {
                    getDestMap(type).put(expr,steps);
                }

            }
        }
    }


    private void processSteps(List<Step> l) throws IOException, InterruptedException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        processSteps(l,null,null);
    }

    private String replaceText(String text,List<Feature.TableRow.TableCell> header,List<Feature.TableRow.TableCell> values) {
        if(header==null || values==null) {
            return text;
        }

        if(header.size() != values.size()) {
            throw new RockException("Size mismatch in examples. Number of header does not match values");
        }

        String ret=text;

        for(int i=0;i<header.size();i++) {
            TableCell h=header.get(i);
            TableCell v=values.get(i);
            ret=ret.replaceAll("<"+h.getValue()+">",v.getValue());
        }

        return ret;
    }


    private void processSteps(List<Step> l,List<Feature.TableRow.TableCell> header,List<Feature.TableRow.TableCell> values) throws NoSuchMethodException, InterruptedException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {

        if(l==null) {
            return;
        }

        String currentKeyword="given";

        for (Step step:l) {

            String text=replaceText(step.getText(),header,values);

            LOG.info("Step: {} {}", step.getKeyword(), text);

            // Identify the keyword
            String keyWord = translate(step.getKeyword());

            switch (keyWord) {
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

            boolean found=false;

            for (String regex : steps.keySet()) {
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(text);
                if (m.matches()) {

                    if(found) {
                        LOG.warn("Multiple match for step {} {}",step.getKeyword(),text);
                    }

                    found=true;
                    LOG.debug("Match => {}", regex);

                    // Clean previously defined variables starting by "gherkin."
                    ArrayList<String> tokill = new ArrayList<>();
                    scenario.getLocalContext().keySet().forEach((k) -> {
                        if (k.startsWith("gherkin.") && !k.equals("gherkin.scenario") ) tokill.add(k);
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
                    scenario.getLocalContext().put("gherkin.step", text);
                    scenario.getLocalContext().put("gherkin.keyword", step.getKeyword());
                    scenario.getLocalContext().put("gherkin.line", step.getLocation().getLine());
                    scenario.getLocalContext().put("gherkin.column", step.getLocation().getColumn());

                    scenario.setSubContext(step.getKeyword()+text);
                    scenario.run(steps.get(regex));
                    scenario.setSubContext(null);
                }
            }

            if(!found) {
                LOG.warn("No match for step {} {}",step.getKeyword(),text);
            }

        }

    }


    private String translate(String w) {
        String w2 = w.toLowerCase().trim();
        String ret=translations.get(w2);
        return (ret==null?w2:ret);
    }


    private void processScenario(Feature.Scenario gherkinScenario,List<Step> backgroundFeature,List<Step> backgroundRule) throws NoSuchMethodException, InterruptedException, IOException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {

        LOG.info("Run scenario: {}", gherkinScenario.getName());

        scenario.getLocalContext().put("gherkin.scenario", gherkinScenario.getName());

        String tr = translate(gherkinScenario.getKeyword());

        if(tr.equals("scenario outline") || tr.equals("scenarioOutline")) {

            List<Feature.Scenario.Examples> examples=gherkinScenario.getExamplesList();

            if(examples==null || examples.size()!= 1 ) {
                throw new RockException("Scenario outline must have 1 examples");
            }

            List<TableCell> header = examples.get(0).getTableHeader().getCellsList();

            for(Feature.TableRow row: examples.get(0).getTableBodyList()) {
                List<TableCell> values = row.getCellsList();
                if(backgroundFeature!=null) {
                    LOG.info("Feature background");
                    processSteps(backgroundFeature);

                }

                if(backgroundRule!=null) {
                    LOG.info("Rule background");
                    processSteps(backgroundRule);
                }

                processSteps(gherkinScenario.getStepsList(),header,values);
            }

        } else {

            if(backgroundFeature!=null) {
                LOG.info("Feature background");
                processSteps(backgroundFeature);

            }

            if(backgroundRule!=null) {
                LOG.info("Rule background");
                processSteps(backgroundRule);
            }

            processSteps(gherkinScenario.getStepsList());

        }

        scenario.getLocalContext().put("gherkin.scenario", null);

    }

    
    @RockWord(keyword="gherkin")
    @NoExpand
    public Map<String, Object> gherkin(Map<String, Object> params) throws IOException, NoSuchMethodException, InterruptedException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {

        String dir= scenario.getDir();
        featureFile = getStringParam(params,"rock.value");

        load(params,"given");
        load(params,"when");
        load(params,"then");
        load(params,"expr");

        String gherkin=new String(Files.readAllBytes(Paths.get(dir+"/"+featureFile)));

        Messages.Envelope envelope = makeSourceEnvelope(gherkin, featureFile);
        List<Messages.Envelope> envelopes = Gherkin.fromSources(singletonList(envelope), false, true, false, idGenerator).collect(Collectors.toList());

        GherkinDocument gherkinDocument = envelopes.get(0).getGherkinDocument();
        Feature feature = gherkinDocument.getFeature();
        featureName = feature.getName();

        int nb = feature.getChildrenCount();

        Feature.Scenario gherkinScenario=null;
        Step step=null;

        List<Step> backgroundFeature=null;
        List<Step> backgroundRule=null;

        try {

            for (int i = 0; i < nb; i++) {

                FeatureChild ch = feature.getChildren(i);

                if(ch.hasBackground()) {
                    backgroundFeature = ch.getBackground().getStepsList();
                    continue;
                }

                if(ch.hasRule()) {
                    // The feature has rules
                    int nbScen = ch.getRule().getChildrenCount();

                    LOG.info("Rule: {}", ch.getRule().getName());

                    backgroundRule = null;

                    for(int j=0; j < nbScen; j++) {

                        if(ch.getRule().getChildren(j).hasBackground()) {
                            backgroundRule = ch.getRule().getChildren(j).getBackground().getStepsList();
                            continue;
                        }

                        FeatureChild.RuleChild chScen = ch.getRule().getChildren(j);
                        gherkinScenario = chScen.getScenario();
                        processScenario(gherkinScenario,backgroundFeature,backgroundRule);

                    }

                } else {

                    // The feature has directly scenarios
                    gherkinScenario = ch.getScenario();
                    processScenario(gherkinScenario,backgroundFeature,null);

                }

            }

        } catch(Exception e) {

            LOG.error("Exception in Gherkin step",e);

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
