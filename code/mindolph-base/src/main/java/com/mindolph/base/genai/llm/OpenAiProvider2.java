package com.mindolph.base.genai.llm;

import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.HashMap;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class OpenAiProvider2 extends BaseLlmProvider {
    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider2.class);

    private final String apiKey;
    private final String aiModel;
    private final String TEMPLATE = """
            {input}
            your output must be in format: {format}.
            your output should be {length}.
            """;

    public OpenAiProvider2(String apiKey, String aiModel) {
        super();
        this.apiKey = apiKey;
        this.aiModel = aiModel;
    }

    private OpenAiChatModel buildAI(float temperature) {
        log.info("Build OpenAI with model %s and access %s".formatted(this.aiModel,
                super.proxyEnabled ? "with proxy %s".formatted(this.proxyUrl) : "without proxy"));
        OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(this.apiKey)
                .modelName(this.aiModel)
                .maxRetries(1)
                .timeout(Duration.ofSeconds(30))
                .temperature((double) temperature);
        if (super.proxyEnabled) {
            Proxy.Type proxyType = Proxy.Type.valueOf(super.proxyType);
            builder.proxy(new Proxy(proxyType, new InetSocketAddress(this.proxyHost, this.proxyPort)));
        }
        OpenAiChatModel model = builder.build();
        return model;
    }

    @Override
    public String predict(String input, float temperature, OutputParams outputParams) {
        PromptTemplate promptTemplate = PromptTemplate.from(TEMPLATE);
        OpenAiChatModel chatOpenAI = buildAI(temperature);
        Prompt prompt = promptTemplate.apply(new HashMap<>() {
            {
                put("input", input);
                put("format", "Markdown");
                put("length", outputParams.outputAdjust() == Constants.OutputAdjust.SHORTER ? "simplified" : "detailed");
            }
        });
        return chatOpenAI.generate(prompt.text());
    }
}
