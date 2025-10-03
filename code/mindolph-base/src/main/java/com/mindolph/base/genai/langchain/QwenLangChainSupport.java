package com.mindolph.base.genai.langchain;

import com.google.gson.JsonParser;
import com.mindolph.base.genai.llm.OkHttpClientAdapter;
import com.mindolph.core.config.ProxyMeta;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.core.util.Tuple2;
import dev.langchain4j.community.model.dashscope.QwenChatModel.QwenChatModelBuilder;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel.QwenStreamingChatModelBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.apache.commons.lang3.StringUtils;

/**
 * @since 1.13.2
 */
public interface QwenLangChainSupport extends LangChainSupport {

    @Override
    default Tuple2<ChatModel, OkHttpClientAdapter> buildChatModel(ProviderMeta providerMeta, ModelMeta modelMeta, double temperature, ProxyMeta proxyMeta, boolean proxyEnabled) {
        String modelName = modelMeta.getName();
        logParameters(GenAiModelProvider.ALI_Q_WEN.name(), modelName, proxyEnabled && providerMeta.useProxy(), proxyMeta);
        QwenChatModelBuilder builder = new QwenChatModelBuilder()
                .apiKey(providerMeta.apiKey())
                .modelName(modelName)
                .temperature((float) temperature);
        if (modelMeta.maxTokens() != 0) builder.maxTokens(modelMeta.maxTokens());
        // it's sad that the QwenChatModelBuilder neither support customizing the HttpClient nor allow stopping the http request.
        return new Tuple2<>(builder.build(), null);
    }

    @Override
    default Tuple2<StreamingChatModel, OkHttpClientAdapter> buildStreamingChatModel(ProviderMeta providerMeta, ModelMeta modelMeta, double temperature, ProxyMeta proxyMeta, boolean proxyEnabled) {
        String modelName = modelMeta.getName();
        logParameters(GenAiModelProvider.ALI_Q_WEN.name(), modelName, proxyEnabled && providerMeta.useProxy(), proxyMeta);
        QwenStreamingChatModelBuilder builder = new QwenStreamingChatModelBuilder()
                .apiKey(providerMeta.apiKey())
                .modelName(modelName)
                .temperature((float) temperature);
        if (modelMeta.maxTokens() != 0) builder.maxTokens(modelMeta.maxTokens());
        // it's sad that the QwenChatModelBuilder neither support customizing the HttpClient nor allow stopping the http request.
        return new Tuple2<>(builder.build(), null);
    }

    @Override
    default String extractErrorMessageFromLLM(String llmMsg) {
        String message = JsonParser.parseString(llmMsg).getAsJsonObject().get("message").getAsString();
        if (StringUtils.isBlank(message)) {
            message = "Error occurred or user stopped.";
        }
        return message;
    }
}
