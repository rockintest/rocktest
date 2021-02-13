package io.rocktest.modules.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ModuleInfo {

    private String className;
    private Class classType;
    private String method;
    private String result;
    private String extension;
    private String[] params;

}
