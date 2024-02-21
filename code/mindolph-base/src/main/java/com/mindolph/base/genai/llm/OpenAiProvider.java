package com.mindolph.base.genai.llm;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class OpenAiProvider extends BaseLlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);

    public OpenAiProvider(String apiKey, String aiModel) {
        super(apiKey, aiModel);
    }

    @Override
    protected ChatLanguageModel buildAI(float temperature) {
        log.info("Build OpenAI with model %s and access %s".formatted(this.aiModel,
                super.proxyEnabled ? "with %s proxy %s".formatted(Proxy.Type.valueOf(super.proxyType), this.proxyUrl) : "without proxy"));
        OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(this.apiKey)
                .modelName(this.aiModel)
                .maxRetries(1)
                .timeout(Duration.ofSeconds(timeout))
                .temperature((double) temperature);
        if (super.proxyEnabled) {
            Proxy.Type proxyType = Proxy.Type.valueOf(super.proxyType);
            builder.proxy(new Proxy(proxyType, new InetSocketAddress(this.proxyHost, this.proxyPort)));
        }
        OpenAiChatModel model = builder.build();
        return model;
    }


}
