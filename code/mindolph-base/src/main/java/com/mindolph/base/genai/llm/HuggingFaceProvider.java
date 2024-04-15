package com.mindolph.base.genai.llm;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

/**
 * @deprecated
 */
public class HuggingFaceProvider extends BaseLangchainLlmProvider {

    public HuggingFaceProvider(String apiKey, String aiModel) {
        super(apiKey, aiModel);
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
}
