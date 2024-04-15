package com.mindolph.base.genai.llm;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7.3
 */
public class OllamaProvider extends BaseLangchainLlmProvider {

    private final String baseUrl;

    public OllamaProvider(String baseUrl, String aiModel) {
        super(null, aiModel);
        this.baseUrl = baseUrl;
    }

    @Override
    protected ChatLanguageModel buildAI(float temperature) {
        OllamaChatModel.OllamaChatModelBuilder builder = new OllamaChatModel.OllamaChatModelBuilder()
                .baseUrl(this.baseUrl)
                .modelName(super.aiModel)
                .maxRetries(1)
                .timeout(Duration.ofSeconds(super.timeout))
                .temperature((double) temperature);
        return builder.build();
    }
}
