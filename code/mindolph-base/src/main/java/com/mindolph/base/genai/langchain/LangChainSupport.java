package com.mindolph.base.genai.langchain;

import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.llm.OkHttpClientAdapter;
import com.mindolph.base.genai.llm.OkHttpClientBuilder;
import com.mindolph.core.config.ProxyMeta;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.core.util.Tuple2;
import com.mindolph.mfx.preference.FxPreferences;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Proxy;
import java.time.Duration;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_TIMEOUT;

/**
 * @since 1.13.2
 */
public interface LangChainSupport {

    Logger log = LoggerFactory.getLogger(LangChainSupport.class);

    /**
     * Build langchain chat model for generating chatting (no streaming).
     *
     * @param providerMeta
     * @param modelMeta
     * @param temperature
     * @param proxyMeta
     * @param proxyEnabled
     * @return langchain chat model and the adapter of OKHttp client (to pause/stop http request)
     */
    Tuple2<ChatModel, OkHttpClientAdapter> buildChatModel(ProviderMeta providerMeta, ModelMeta modelMeta, double temperature, ProxyMeta proxyMeta, boolean proxyEnabled);

    /**
     * Build langchain chat model for streaming chatting.
     *
     * @param providerMeta
     * @param modelMeta
     * @param temperature
     * @param proxyMeta
     * @param proxyEnabled
     * @return langchain chat model and the adapter of OKHttp client (to pause/stop http request)
     */
    Tuple2<StreamingChatModel, OkHttpClientAdapter> buildStreamingChatModel(ProviderMeta providerMeta, ModelMeta modelMeta, double temperature, ProxyMeta proxyMeta, boolean proxyEnabled);

    /**
     * Extract readable error message from llm message,
     * Note: if the LLM is meant to return JSON, but turns out other format, exception will throw.
     *
     * @param llmMsg
     * @return
     */
    String extractErrorMessageFromLLM(String llmMsg);

    /**
     * Factory method to create the LangChainSupport implementation by the provider type.
     *
     * @param providerType
     * @return
     */
    static LangChainSupport createSupport(GenAiModelProvider providerType) {
        switch (providerType) {
            case OPEN_AI -> {
                return new OpenAiLangChainSupport() {
                };
            }
            case ALI_Q_WEN -> {
                return new QwenLangChainSupport() {
                };
            }
            case GEMINI -> {
                return new GeminiLangChainSupport() {
                };
            }
            case OLLAMA -> {
                return new OllamaLangChainSupport() {
                };
            }
            default -> throw new RuntimeException("Not supported provider:%s".formatted(providerType.name()));
        }
    }

    default ModelMeta determineModel(Input input, ModelMeta modelMeta) {
        if (StringUtils.isBlank(input.model())) {
            return modelMeta;
        }
        else {
            return LlmConfig.getIns().lookupModel(input.provider(), input.model());
        }
    }

    default Duration defaultTimeout() {
        return Duration.ofSeconds(FxPreferences.getInstance().getPreference(GEN_AI_TIMEOUT, 30));
    }

    /**
     * Create http client builder with proxy support only if proxy is enabled and proxy meta is provided.
     *
     * @return
     * @since 1.13.1
     */
    default OkHttpClientBuilder createHttpClientBuilder(ProviderMeta providerMeta, boolean proxyEnabled, ProxyMeta proxyMeta) {
        OkHttpClientBuilder okHttpClientBuilder = new OkHttpClientBuilder();
        okHttpClientBuilder.connectTimeout(defaultTimeout());
        if (providerMeta.useProxy() && proxyEnabled && proxyMeta != null) {
            okHttpClientBuilder.setProxyHost(proxyMeta.host()).setProxyPort(proxyMeta.port()).setProxyType(proxyMeta.type().toUpperCase());
        }
        return okHttpClientBuilder;
    }

    /**
     *
     * @param providerName
     * @param modelName
     * @param useProxy     proxy enabled globally and activated for the provider.
     * @param proxyMeta
     */
    default void logParameters(String providerName, String modelName, boolean useProxy, ProxyMeta proxyMeta) {
        log.info("Build %s model %s and access %s".formatted(providerName, modelName,
                useProxy ? "with %s proxy %s".formatted(Proxy.Type.valueOf(proxyMeta.type().toUpperCase()), proxyMeta.url()) : "without proxy"));
    }
}
