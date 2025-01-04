package com.mindolph.base.genai.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7.4
 */
public abstract class BaseLangChainLlmProvider extends BaseLlmProvider {

    private static final Logger log = LoggerFactory.getLogger(BaseLangChainLlmProvider.class);

    public BaseLangChainLlmProvider(String apiKey, String aiModel, boolean useProxy) {
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

    @Override
    public void stream(String input, float temperature, OutputParams outputParams, Consumer<StreamToken> consumer) {
        Prompt prompt = this.createPrompt(input, outputParams);
        StreamingChatLanguageModel llm = buildStreamingAI(temperature);
        llm.generate(prompt.text().trim(), new StreamingResponseHandler<>() {
            @Override
            public void onNext(String s) {
                consumer.accept(new StreamToken(s, false, false));
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                log.debug("completed: %s%n".formatted(response.content()));
                consumer.accept(new StreamToken(response.content().text(), true, false));
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("LLM streaming with error.", throwable);
                // force to stop when exception happens.
                if (throwable != null) {
                    consumer.accept(new StreamToken(extractErrorMessage(throwable), true, true));
                }
            }
        });

    }

    private Prompt createPrompt(String input, OutputParams outputParams) {
        log.debug("Proxy: %s".formatted(System.getenv("http.proxyHost")));
        PromptTemplate promptTemplate = PromptTemplate.from(TEMPLATE);
        Map<String, Object> params = super.formatParams(input, outputParams);
        log.debug(String.valueOf(params));
        Prompt prompt = promptTemplate.apply(params);
        log.info("prompt: '%s'".formatted(prompt.text()));
        return prompt;
    }

    protected abstract ChatLanguageModel buildAI(float temperature);

    protected abstract StreamingChatLanguageModel buildStreamingAI(float temperature);

    protected abstract String extractErrorMessage(Throwable throwable);
}
