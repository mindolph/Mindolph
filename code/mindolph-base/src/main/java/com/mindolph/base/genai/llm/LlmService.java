package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class LlmService {
    private static final Logger log = LoggerFactory.getLogger(LlmService.class);
    private static LlmService ins;
    private boolean isStopped; // stopped by user

    private final Map<GenAiModelProvider, LlmProvider> llmProviderMap = new HashMap<>();

    public static synchronized LlmService getIns() {
        if (ins == null) {
            ins = new LlmService();
        }
        return ins;
    }

    private LlmService() {
        // reload active LLM provider if preferences changed
        PluginEventBus.getIns().subscribePreferenceChanges(o -> {
            log.info("On preferences changed, reload LLM");
//            this.loadActiveLlm();
        });
    }

    private LlmProvider getLlmProvider(GenAiModelProvider provider, String modelName) {
        LlmProvider llmProvider;
        if (Boolean.parseBoolean(System.getenv("mock-llm"))) {
            log.warn("Using mock LLM provider");
            llmProvider = new DummyLlmProvider();
            llmProviderMap.put(provider, llmProvider);
        }
        else {
            llmProvider = llmProviderMap.get(provider);
            if (llmProvider == null) {
                ProviderMeta providerMeta = LlmConfig.getIns().loadProviderMeta(provider.name());
                ModelMeta modelMeta = LlmConfig.getIns().lookupModel(provider, modelName);
                llmProvider = LlmProviderFactory.create(provider.name(), providerMeta, modelMeta);
                log.info("Using llm provider %s and model %s".formatted(provider.name(), modelName));
                llmProviderMap.put(provider, llmProvider);
            }
        }
        return llmProvider;
    }

    /**
     * @param input
     * @param outputParams
     * @return
     */
    public StreamPartial predict(Input input, OutputParams outputParams) {
        log.info("Generate content with LLM provider");
        isStopped = false;
        StreamPartial generated = null;
        try {
            LlmProvider llmProvider = getLlmProvider(input.provider(), input.model());
            generated = llmProvider.predict(input, outputParams);
        } catch (Exception e) {
            if (isStopped) {
                log.debug("Got exception but the process is stopped: " + e.getLocalizedMessage());
                return null;
            }
            else {
                throw e;
            }
        }
        if (isStopped) {
            isStopped = false;
            return null; // force to return null since it has been stopped by user.
        }
        // strip user input if the generated text contains use input in the head of it.
        if (generated.text().startsWith(input.text())) {
            generated.setText(generated.text().substring(input.text().length()));
        }
        generated.setText(generated.text().trim());
        log.debug("Generated: %s".formatted(generated));
        return generated;
    }

    /**
     * @param input
     * @param outputParams
     * @param consumer     to handle streaming result, like streaming output, error handling or stopping handling.
     */
    public void stream(Input input, OutputParams outputParams, Consumer<StreamPartial> consumer) {
        isStopped = false;
        LlmProvider llmProvider = getLlmProvider(input.provider(), input.model());
        llmProvider.stream(input, outputParams, streamToken -> {
            if (isStopped) {
                // Don't use stopping flag to control the working states, since the stream might return with multiple times even you stop it.
                llmProvider.stopStreaming();
                // force to stop
                streamToken.setStop(true);
                streamToken.setText("Streaming is stopped by user/exception");
                consumer.accept(streamToken);
                // throw new RuntimeException("Streaming is stopped by user/exception"); // this exception stops the streaming from http connection.
            }
            consumer.accept(streamToken);
        });
    }

    /**
     * Stop the LLM actions (ignore the generated actually)
     */
    public void stop() {
        this.isStopped = true;
    }

    public boolean isStopped() {
        return isStopped;
    }

}
