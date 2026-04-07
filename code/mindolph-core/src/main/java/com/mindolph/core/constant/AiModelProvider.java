package com.mindolph.core.constant;

import org.apache.commons.lang3.Strings;

/**
 * @author mindolph.com@gmail.com
 */
public enum AiModelProvider {

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

    public static boolean isRagSupportedByLangchain(String providerId) {
        // NOTE: some providers are supported by LangChain4j but not with streaming like ChatGLM, they are excluded.
        // some providers support like HuggingFace don't include some features like streaming, they are also excluded.
        // the custom openai like apis should be able to do rag, they are included.
        return !Strings.CS.equalsAny(providerId, DEEP_SEEK.name, CHAT_GLM.name, HUGGING_FACE.name);
    }

    AiModelProvider(String name, ProviderType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @param name
     * @return
     * @deprecated
     */
    @Deprecated
    public static AiModelProvider fromName(String name) {
        for (AiModelProvider vendor : AiModelProvider.values()) {
            if (vendor.name.equals(name)) {
                return vendor;
            }
        }
        return null;
    }

    /**
     * The workaround for get the KEY instead of the display name.
     * This method should be deprecated later.
     *
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
        // public apis
        PUBLIC,
        // private apis like ollma
        PRIVATE,
        // internal
        INTERNAL,
        // custom provider(public or private)
        CUSTOM
    }
}
