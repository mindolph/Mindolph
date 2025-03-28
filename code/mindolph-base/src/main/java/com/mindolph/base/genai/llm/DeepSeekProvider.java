package com.mindolph.base.genai.llm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.util.OkHttpUtils;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @since 1.11
 */
public class DeepSeekProvider extends BaseOpenAiLikeApiLlmProvider {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekProvider.class);

    String API_URL = "https://api.deepseek.com/chat/completions";

    String template = """
            {
                "model": "%s",
                "messages": [
                    {"role": "system", "content": "You are a helpful assistant."},
                    {"role": "user", "content": "%s"}
                ],
                "stream": false,
                "temperature": %s,
                "max_tokens": %d,
                "top_p": 0.8
            }
            """;

    String streamTemplate = """
            {
                "model": "%s",
                "messages": [
                    {"role": "system", "content": "You are a helpful assistant."},
                    {"role": "user", "content": "%s"}
                ],
                "stream": true,
                "temperature": %s,
                "max_tokens": %d,
                "top_p": 0.8
            }
            """;

    public DeepSeekProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    public void stream(Input input, OutputParams outputParams, Consumer<StreamToken> consumer) {
        RequestBody requestBody = super.createRequestBody(streamTemplate, determineModel(input), input, outputParams);
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer %s".formatted(apiKey))
                .header("x-wait-for-model", "true")
                .post(requestBody)
                .build();
        AtomicInteger outputTokens = new AtomicInteger();
        streamEventSource = OkHttpUtils.sse(client, request, (Consumer<String>) data -> {
            if (log.isTraceEnabled()) log.trace(data);
            if ("[DONE]".equals(data)) {
                return;
            }
            JsonObject resObject = JsonParser.parseString(data).getAsJsonObject();
            JsonObject choices = resObject.get("choices").getAsJsonArray().get(0).getAsJsonObject();

            boolean isStop = super.determineStreamStop(choices, "finish_reason");
            if (isStop) {
                outputTokens.set(resObject.get("usage").getAsJsonObject().get("completion_tokens").getAsInt());
                consumer.accept(new StreamToken(StringUtils.EMPTY, outputTokens.get(), true, false));
            }
            else {
                String result = choices
                        .get("delta").getAsJsonObject()
                        .get("content").getAsString();
                consumer.accept(new StreamToken(result, false, false));
            }
        }, (msg, throwable) -> {
//            log.error(msg, throwable);
            log.error("DeepSeek api response error", throwable);
            String message = "ERROR";
            if (StringUtils.isNotBlank(msg)) {
                try {
                    message = JsonParser.parseString(msg).getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString();
                } catch (JsonSyntaxException e) {
                    log.warn("Not exception from DeepSeek API: " + e.getLocalizedMessage());
                    // skip parsing exception
                    message = msg;
                }
            }
            consumer.accept(new StreamToken(message, true, true));
        }, () -> {
//            consumer.accept(new StreamToken(StringUtils.EMPTY, outputTokens.get(), true, false));
        });
    }

    protected List<String> getFinishReasons() {
        return List.of(new String[]{"stop", "length", "content_filter", "tool_calls", "insufficient_system_resource"});
    }

    @Override
    protected String apiUrl() {
        return API_URL;
    }

    @Override
    protected String predictPromptTemplate() {
        return template;
    }

    @Override
    protected String streamPromptTemplate() {
        return streamTemplate;
    }
}
