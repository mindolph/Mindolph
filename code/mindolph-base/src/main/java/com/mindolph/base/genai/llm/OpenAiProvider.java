package com.mindolph.base.genai.llm;

import com.hw.langchain.chat.models.openai.ChatOpenAI;
import com.mindolph.base.genai.GenAiEvents;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class OpenAiProvider extends BaseLlmProvider {

    private final String apiKey;
    private final String aiModel;

    public OpenAiProvider(String apiKey, String aiModel) {
        super();
        this.apiKey = apiKey;
        this.aiModel = aiModel;
    }

    @Override
    public String predict(String input, float temperature, GenAiEvents.OutputAdjust outputAdjust) {
        ChatOpenAI.ChatOpenAIBuilder<?, ?> builder = ChatOpenAI.builder()
                .openaiApiKey(this.apiKey)
                .model(this.aiModel)
                .stream(false)
                .maxRetries(2)
                .requestTimeout(16)
                .temperature(temperature);
        if (super.proxyEnabled) {
            builder.openaiProxy(super.proxyUrl);
        }
        var llm = builder.build().init();

        return llm.predict(input);
    }
}
