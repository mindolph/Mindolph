package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents.Input;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
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

    private boolean stopStreaming = false;

    public BaseLangChainLlmProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    public StreamToken predict(Input input, OutputParams outputParams) {
        log.debug("Proxy: %s".formatted(System.getenv("http.proxyHost")));
        PromptTemplate promptTemplate = PromptTemplate.from(PROMPT_FORMAT_TEMPLATE);
        Map<String, Object> params = super.formatParams(input.text(), outputParams);
        log.debug(String.valueOf(params));
        Prompt prompt = promptTemplate.apply(params);
        log.info("prompt: %s".formatted(prompt.text()));
        ChatModel llm = buildAI(input);
        ChatMessage chatMessage = new UserMessage(prompt.text());
        ChatResponse aiMessage = llm.chat(chatMessage);
        return new StreamToken(aiMessage.aiMessage().text(), aiMessage.tokenUsage().outputTokenCount(), true, false);
    }

    @Override
    public void stream(Input input, OutputParams outputParams, Consumer<StreamToken> consumer) {
        Prompt prompt = this.createPrompt(input.text(), outputParams);
        StreamingChatModel llm = buildStreamingAI(input);
        this.stopStreaming = false;
        llm.chat(prompt.text().trim(), new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String s) {
                if (stopStreaming){
                    throw new RuntimeException("user stop streaming");
                }
                consumer.accept(new StreamToken(s, false, false));
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                log.debug("completed: %s%n".formatted(response.aiMessage()));
                consumer.accept(new StreamToken(response.aiMessage().text(), response.tokenUsage().outputTokenCount(),
                        true, false));
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

    @Override
    public void stopStreaming() {
        this.stopStreaming = true;
    }

    private Prompt createPrompt(String input, OutputParams outputParams) {
        PromptTemplate promptTemplate = PromptTemplate.from(PROMPT_FORMAT_TEMPLATE);
        Map<String, Object> params = super.formatParams(input, outputParams);
        Prompt prompt = promptTemplate.apply(params);
        log.info("prompt: '%s'".formatted(prompt.text()));
        return prompt;
    }

    /**
     * Create proxied http client builder if required.
     *
     * @return
     * @since 1.13.1
     */
    protected HttpClientBuilder createProxyHttpClientBuilder() {
        if (super.proxyEnabled && super.useProxy) {
            OkHttpClientBuilderAdapter okHttpClientBuilder = new OkHttpClientBuilderAdapter();
            okHttpClientBuilder.setProxyHost(super.proxyHost).setProxyPort(super.proxyPort).setProxyType(super.proxyType.toUpperCase());
            return okHttpClientBuilder;
        }
        else {
            return null;
        }
    }

    protected abstract ChatModel buildAI(Input input);

    protected abstract StreamingChatModel buildStreamingAI(Input input);

    protected abstract String extractErrorMessage(Throwable throwable);
}
