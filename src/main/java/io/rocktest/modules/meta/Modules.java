package io.rocktest.modules.meta;

import java.util.HashMap;

/**
 * Static functions to manage modules
 */
public class Modules {

    private static HashMap<String,ModuleInfo> info = new HashMap<>();

    /**
     * Add a new module in the system (at startup)
     * @param word
     * @param moduleInfo
     */
    public static void addModule(String word, ModuleInfo moduleInfo) {
        info.put(word,moduleInfo);
    }

    /**
     * Gets the module information from the keyword
     * @param word
     * @return The module information
     */
    public static ModuleInfo getModule(String word) {
        return info.get(word);
    }

}
