package com.mindolph.core.constant;

/**
 * @author mindolph.com@gmail.com
 */
public enum GenAiModelProvider {

    OPEN_AI("OpenAI", ProviderType.API),
    GEMINI("Gemini", ProviderType.API),
    ALI_Q_WEN("Qwen", ProviderType.API),
    OLLAMA("Ollama", ProviderType.PRIVATE);

    private final String name;
    private final ProviderType type;

    GenAiModelProvider(String name, ProviderType type) {
        this.name = name;
        this.type = type;
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

    public ProviderType getType() {
        return type;
    }

    public enum ProviderType {
        API, PRIVATE
    }
}
