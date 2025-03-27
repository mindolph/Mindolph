package com.mindolph.base.genai.llm;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mindolph.base.genai.GenAiEvents.Input;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Proxy;
import java.time.Duration;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class OpenAiProvider extends BaseLangChainLlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);

    public OpenAiProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    protected ChatLanguageModel buildAI(Input input) {
        log.info("Build OpenAI with model %s and access %s".formatted(this.aiModel,
                super.proxyEnabled ? "with %s proxy %s".formatted(Proxy.Type.valueOf(super.proxyType.toUpperCase()), this.proxyUrl) : "without proxy"));
        OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(this.apiKey)
                .modelName(determineModel(input))
                .maxRetries(1)
                .timeout(Duration.ofSeconds(timeout))
                .temperature((double) input.temperature());
        if (input.maxTokens() != 0) builder.maxTokens(input.maxTokens());
        if (super.proxyEnabled && super.useProxy) {
            OkHttpClientBuilderAdapter okHttpClientBuilder = new OkHttpClientBuilderAdapter();
            okHttpClientBuilder.setProxyHost(super.proxyHost).setProxyPort(super.proxyPort).setProxyType(super.proxyType.toUpperCase());
            builder.httpClientBuilder(okHttpClientBuilder);
        }
        return builder.build();
    }

    @Override
    protected StreamingChatLanguageModel buildStreamingAI(Input input) {
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .apiKey(this.apiKey)
                .modelName(determineModel(input))
                .timeout(Duration.ofSeconds(timeout))
                .temperature((double) input.temperature());
        if (input.maxTokens() != 0) builder.maxTokens(input.maxTokens());
        if (super.proxyEnabled && super.useProxy) {
            OkHttpClientBuilderAdapter okHttpClientBuilder = new OkHttpClientBuilderAdapter();
            okHttpClientBuilder.setProxyHost(super.proxyHost).setProxyPort(super.proxyPort).setProxyType(super.proxyType.toUpperCase());
            builder.httpClientBuilder(okHttpClientBuilder);
        }
        return builder.build();
    }

    @Override
    protected String extractErrorMessage(Throwable throwable) {
        try {
            return JsonParser.parseString(throwable.getLocalizedMessage()).getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString();
        } catch (JsonSyntaxException e) {
            return throwable.getLocalizedMessage();
        }
    }
}
