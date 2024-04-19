package com.mindolph.base.genai.llm;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenChatModel;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7.2
 */
public class QwenProvider extends BaseLangchainLlmProvider {
    public QwenProvider(String apiKey, String aiModel) {
        super(apiKey, aiModel);
    }

    @Override
    protected ChatLanguageModel buildAI(float temperature) {
        QwenChatModel.QwenChatModelBuilder chatModelBuilder = new QwenChatModel.QwenChatModelBuilder()
                .apiKey(this.apiKey)
                .modelName(this.aiModel)
                .temperature(temperature);
        return chatModelBuilder.build();
    }
}
