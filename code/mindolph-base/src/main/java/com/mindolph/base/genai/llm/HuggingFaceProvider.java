package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;

import java.time.Duration;

/**
 * HuggingFace requires proxy but LangChain doesn't support it yet.
 *
 * @deprecated
 */
public class HuggingFaceProvider extends BaseLangChainLlmProvider {

    public HuggingFaceProvider(ProviderMeta providerMeta, ModelMeta modelMeta) {
        super(providerMeta, modelMeta);
    }

    @Override
    protected ChatModel buildAI(Input input) {
        System.setProperty("http.proxyHost", proxyMeta.host());
        System.setProperty("http.proxyPort", String.valueOf(proxyMeta.port()));
        HuggingFaceChatModel.Builder builder = HuggingFaceChatModel.builder()
                .timeout(Duration.ofSeconds(super.timeout))
                .accessToken(providerMeta.apiKey())
                .modelId(determineModel(input))
                .temperature((double) input.temperature());
        if (input.maxTokens() != 0) builder.maxNewTokens(input.maxTokens());
//        if (super.proxyEnabled) {
//            Proxy.Type proxyType = Proxy.Type.valueOf(super.proxyType);
//            builder.proxy(new Proxy(proxyType, new InetSocketAddress(this.proxyHost, this.proxyPort)));
//        }
        return builder.build();
    }

    @Override
    protected StreamingChatModel buildStreamingAI(Input input) {
        return null;
    }

    @Override
    protected String extractErrorMessage(Throwable throwable) {
        return "";
    }
}
