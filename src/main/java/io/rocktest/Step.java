package io.rocktest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@NoArgsConstructor
public class Step {

    private Map origin;

    private String type;
    private String value;
    private List<Object> expect;
    private List<Object> steps;
    private List<String> values;
    private String desc;
    private String body;
    private String name;
    private Map params;

    private List valid = Arrays.asList("step","type","value","expect","steps","values","desc","body","name","params");

    private String asString(Object o) {
        if(o==null)
            return null;
        return String.valueOf(o);
    }

    public Step(Map<String,Object> m) {

        origin = m;

        m.keySet().forEach(str -> {
            if(! valid.contains(str)) {
                throw new RockException("Property \""+str+"\" for step invalid. Expected "+valid.toString());
            }
        });

        if(m.get("type")!=null) {
            type=asString(m.get("type"));
        }

        value=asString(m.get("value"));
        name=asString(m.get("name"));

        if(m.get("step")!=null) {

            Pattern p = Pattern.compile("^ *([^ ]*) (.*)$");
            Matcher matcher = p.matcher(asString(m.get("step")));

            if (!matcher.matches()) {
                type=asString(m.get("step"));
            } else {
                type=matcher.group(1);

                if(type.trim().equals("function")) {
                    name =  matcher.group(2);
                } else {
                    value = matcher.group(2);
                }
            }
        }

        expect=(List)m.get("expect");
        steps=(List)m.get("steps");
        values=(List)m.get("values");
        desc=asString(m.get("desc"));
        body=asString(m.get("body"));
        params=(Map)m.get("params");
    }


    public String toYaml() {
        return toYaml(null);
    }


    public String toYaml(String node) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        if(node!=null) {
            HashMap<String,Map> tmp=new HashMap<>();
            tmp.put(node,origin);
            return yaml.dump(tmp);
        }

        return yaml.dump(origin);
    }

}
