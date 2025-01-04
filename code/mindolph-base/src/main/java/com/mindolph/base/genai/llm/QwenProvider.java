package com.mindolph.base.genai.llm;

import com.google.gson.JsonParser;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenChatModel;
import dev.langchain4j.model.dashscope.QwenStreamingChatModel;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7.2
 */
public class QwenProvider extends BaseLangChainLlmProvider {

    public QwenProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    protected ChatLanguageModel buildAI(float temperature) {
        QwenChatModel.QwenChatModelBuilder chatModelBuilder = new QwenChatModel.QwenChatModelBuilder()
                .apiKey(this.apiKey)
                .modelName(this.aiModel)
                .temperature(temperature);
        return chatModelBuilder.build();
    }

    @Override
    protected StreamingChatLanguageModel buildStreamingAI(float temperature) {
        QwenStreamingChatModel.QwenStreamingChatModelBuilder chatModelBuilder = new QwenStreamingChatModel.QwenStreamingChatModelBuilder();
        chatModelBuilder
                .apiKey(this.apiKey)
                .modelName(this.aiModel)
                .temperature(temperature);
        return chatModelBuilder.build();
    }

    @Override
    protected String extractErrorMessage(Throwable throwable) {
        return JsonParser.parseString(throwable.getLocalizedMessage()).getAsJsonObject().get("message").getAsString();
    }
}
