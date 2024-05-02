package com.mindolph.base.genai.llm;

import com.mindolph.core.constant.GenAiConstants.ProviderProps;
import com.mindolph.base.plugin.PluginEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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
            log.info("Using llm provider: " + activeAiProvider);
            if (OPEN_AI.getName().equals(activeAiProvider)) {
                ProviderProps props = map.get(OPEN_AI.getName());
                llmProvider = new OpenAiProvider(props.apiKey(), props.aiModel());
            }
            else if (GEMINI.getName().equals(activeAiProvider)){
                ProviderProps props = map.get(GEMINI.getName());
                llmProvider = new GeminiProvider(props.apiKey(), props.aiModel());
            }
            else if (ALI_Q_WEN.getName().equals(activeAiProvider)) {
                ProviderProps props = map.get(ALI_Q_WEN.getName());
                llmProvider = new QwenProvider(props.apiKey(), props.aiModel());
            }
            else if (OLLAMA.getName().equals(activeAiProvider)) {
                ProviderProps props = map.get(OLLAMA.getName());
                llmProvider = new OllamaProvider(props.baseUrl(), props.aiModel());
            }
            else if (HUGGING_FACE.getName().equals(activeAiProvider)){
                ProviderProps props = map.get(HUGGING_FACE.getName());
                llmProvider = new HuggingFaceProvider2(props.apiKey(), props.aiModel());
            }
            else if (CHAT_GLM.getName().equals(activeAiProvider)) {
                ProviderProps props = map.get(activeAiProvider);
                llmProvider = new ChatGlmProvider(props.apiKey(), props.aiModel());
            }
            else {
                throw new RuntimeException("No llm provider setup: " + activeAiProvider);
            }
        }
    }


    public String predict(String input, float temperature, OutputParams outputParams) {
        log.info("Generate content with LLM provider");
        String generated = null;
        try {
            generated = llmProvider.predict(input, temperature, outputParams);
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
        if (generated.startsWith(input)) {
            generated = generated.substring(input.length());
        }
        generated = generated.trim();
        log.debug("Generated: " + generated);
        return generated;
    }

    /**
     * Stop the LLM prediction (ignore the generated actually)
     */
    public void stop() {
        this.isStopped = true;
    }

    public String getActiveAiProvider() {
        return activeAiProvider;
    }
}
