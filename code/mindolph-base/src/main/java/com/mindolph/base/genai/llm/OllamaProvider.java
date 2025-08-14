package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents.Input;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;

import java.time.Duration;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7.3
 */
public class OllamaProvider extends BaseLangChainLlmProvider {

    private final String baseUrl;

    public OllamaProvider(String baseUrl, String aiModel, boolean useProxy) {
        super(null, aiModel, useProxy);
        this.baseUrl = baseUrl;
    }

    @Override
    protected ChatModel buildAI(Input input) {
        OllamaChatModel.OllamaChatModelBuilder builder = new OllamaChatModel.OllamaChatModelBuilder()
                .baseUrl(this.baseUrl)
                .modelName(determineModel(input))
                .maxRetries(1)
                .timeout(Duration.ofSeconds(super.timeout))
                .temperature((double) input.temperature());
        return builder.build();
    }

    @Override
    protected StreamingChatModel buildStreamingAI(Input input) {
        OllamaStreamingChatModel.OllamaStreamingChatModelBuilder builder = new OllamaStreamingChatModel.OllamaStreamingChatModelBuilder()
                .baseUrl(this.baseUrl)
                .modelName(determineModel(input))
                .timeout(Duration.ofSeconds(super.timeout))
                .temperature((double) input.temperature());
        // TODO support maxTokens if Ollama support it.
        return builder.build();
    }

    @Override
    protected String extractErrorMessage(Throwable throwable) {
        return throwable.getLocalizedMessage();
    }
}
