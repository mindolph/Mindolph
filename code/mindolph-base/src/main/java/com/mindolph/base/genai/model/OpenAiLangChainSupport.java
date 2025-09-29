package com.mindolph.base.genai.model;

import com.google.gson.JsonParser;
import com.mindolph.base.genai.llm.OkHttpClientAdapter;
import com.mindolph.base.genai.llm.OkHttpClientBuilder;
import com.mindolph.core.config.ProxyMeta;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.core.util.Tuple2;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder;

/**
 *
 */
public interface OpenAiLangChainSupport extends LangChainSupport {

    @Override
    default Tuple2<ChatModel, OkHttpClientAdapter> buildChatModel(ProviderMeta providerMeta, ModelMeta modelMeta, double temperature, ProxyMeta proxyMeta, boolean proxyEnabled) {
        String modelName = modelMeta.getName();
        logParameters(GenAiModelProvider.OPEN_AI.name(), modelName, proxyEnabled, proxyMeta);
        OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(providerMeta.apiKey())
                .modelName(modelName)
                .maxRetries(1)
                .timeout(defaultTimeout())
                .temperature(temperature);
        if (modelMeta.maxTokens() != 0) builder.maxTokens(modelMeta.maxTokens());
        OkHttpClientBuilder httpClientBuilder = createHttpClientBuilder(providerMeta, proxyEnabled && providerMeta.useProxy(), proxyMeta);
        builder.httpClientBuilder(httpClientBuilder);
        return new Tuple2<>(builder.build(), httpClientBuilder.getOkHttpClientAdapter());
    }

    @Override
    default Tuple2<StreamingChatModel, OkHttpClientAdapter> buildStreamingChatModel(ProviderMeta providerMeta, ModelMeta modelMeta, double temperature, ProxyMeta proxyMeta, boolean proxyEnabled) {
        String modelName = modelMeta.getName();
        logParameters(GenAiModelProvider.OPEN_AI.name(), modelName, proxyEnabled, proxyMeta);
        OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .apiKey(providerMeta.apiKey())
                .modelName(modelName)
                .timeout(defaultTimeout())
                .temperature(temperature);
        if (modelMeta.maxTokens() != 0) builder.maxTokens(modelMeta.maxTokens());
        OkHttpClientBuilder httpClientBuilder = createHttpClientBuilder(providerMeta, proxyEnabled && providerMeta.useProxy(), proxyMeta);
        builder.httpClientBuilder(httpClientBuilder);
        return new Tuple2<>(builder.build(), httpClientBuilder.getOkHttpClientAdapter());
    }

    @Override
    default String extractErrorMessageFromLLM(String llmMsg) {
        return JsonParser.parseString(llmMsg).getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString();
    }
}
