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

    private String asString(Object o) {
        if(o==null)
            return null;
        return String.valueOf(o);
    }

    public Step(Map m) {

        m.keySet().forEach(o -> {
            if(! valid.contains(o)) {
                throw new RuntimeException("Property \""+o+"\" for step invalid. Expected "+valid.toString());
            }
        });

        type=asString(m.get("type"));
        value=asString(m.get("value"));
        expect=(List)m.get("expect");
        steps=(List)m.get("steps");
        values=(List)m.get("values");
        desc=asString(m.get("desc"));
        body=asString(m.get("body"));
        name=asString(m.get("name"));
        params=(Map)m.get("params");
    }

}
