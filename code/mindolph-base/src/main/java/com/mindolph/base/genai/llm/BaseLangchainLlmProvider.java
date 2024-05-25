package com.mindolph.base.genai.llm;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7.4
 */
public abstract class BaseLangchainLlmProvider extends BaseLlmProvider {

    private static final Logger log = LoggerFactory.getLogger(BaseLangchainLlmProvider.class);

    public BaseLangchainLlmProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    public String predict(String input, float temperature, OutputParams outputParams) {
        log.debug("Proxy: " + System.getenv("http.proxyHost"));
        PromptTemplate promptTemplate = PromptTemplate.from(TEMPLATE);
        Map<String, Object> params = super.formatParams(input, outputParams);
        log.debug(String.valueOf(params));
        Prompt prompt = promptTemplate.apply(params);
        log.info("prompt: %s".formatted(prompt.text()));
        ChatLanguageModel llm = buildAI(temperature);
        return llm.generate(prompt.text());
    }

    protected abstract ChatLanguageModel buildAI(float temperature);
}
