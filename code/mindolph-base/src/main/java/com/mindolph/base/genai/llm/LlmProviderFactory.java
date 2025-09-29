package com.mindolph.base.genai.llm;

import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;

import static com.mindolph.core.constant.GenAiModelProvider.*;
import static com.mindolph.core.constant.GenAiModelProvider.MOONSHOT;

/**
 * @since 1.13.0
 */
public class LlmProviderFactory {
    
    public static LlmProvider create(String providerName, ProviderMeta providerMeta, ModelMeta modelMeta) {
        if (providerMeta != null) {
            LlmProvider llmProvider = null;
            if (OPEN_AI.name().equals(providerName)) {
                llmProvider = new OpenAiProvider(providerMeta, modelMeta);
            }
            else if (GEMINI.name().equals(providerName)) {
                llmProvider = new GeminiProvider(providerMeta, modelMeta);
            }
            else if (ALI_Q_WEN.name().equals(providerName)) {
                llmProvider = new QwenProvider(providerMeta, modelMeta);
            }
            else if (OLLAMA.name().equals(providerName)) {
                llmProvider = new OllamaProvider(providerMeta, modelMeta);
            }
            else if (HUGGING_FACE.name().equals(providerName)) {
                llmProvider = new HuggingFaceProvider2(providerMeta, modelMeta);
            }
            else if (CHAT_GLM.name().equals(providerName)) {
                llmProvider = new ChatGlmProvider(providerMeta, modelMeta);
            }
            else if (DEEP_SEEK.name().equals(providerName)) {
                llmProvider = new DeepSeekProvider(providerMeta, modelMeta);
            }
            else if (MOONSHOT.name().equals(providerName)) {
                llmProvider = new MoonshotProvider(providerMeta, modelMeta);
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
