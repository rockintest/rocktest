package io.rocktest;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class Step {

    private String type;
    private String value;
    private List<Object> expect;
    private String[] values;
    private String desc;
    private String body;
    Map params;

}
