package com.mindolph.base.genai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.llm.*;
import com.mindolph.core.constant.GenAiConstants.OutputAdjust;
import com.mindolph.core.constant.GenAiConstants.OutputFormat;
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

    private final Input testInput = new InputBuilder().text("讲个笑话").temperature(0.5f).maxTokens(512).outputAdjust(OutputAdjust.SHORTER).isRetry(false).isStreaming(false).createInput();

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
        StreamToken predict = provider.predict(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT));
        System.out.println(predict);
    }

    @Test
    public void chatglmStream() {
        disableProxy();
        String apiKey = loadApiKey(CHAT_GLM.getName());
        ChatGlmProvider provider = new ChatGlmProvider(apiKey, "glm-4", false);
        provider.stream(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT), streamToken -> {
            System.out.println("result: " + streamToken.text());
        });
        waitUntilStreamDone(20);
    }

    @Test
    public void huggingface() {
        enableProxy();
        String apiKey = loadApiKey(HUGGING_FACE.getName());
        HuggingFaceProvider2 provider = new HuggingFaceProvider2(apiKey, "mistralai/Mistral-7B-Instruct-v0.2", true);
        StreamToken predict = provider.predict(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT));
        System.out.println(predict);
    }

    @Test
    public void huggingfaceStream() {
        enableProxy();
        String apiKey = loadApiKey(HUGGING_FACE.getName());
        HuggingFaceProvider2 provider = new HuggingFaceProvider2(apiKey, "mistralai/Mistral-7B-Instruct-v0.2", true);
        provider.stream(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT), streamToken -> {
            System.out.println(streamToken.text());
        });
        waitUntilStreamDone(20);
    }

    private static void waitUntilStreamDone(int x) {
        try {
            Thread.sleep(x * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void qwen() {
        disableProxy();
        String apiKey = loadApiKey(ALI_Q_WEN.getName());
        QwenProvider provider = new QwenProvider(apiKey, "qwen-turbo", false);
        StreamToken predict = provider.predict(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT));
        System.out.println(predict);
    }

    @Test
    public void qwenStream() {
        disableProxy();
        String apiKey = loadApiKey(ALI_Q_WEN.getName());
        QwenProvider provider = new QwenProvider(apiKey, "qwen-turbo", false);
        provider.stream(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT),
                streamToken -> System.out.println(streamToken.text()));
        waitUntilStreamDone(20);
    }

    @Test
    public void geminiStream() {
        enableProxy();
        String apiKey = loadApiKey(GEMINI.getName());
        System.out.println(apiKey);
        GeminiProvider geminiProvider = new GeminiProvider(apiKey, "gemini-pro", true);
        geminiProvider.stream(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT), streamToken -> {
            System.out.println(streamToken.text());
        });
        waitUntilStreamDone(30);
    }

    @Test
    public void deepSeek() {
        disableProxy();
        String apiKey = loadApiKey(DEEP_SEEK.getName());
        DeepSeekProvider provider = new DeepSeekProvider(apiKey, "deepseek-chat", false);
        StreamToken predict = provider.predict(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT));
        System.out.println(predict);
    }
}
