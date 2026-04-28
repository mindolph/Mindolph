package com.mindolph.base.genai.llm;

import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import org.swiftboot.util.I18nHelper;

import static com.mindolph.core.constant.AiModelProvider.*;
import static com.mindolph.core.constant.AiModelProvider.MOONSHOT;

/**
 * @since 1.13.0
 */
public class LlmProviderFactory {

    private static final I18nHelper i18n = I18nHelper.getInstance();

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
                llmProvider = new QwenProvider2(providerMeta, modelMeta);
            }
            else if (OLLAMA.name().equals(providerName)) {
                llmProvider = new OllamaProvider(providerMeta, modelMeta);
            }
            else if (HUGGING_FACE.name().equals(providerName)) {
                llmProvider = new HuggingFaceProvider(providerMeta, modelMeta);
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
                llmProvider = new CustomModelProvider(providerMeta, modelMeta);
//                throw new RuntimeException("No llm provider setup: %s".formatted(providerName));
            }
            return llmProvider;
        }
        else {
            throw new RuntimeException(i18n.get("llm.provider.setup.not.completed", providerName));
        }
    }
}
