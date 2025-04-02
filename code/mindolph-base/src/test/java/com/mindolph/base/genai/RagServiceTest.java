package com.mindolph.base.genai;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.rag.RagService;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.ProviderProps;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.swiftboot.util.ClasspathResourceUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @since unknown
 */
public class RagServiceTest extends BaseLlmTest {

    @BeforeEach
    public void setupAgents() {
        String agentsJson = """
                {
                  "4c1ce115-2215-41c6-b612-0c423471983e": {
                    "id": "4c1ce115-2215-41c6-b612-0c423471983e",
                    "name": "My Agent",
                    "provider": "DEEP_SEEK",
                    "chatModel": {
                      "name": "deepseek-chat",
                      "maxTokens": 8192,
                      "active": false
                    },
                    "promptTemplate": ""
                  }
                }
                """;

        FxPreferences.getInstance().savePreference(PrefConstants.GEN_AI_AGENTS, agentsJson);

        // config provider and save to preferences
        String json = ClasspathResourceUtils.readResourceToString("api_keys.json");

        if (StringUtils.isNotBlank(json)) {
            super.props = JsonParser.parseString(json).getAsJsonObject();
            Map<String, ProviderProps> propsMap = new HashMap<>();
            String apiKey = loadApiKey(GenAiModelProvider.DEEP_SEEK.getName());
            ProviderProps pp = new ProviderProps(apiKey, "", "deepseek-chat", false);
            propsMap.put(GenAiModelProvider.DEEP_SEEK.getName(), pp);
            FxPreferences.getInstance().savePreference(PrefConstants.GEN_AI_PROVIDERS, new Gson().toJson(propsMap));
        }
    }

    @Test
    public void test() {
        super.enableProxy();
        Map<String, AgentMeta> agentMap = LlmConfig.getIns().loadAgents();
        if (agentMap == null || agentMap.isEmpty()) {
            Assertions.fail("Setup agents first");
        }
        AgentMeta agentMeta = agentMap.get(agentMap.keySet().stream().findFirst().get());
        RagService.getInstance().useAgent(agentMeta, () -> {
            RagService.getInstance().chat("What can I do?", tokenStream -> {
                tokenStream.onRetrieved(contents -> {
                            System.out.println("onRetrieved: " + contents);
                        })
                        .onPartialResponse(s -> System.out.println("onPartialResponse: " + s))
                        .onError(throwable -> System.out.println("onError: " + throwable)).start();
            });
        });
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
