package com.mindolph.base.genai.llm;

import com.mindolph.core.llm.ProviderMeta;

import static com.mindolph.core.constant.GenAiModelProvider.*;
import static com.mindolph.core.constant.GenAiModelProvider.MOONSHOT;

/**
 * @since unknown
 */
public class LlmProviderFactory {
    
    public static LlmProvider create(String providerName, ProviderMeta props) {
        if (props != null) {
            LlmProvider llmProvider = null;
            if (OPEN_AI.name().equals(providerName)) {
                llmProvider = new OpenAiProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (GEMINI.name().equals(providerName)) {
                llmProvider = new GeminiProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (ALI_Q_WEN.name().equals(providerName)) {
                llmProvider = new QwenProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (OLLAMA.name().equals(providerName)) {
                llmProvider = new OllamaProvider(props.baseUrl(), props.aiModel(), props.useProxy());
            }
            else if (HUGGING_FACE.name().equals(providerName)) {
                llmProvider = new HuggingFaceProvider2(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (CHAT_GLM.name().equals(providerName)) {
                llmProvider = new ChatGlmProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (DEEP_SEEK.name().equals(providerName)) {
                llmProvider = new DeepSeekProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (MOONSHOT.name().equals(providerName)) {
                llmProvider = new MoonshotProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else {
                throw new RuntimeException("No llm provider setup: %s".formatted(providerName));
            }
            return llmProvider;
        }
        else {
            throw new RuntimeException("LLM provider setup is not completed: %s".formatted(providerName));
        }
    }
}
