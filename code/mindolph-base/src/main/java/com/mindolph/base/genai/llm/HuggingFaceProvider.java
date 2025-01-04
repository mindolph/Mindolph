package com.mindolph.base.genai.llm;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;

import java.time.Duration;

/**
 * @deprecated
 */
public class HuggingFaceProvider extends BaseLangChainLlmProvider {

    public HuggingFaceProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    protected ChatLanguageModel buildAI(float temperature) {
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", String.valueOf(proxyPort));
        HuggingFaceChatModel.Builder builder = HuggingFaceChatModel.builder()
                .timeout(Duration.ofSeconds(super.timeout))
                .accessToken(apiKey)
                .modelId(aiModel)
                .temperature((double) temperature);
//        if (super.proxyEnabled) {
//            Proxy.Type proxyType = Proxy.Type.valueOf(super.proxyType);
//            builder.proxy(new Proxy(proxyType, new InetSocketAddress(this.proxyHost, this.proxyPort)));
//        }
        return builder.build();
    }

    @Override
    protected StreamingChatLanguageModel buildStreamingAI(float temperature) {
        return null;
    }

    @Override
    protected String extractErrorMessage(Throwable throwable) {
        return "";
    }
}
