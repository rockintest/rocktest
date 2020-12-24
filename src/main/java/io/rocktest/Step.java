package io.rocktest;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class Step {

    private String type;
    private String value;
    private List<Object> expect;
    private List<Object> steps;
    private List<String> values;
    private String desc;
    private String body;
    private String name;
    private Map params;

    private List valid = Arrays.asList("type","value","expect","steps","values","desc","body","name","params");

    public Step(Map m) {

        m.keySet().forEach(o -> {
            if(! valid.contains(o)) {
                throw new RuntimeException("Property \""+o+"\" for step invalid. Expected "+valid.toString());
            }
        });

        type=(String)m.get("type");
        value=(String)m.get("value");
        expect=(List)m.get("expect");
        steps=(List)m.get("steps");
        values=(List)m.get("values");
        desc=(String)m.get("desc");
        body=(String)m.get("body");
        name=(String)m.get("name");
        params=(Map)m.get("params");
    }

}
