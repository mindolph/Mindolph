package com.mindolph.base.genai.llm;

import com.mindolph.core.llm.ProviderProps;

import static com.mindolph.core.constant.GenAiModelProvider.*;
import static com.mindolph.core.constant.GenAiModelProvider.MOONSHOT;

/**
 * @since unknown
 */
public class LlmProviderFactory {
    
    public static LlmProvider create(String providerName, ProviderProps props) {
        if (props != null) {
            LlmProvider llmProvider = null;
            if (OPEN_AI.getName().equals(providerName)) {
                llmProvider = new OpenAiProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (GEMINI.getName().equals(providerName)) {
                llmProvider = new GeminiProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (ALI_Q_WEN.getName().equals(providerName)) {
                llmProvider = new QwenProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (OLLAMA.getName().equals(providerName)) {
                llmProvider = new OllamaProvider(props.baseUrl(), props.aiModel(), props.useProxy());
            }
            else if (HUGGING_FACE.getName().equals(providerName)) {
                llmProvider = new HuggingFaceProvider2(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (CHAT_GLM.getName().equals(providerName)) {
                llmProvider = new ChatGlmProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (DEEP_SEEK.getName().equals(providerName)) {
                llmProvider = new DeepSeekProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (MOONSHOT.getName().equals(providerName)) {
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
