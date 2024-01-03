package com.mindolph.base.genai.llm;

import com.hw.langchain.chat.models.openai.ChatOpenAI;
import com.mindolph.base.genai.GenAiEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class OpenAiProvider extends BaseLlmProvider {
    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);

    private final String apiKey;
    private final String aiModel;

    public OpenAiProvider(String apiKey, String aiModel) {
        super();
        this.apiKey = apiKey;
        this.aiModel = aiModel;
    }

    private ChatOpenAI buildAI(float temperature) {
        log.info("Build OpenAI with model %s and access %s proxy".formatted(this.aiModel, super.proxyEnabled ? "with" : "without"));
        ChatOpenAI.ChatOpenAIBuilder<?, ?> builder = ChatOpenAI.builder()
                .openaiApiKey(this.apiKey)
                .model(this.aiModel)
                .stream(false)
                .maxRetries(1)
                .requestTimeout(5)
                .temperature(temperature);
        if (super.proxyEnabled) {
            builder.openaiProxy(super.proxyUrl);
        }
        return builder.build().init();
    }

    @Override
    public String predict(String input, float temperature, GenAiEvents.OutputAdjust outputAdjust) {
        // TODO with adjust options
        return buildAI(temperature).predict(input);
    }
}
