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
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel.OllamaStreamingChatModelBuilder;

import java.net.Proxy;

/**
 *
 */
public interface OllamaLangChainSupport extends LangChainSupport {

    @Override
    default Tuple2<ChatModel, OkHttpClientAdapter> buildChatModel(ProviderMeta providerMeta, ModelMeta modelMeta, double temperature, ProxyMeta proxyMeta, boolean proxyEnabled) {
        String modelName = modelMeta.getName();
        logParameters(GenAiModelProvider.HUGGING_FACE.name(), modelName, proxyEnabled, proxyMeta);
        OllamaChatModel.OllamaChatModelBuilder builder = OllamaChatModel.builder()
                .baseUrl(providerMeta.baseUrl())
                .modelName(modelName)
                .maxRetries(1)
                .timeout(defaultTimeout())
                .temperature(temperature);
        OkHttpClientBuilder httpClientBuilder = createHttpClientBuilder(providerMeta, proxyEnabled && providerMeta.useProxy(), proxyMeta);
        builder.httpClientBuilder(httpClientBuilder);
        return new Tuple2<>(builder.build(), httpClientBuilder.getOkHttpClientAdapter());
    }

    @Override
    default Tuple2<StreamingChatModel, OkHttpClientAdapter> buildStreamingChatModel(ProviderMeta providerMeta, ModelMeta modelMeta, double temperature, ProxyMeta proxyMeta, boolean proxyEnabled) {
        String modelName = modelMeta.getName();
        logParameters(GenAiModelProvider.HUGGING_FACE.name(), modelName, proxyEnabled && providerMeta.useProxy(), proxyMeta);
        OllamaStreamingChatModelBuilder builder = OllamaStreamingChatModel.builder()
                .baseUrl(providerMeta.baseUrl())
                .modelName(modelName)
                .timeout(defaultTimeout())
                .temperature(temperature);
        // TODO with maxTokens once Ollama support it.
        OkHttpClientBuilder httpClientBuilder = createHttpClientBuilder(providerMeta, proxyEnabled, proxyMeta);
        builder.httpClientBuilder(httpClientBuilder);
        return new Tuple2<>(builder.build(), httpClientBuilder.getOkHttpClientAdapter());
    }

    @Override
    default String extractErrorMessageFromLLM(String llmMsg) {

        return llmMsg;
    }
}
