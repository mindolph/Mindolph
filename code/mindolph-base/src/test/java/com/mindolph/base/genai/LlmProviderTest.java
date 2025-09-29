package com.mindolph.base.genai;

import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.llm.*;
import com.mindolph.core.constant.GenAiConstants.OutputAdjust;
import com.mindolph.core.constant.GenAiConstants.OutputFormat;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import org.junit.jupiter.api.Test;

import static com.mindolph.core.constant.GenAiModelProvider.*;

/**
 * @since 1.7.5
 */
public class LlmProviderTest extends BaseLlmTest {

    private final Input testInput = new InputBuilder().text("讲个笑话").temperature(0.5f).maxTokens(512).outputAdjust(OutputAdjust.SHORTER).isRetry(false).isStreaming(false).createInput();


    @Test
    public void openai() {
        enableProxy();
        ProviderMeta providerMeta = loadProviderMeta(CHAT_GLM.name(), false);
        ModelMeta modelMeta = loadModelMeta("non-exist");
        OpenAiProvider provider = new OpenAiProvider(providerMeta, modelMeta);
        StreamPartial predict = provider.predict(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT));
        System.out.println(predict);
    }

    @Test
    public void chatglm() {
        disableProxy();
        ProviderMeta providerMeta = loadProviderMeta(CHAT_GLM.name(), false);
        ModelMeta modelMeta = loadModelMeta("glm-4");
        ChatGlmProvider provider = new ChatGlmProvider(providerMeta, modelMeta);
        StreamPartial predict = provider.predict(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT));
        System.out.println(predict);
    }

    @Test
    public void chatglmStream() {
        disableProxy();
        ProviderMeta providerMeta = loadProviderMeta(CHAT_GLM.name(), false);
        ModelMeta modelMeta = loadModelMeta("glm-4");
        ChatGlmProvider provider = new ChatGlmProvider(providerMeta, modelMeta);
        provider.stream(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT), streamToken -> {
            System.out.println("result: " + streamToken.text());
        });
        waitUntilStreamDone(20);
    }

    @Test
    public void huggingface() {
        enableProxy();
        ProviderMeta providerMeta = loadProviderMeta(HUGGING_FACE.name(), false);
        ModelMeta modelMeta = loadModelMeta("mistralai/Mistral-7B-Instruct-v0.2");
        HuggingFaceProvider2 provider = new HuggingFaceProvider2(providerMeta, modelMeta);
        StreamPartial predict = provider.predict(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT));
        System.out.println(predict);
    }

    @Test
    public void huggingfaceStream() {
        enableProxy();
        ProviderMeta providerMeta = loadProviderMeta(HUGGING_FACE.name(), false);
        ModelMeta modelMeta = loadModelMeta("mistralai/Mistral-7B-Instruct-v0.2");
        HuggingFaceProvider2 provider = new HuggingFaceProvider2(providerMeta, modelMeta);
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
        ProviderMeta providerMeta = loadProviderMeta(ALI_Q_WEN.name(), false);
        ModelMeta modelMeta = loadModelMeta("qwen-turbo");
        QwenProvider provider = new QwenProvider(providerMeta, modelMeta);

        StreamPartial predict = provider.predict(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT));
        System.out.println(predict);
    }

    @Test
    public void qwenStream() {
        disableProxy();
        ProviderMeta providerMeta = loadProviderMeta(ALI_Q_WEN.name(), false);
        ModelMeta modelMeta = loadModelMeta("qwen-turbo");
        QwenProvider provider = new QwenProvider(providerMeta, modelMeta);
        provider.stream(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT),
                streamToken -> System.out.println(streamToken.text()));
        waitUntilStreamDone(20);
    }

    @Test
    public void geminiStream() {
        enableProxy();
        ProviderMeta providerMeta = loadProviderMeta(GEMINI.name(), false);
        System.out.println(providerMeta);
        ModelMeta modelMeta = loadModelMeta("gemini-pro");
        GeminiProvider geminiProvider = new GeminiProvider(providerMeta, modelMeta);
        geminiProvider.stream(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT), streamToken -> {
            System.out.println(streamToken.text());
        });
        waitUntilStreamDone(30);
    }

    @Test
    public void deepSeek() {
        disableProxy();
        ProviderMeta providerMeta = loadProviderMeta(DEEP_SEEK.name(), false);
        ModelMeta modelMeta = loadModelMeta("deepseek-chat");
        DeepSeekProvider provider = new DeepSeekProvider(providerMeta, modelMeta);
        StreamPartial predict = provider.predict(testInput, new OutputParams(OutputAdjust.SHORTER, OutputFormat.TEXT));
        System.out.println(predict);
    }
}
