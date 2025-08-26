package com.mindolph.core.constant;

/**
 * @author mindolph.com@gmail.com
 */
public enum GenAiModelProvider {

    OPEN_AI("OpenAI", ProviderType.PUBLIC),
    GEMINI("Gemini", ProviderType.PUBLIC),
    ALI_Q_WEN("Qwen", ProviderType.PUBLIC),
    OLLAMA("Ollama", ProviderType.PRIVATE),
    HUGGING_FACE("HuggingFace", ProviderType.PUBLIC),
    CHAT_GLM("ChatGLM", ProviderType.PUBLIC),
    DEEP_SEEK("DeepSeek", ProviderType.PUBLIC),
    MOONSHOT("Moonshot", ProviderType.PUBLIC),
    INTERNAL("Internal", ProviderType.INTERNAL);


    private final String name;
    private final ProviderType type;

    GenAiModelProvider(String name, ProviderType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @deprecated
     * @param name
     * @return
     */
    @Deprecated
    public static GenAiModelProvider fromName(String name) {
        for (GenAiModelProvider vendor : GenAiModelProvider.values()) {
            if (vendor.name.equals(name)) {
                return vendor;
            }
        }
        return null;
    }

    /**
     * The workaround for get the KEY instead of the display name.
     * This method should be deprecated later.
     * @return
     * @deprecated use name()
     */
    public String getName() {
        return name();
    }

    public String getDisplayName() {
        return name;
    }

    public ProviderType getType() {
        return type;
    }

    public enum ProviderType {
        PUBLIC, PRIVATE, INTERNAL
    }
}
