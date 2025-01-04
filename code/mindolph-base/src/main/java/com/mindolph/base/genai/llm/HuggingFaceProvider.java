package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents.Input;
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
    protected ChatLanguageModel buildAI(Input input) {
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", String.valueOf(proxyPort));
        HuggingFaceChatModel.Builder builder = HuggingFaceChatModel.builder()
                .timeout(Duration.ofSeconds(super.timeout))
                .accessToken(apiKey)
                .modelId(determineModel(input))
                .temperature((double) input.temperature());
//        if (super.proxyEnabled) {
//            Proxy.Type proxyType = Proxy.Type.valueOf(super.proxyType);
//            builder.proxy(new Proxy(proxyType, new InetSocketAddress(this.proxyHost, this.proxyPort)));
//        }
        return builder.build();
    }

    @Override
    protected StreamingChatLanguageModel buildStreamingAI(Input input) {
        return null;
    }

    @Override
    protected String extractErrorMessage(Throwable throwable) {
        return "";
    }
}
