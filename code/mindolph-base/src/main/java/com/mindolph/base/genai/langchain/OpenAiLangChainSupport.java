package com.mindolph.base.genai.langchain;

import com.google.gson.JsonParser;
import com.mindolph.base.genai.llm.OkHttpClientAdapter;
import com.mindolph.base.genai.llm.OkHttpClientBuilder;
import com.mindolph.core.config.ProxyMeta;
import com.mindolph.core.constant.AiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.core.util.Tuple2;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.13.2
 */
public interface OpenAiLangChainSupport extends LangChainSupport {

    // OpenAI compatible APIS's base url.
    Map<AiModelProvider, String> BASE_URLS = new HashMap<>() {
        {
            put(AiModelProvider.CHAT_GLM, "https://open.bigmodel.cn/api/paas/v4");
            put(AiModelProvider.MOONSHOT, "https://api.moonshot.cn/v1/");
            put(AiModelProvider.DEEP_SEEK, "https://api.deepseek.com/");
            put(AiModelProvider.ALI_Q_WEN, "https://dashscope.aliyuncs.com/compatible-mode/v1");
            put(AiModelProvider.QIANFAN, "https://qianfan.baidubce.com/v2");
            put(AiModelProvider.HUGGING_FACE, "https://router.huggingface.co/v1");
        }
    };

    @Override
    default Tuple2<ChatModel, OkHttpClientAdapter> buildChatModel(ProviderMeta providerMeta, ModelMeta modelMeta, double temperature, ProxyMeta proxyMeta, boolean proxyEnabled) {
        String modelName = modelMeta.getName();
        double safeTemperature = Math.round(temperature * 100) / 100.0;
        logParameters(providerMeta.getName(), modelName, proxyEnabled && providerMeta.useProxy(), proxyMeta);
        OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(providerMeta.apiKey())
                .modelName(modelName)
                .sendThinking(false)
                .returnThinking(false)
                .maxRetries(1)
                .timeout(defaultTimeout());
        if (StringUtils.isNotBlank(providerMeta.baseUrl())) {
            // user provided base url.
            builder.baseUrl(providerMeta.baseUrl());
        }
        else {
            // base url from official.
            builder.baseUrl(getBaseUrl(providerMeta.getId()));
        }
        // The moonshot doesn't need the temperature
        if (!AiModelProvider.MOONSHOT.name().equals(providerMeta.getId())) {
            builder.temperature(safeTemperature);
        }
        if (modelMeta.maxTokens() != 0) builder.maxTokens(modelMeta.maxTokens());
        OkHttpClientBuilder httpClientBuilder = createHttpClientBuilder(providerMeta, proxyEnabled && providerMeta.useProxy(), proxyMeta);
        builder.httpClientBuilder(httpClientBuilder);
        return new Tuple2<>(builder.build(), httpClientBuilder.getOkHttpClientAdapter());
    }

    @Override
    default Tuple2<StreamingChatModel, OkHttpClientAdapter> buildStreamingChatModel(ProviderMeta providerMeta, ModelMeta modelMeta, double temperature, ProxyMeta proxyMeta, boolean proxyEnabled) {
        String modelName = modelMeta.getName();
        // the temperature in JSON might be too long for some APIs.
        double safeTemperature = Math.round(temperature * 100) / 100.0;
        logParameters(providerMeta.getName(), modelName, proxyEnabled && providerMeta.useProxy(), proxyMeta);
        OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .apiKey(providerMeta.apiKey())
                .modelName(modelName)
                .sendThinking(false)
                .returnThinking(false)
                .timeout(defaultTimeout());
        if (StringUtils.isNotBlank(providerMeta.baseUrl())) {
            // user provided base url.
            builder.baseUrl(providerMeta.baseUrl());
        }
        else {
            // base url from official.
            builder.baseUrl(getBaseUrl(providerMeta.getId()));
        }
        // The moonshot doesn't need the temperature
        if (!AiModelProvider.MOONSHOT.name().equals(providerMeta.getId())) {
            builder.temperature(safeTemperature);
        }
        if (modelMeta.maxTokens() != 0) builder.maxTokens(modelMeta.maxTokens());
        OkHttpClientBuilder httpClientBuilder = createHttpClientBuilder(providerMeta, proxyEnabled && providerMeta.useProxy(), proxyMeta);
        builder.httpClientBuilder(httpClientBuilder);
        return new Tuple2<>(builder.build(), httpClientBuilder.getOkHttpClientAdapter());
    }

    @Override
    default String extractErrorMessageFromLLM(String llmMsg) {
        log.debug("Response: " + llmMsg);
        try {
            return JsonParser.parseString(llmMsg).getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString();
        } catch (Exception e) {
            return llmMsg;
        }
    }

    default String getBaseUrl(String providerId) {
        return BASE_URLS.get(AiModelProvider.valueOf(providerId));
    }
}
