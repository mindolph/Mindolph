package com.mindolph.base.genai.llm;

import com.google.gson.JsonParser;
import com.mindolph.base.genai.GenAiEvents.Input;
import dev.langchain4j.community.model.dashscope.QwenChatModel.QwenChatModelBuilder;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel.QwenStreamingChatModelBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.apache.commons.lang3.StringUtils;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7.2
 */
public class QwenProvider extends BaseLangChainLlmProvider {

    public QwenProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    protected ChatModel buildAI(Input input) {
        QwenChatModelBuilder chatModelBuilder = new QwenChatModelBuilder()
                .apiKey(this.apiKey)
                .modelName(determineModel(input))
                .temperature(input.temperature());
        if (input.maxTokens() != 0) chatModelBuilder.maxTokens(input.maxTokens());
        return chatModelBuilder.build();
    }

    @Override
    protected StreamingChatModel buildStreamingAI(Input input) {
        QwenStreamingChatModelBuilder chatModelBuilder = new QwenStreamingChatModelBuilder();
        chatModelBuilder
                .apiKey(this.apiKey)
                .modelName(determineModel(input))
                .temperature(input.temperature());
        if (input.maxTokens() != 0) chatModelBuilder.maxTokens(input.maxTokens());
        return chatModelBuilder.build();
    }

    @Override
    protected String extractErrorMessage(Throwable throwable) {
        String message = JsonParser.parseString(throwable.getLocalizedMessage()).getAsJsonObject().get("message").getAsString();
        if (StringUtils.isBlank(message)) {
            message = "Error occurred or user stopped.";
        }
        return message;
    }
}
