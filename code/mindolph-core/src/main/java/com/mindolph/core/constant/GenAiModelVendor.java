package com.mindolph.core.constant;

/**
 * @author mindolph.com@gmail.com
 */
public enum GenAiModelVendor {

    OPEN_AI("OpenAI");

    private final String name;

    GenAiModelVendor(String name) {
        this.name = name;
    }

    public static GenAiModelVendor fromName(String name) {
        for (GenAiModelVendor vendor : GenAiModelVendor.values()) {
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
