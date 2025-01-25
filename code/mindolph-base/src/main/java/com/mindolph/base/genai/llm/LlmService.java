package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.llm.ProviderProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

import static com.mindolph.core.constant.GenAiModelProvider.*;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class LlmService {
    private static final Logger log = LoggerFactory.getLogger(LlmService.class);
    private static LlmService ins;
    private LlmProvider llmProvider;
    private boolean isStopped;
    private String activeAiProvider;

    public static synchronized LlmService getIns() {
        if (ins == null) {
            ins = new LlmService();
        }
        return ins;
    }

    private LlmService() {
        this.loadActiveLlm();
        // reload active LLM provider if preferences changed
        PluginEventBus.getIns().subscribePreferenceChanges(o -> {
            log.info("On preferences changed, reload LLM");
            this.loadActiveLlm();
        });
    }

    private void loadActiveLlm() {
        if (Boolean.parseBoolean(System.getenv("mock-llm"))) {
            log.warn("Using mock LLM provider");
            llmProvider = new DummyLlmProvider();
        }
        else {
            Map<String, ProviderProps> map = LlmConfig.getIns().loadGenAiProviders();
            activeAiProvider = LlmConfig.getIns().getActiveAiProvider();
            log.info("Using llm provider: %s".formatted(activeAiProvider));
            if (OPEN_AI.getName().equals(activeAiProvider)) {
                ProviderProps props = map.get(OPEN_AI.getName());
                llmProvider = new OpenAiProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (GEMINI.getName().equals(activeAiProvider)) {
                ProviderProps props = map.get(GEMINI.getName());
                llmProvider = new GeminiProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (ALI_Q_WEN.getName().equals(activeAiProvider)) {
                ProviderProps props = map.get(ALI_Q_WEN.getName());
                llmProvider = new QwenProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (OLLAMA.getName().equals(activeAiProvider)) {
                ProviderProps props = map.get(OLLAMA.getName());
                llmProvider = new OllamaProvider(props.baseUrl(), props.aiModel(), props.useProxy());
            }
            else if (HUGGING_FACE.getName().equals(activeAiProvider)) {
                ProviderProps props = map.get(HUGGING_FACE.getName());
                llmProvider = new HuggingFaceProvider2(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (CHAT_GLM.getName().equals(activeAiProvider)) {
                ProviderProps props = map.get(activeAiProvider);
                llmProvider = new ChatGlmProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else if (DEEP_SEEK.getName().equals(activeAiProvider)) {
                ProviderProps props = map.get(DEEP_SEEK.getName());
                llmProvider = new DeepSeekProvider(props.apiKey(), props.aiModel(), props.useProxy());
            }
            else {
                throw new RuntimeException("No llm provider setup: " + activeAiProvider);
            }
        }
    }


    public StreamToken predict(Input input, OutputParams outputParams) {
        log.info("Generate content with LLM provider");
        StreamToken generated = null;
        try {
            generated = llmProvider.predict(input, outputParams);
        } catch (Exception e) {
            if (isStopped) {
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

    public void stream(Input input, OutputParams outputParams, Consumer<StreamToken> consumer) {
        llmProvider.stream(input, outputParams, streamToken -> {
            if (isStopped) {
                isStopped = false;
                consumer.accept(streamToken);
                // this exception stops the streaming from http connection.
                throw new RuntimeException("Streaming is stopped by user/exception");
            }
            consumer.accept(streamToken);
        });
    }

    /**
     *
     * @param input
     * @param outputParams
     * @param consumer
     * @since 1.11
     */
    public void summarize(Input input, OutputParams outputParams, Consumer<StreamToken> consumer) {
        String prompt = """
                summarize following content concisely in same language:
                ```
                %s
                ```
                """.formatted(input.text());
        // replace with new prompt
        Input in = new Input(input.model(), prompt, input.temperature(), input.maxTokens(), input.outputAdjust(), input.isRetry(), input.isStreaming());
        llmProvider.stream(in, outputParams, streamToken -> {
            if (isStopped) {
                isStopped = false;
                consumer.accept(streamToken);
                throw new RuntimeException("Streaming is stopped by user/exception"); // this exception stops the streaming from http connection.
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

    public String getActiveAiProvider() {
        return activeAiProvider;
    }
}
