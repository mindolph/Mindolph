package com.mindolph.base.genai.llm;

import com.hw.langchain.chains.llm.LLMChain;
import com.hw.langchain.chat.models.openai.ChatOpenAI;
import com.hw.langchain.prompts.prompt.PromptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class OpenAiProvider extends BaseLlmProvider {
    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);

    private final String apiKey;
    private final String aiModel;
    private final String TEMPLATE = """
            {input}
            your output must be in format: {format}.
            your output should be {length}.
            """;

    public OpenAiProvider(String apiKey, String aiModel) {
        super();
        this.apiKey = apiKey;
        this.aiModel = aiModel;
    }

    private ChatOpenAI buildAI(float temperature) {
        log.info("Build OpenAI with model %s and access %s".formatted(this.aiModel,
                super.proxyEnabled ? "with proxy %s".formatted(this.proxyUrl) : "without proxy"));
        ChatOpenAI.ChatOpenAIBuilder<?, ?> builder = ChatOpenAI.builder()
                .openaiApiKey(this.apiKey)
                .model(this.aiModel)
                .stream(false)
                .maxRetries(1)
                .requestTimeout(60)
                .temperature(temperature);
        if (super.proxyEnabled) {
            builder.openaiProxy(super.proxyUrl);
        }
        return builder.build().init();
    }

    @Override
    public String predict(String input, float temperature, OutputParams outputParams) {
        PromptTemplate promptTemplate = PromptTemplate.fromTemplate(TEMPLATE);
        ChatOpenAI chatOpenAI = buildAI(temperature);
        LLMChain chain = new LLMChain(chatOpenAI, promptTemplate);
        return chain.predict(new HashMap<>() {
            {
                put("input", input);
                put("format", "Markdown");
                put("length", outputParams.outputAdjust() == Constants.OutputAdjust.SHORTER? "simplified" : "detailed");
            }
        });
//        return chatOpenAI.predict(input);
    }
}
