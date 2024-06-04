package com.mindolph.base.genai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mindolph.base.genai.llm.ChatGlmProvider;
import com.mindolph.base.genai.llm.HuggingFaceProvider2;
import com.mindolph.base.genai.llm.OutputParams;
import com.mindolph.base.genai.llm.QwenProvider;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.swiftboot.util.ClasspathResourceUtils;

import static com.mindolph.core.constant.GenAiModelProvider.*;

/**
 * @since 1.7.5
 */
public class LlmProviderTest {


    private JsonObject props;

    @BeforeEach
    public void setup() {
        FxPreferences.getInstance().init(LlmProviderTest.class);

        FxPreferences.getInstance().savePreference("general.proxy.enable", "false"); // disable by default.
        FxPreferences.getInstance().savePreference("general.proxy.type", "HTTP");
        FxPreferences.getInstance().savePreference("general.proxy.host", "127.0.0.1");
        FxPreferences.getInstance().savePreference("general.proxy.port", "2088");

        String json = ClasspathResourceUtils.readResourceToString("api_keys.json");

        if (StringUtils.isNotBlank(json)) {
            props = JsonParser.parseString(json).getAsJsonObject();
        }
    }

    private void enableProxy() {
        FxPreferences.getInstance().savePreference("general.proxy.enable", "true");
    }

    private void disableProxy() {
        FxPreferences.getInstance().savePreference("general.proxy.enable", "false");
    }

    private String loadApiKey(String providerName) {
        if (props.has(providerName)) {
            return props.get(providerName).getAsJsonObject().get("apiKey").getAsString();
        }
        return SystemUtils.getEnvironmentVariable("API_KEY", "NONE");
    }

    @Test
    public void chatglm() {
        disableProxy();
        String apiKey = loadApiKey(CHAT_GLM.getName());
        ChatGlmProvider provider = new ChatGlmProvider(apiKey, "glm-4", false);
        String result = provider.predict("讲个笑话", 0.5f, new OutputParams(GenAiConstants.OutputAdjust.SHORTER, GenAiConstants.OutputFormat.TEXT));
        System.out.println(result);
    }

    @Test
    public void huggingface() {
        enableProxy();
        String apiKey = loadApiKey(HUGGING_FACE.getName());
        HuggingFaceProvider2 provider = new HuggingFaceProvider2(apiKey, "mistralai/Mistral-7B-Instruct-v0.2", true);
        String result = provider.predict("讲个笑话", 0.5f, new OutputParams(GenAiConstants.OutputAdjust.SHORTER, GenAiConstants.OutputFormat.TEXT));
        System.out.println(result);
    }

    @Test
    public void qwen() {
        disableProxy();
        String apiKey = loadApiKey(ALI_Q_WEN.getName());
        QwenProvider provider = new QwenProvider(apiKey, "qwen-turbo", false);
        String result = provider.predict("讲个笑话", 0.5f, new OutputParams(GenAiConstants.OutputAdjust.SHORTER, GenAiConstants.OutputFormat.TEXT));
        System.out.println(result);
    }
}
