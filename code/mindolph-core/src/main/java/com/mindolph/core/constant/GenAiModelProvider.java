package com.mindolph.core.constant;

/**
 * @author mindolph.com@gmail.com
 */
public enum GenAiModelProvider {

    OPEN_AI("OpenAI");

    private final String name;

    GenAiModelProvider(String name) {
        this.name = name;
    }

    public static GenAiModelProvider fromName(String name) {
        for (GenAiModelProvider vendor : GenAiModelProvider.values()) {
            if (vendor.name.equals(name)) {
                return vendor;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

}
