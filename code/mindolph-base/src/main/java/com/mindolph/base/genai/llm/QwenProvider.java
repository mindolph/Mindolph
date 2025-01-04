package com.mindolph.base.genai.llm;

import com.google.gson.JsonParser;
import com.mindolph.base.genai.GenAiEvents;
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
    protected ChatLanguageModel buildAI(GenAiEvents.Input input) {
        QwenChatModel.QwenChatModelBuilder chatModelBuilder = new QwenChatModel.QwenChatModelBuilder()
                .apiKey(this.apiKey)
                .modelName(determineModel(input))
                .temperature(input.temperature());
        return chatModelBuilder.build();
    }

    @Override
    protected StreamingChatLanguageModel buildStreamingAI(GenAiEvents.Input input) {
        QwenStreamingChatModel.QwenStreamingChatModelBuilder chatModelBuilder = new QwenStreamingChatModel.QwenStreamingChatModelBuilder();
        chatModelBuilder
                .apiKey(this.apiKey)
                .modelName(determineModel(input))
                .temperature(input.temperature());
        return chatModelBuilder.build();
    }

    @Override
    protected String extractErrorMessage(Throwable throwable) {
        return JsonParser.parseString(throwable.getLocalizedMessage()).getAsJsonObject().get("message").getAsString();
    }
}
