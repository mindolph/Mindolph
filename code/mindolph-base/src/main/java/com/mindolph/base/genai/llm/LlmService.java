package com.mindolph.base.genai.llm;

import com.mindolph.base.constant.PrefConstants.ProviderProps;
import com.mindolph.base.plugin.PluginEventBus;
import com.mindolph.core.constant.GenAiModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class LlmService {
    private static final Logger log = LoggerFactory.getLogger(LlmService.class);
    private static final LlmService ins = new LlmService();
    private LlmProvider llmProvider;

    public static synchronized LlmService getIns() {
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
            ProviderProps props = map.get(GenAiModelProvider.OPEN_AI.getName());
//            llmProvider = new OpenAiProvider(props.apiKey(), props.aiModel());
            llmProvider = new OpenAiProvider2(props.apiKey(), props.aiModel());
        }
    }


    public String predict(String input, float temperature, OutputParams outputParams) {
        log.info("Generate content with LLM provider");
        return llmProvider.predict(input, temperature, outputParams);
    }

}
