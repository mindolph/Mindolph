package com.mindolph.base.genai.llm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mindolph.base.genai.GenAiEvents;
import com.mindolph.base.util.OkHttpUtils;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @since 1.11
 */
public class DeepSeekProvider extends BaseApiLlmProvider {
    private static final Logger log = LoggerFactory.getLogger(DeepSeekProvider.class);

    String API_URL = "https://api.deepseek.com/chat/completions";

    String template = """
            {
                "model": "deepseek-chat",
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
                "model": "deepseek-chat",
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
    public StreamToken predict(GenAiEvents.Input input, OutputParams outputParams) {
        RequestBody requestBody = super.createRequestBody(template, null, input, outputParams);
        Request request = new Request.Builder()
                .url(API_URL.formatted(determineModel(input)))
                .header("Authorization", "Bearer %s".formatted(apiKey))
                .header("x-wait-for-model", "true")
                .post(requestBody)
                .build();
        log.debug(request.toString());
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String strBody = response.body().string();
                log.debug(strBody);
                JsonObject respBody = JsonParser.parseString(strBody).getAsJsonObject();
                throw new RuntimeException("%d %s".formatted(response.code(),
                        respBody.get("error").getAsJsonObject().get("message").getAsString()));
            }
            String resBodyInJson = response.body().string();
            JsonObject resObject = JsonParser.parseString(resBodyInJson).getAsJsonObject();
            String result = resObject.get("choices").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("message").getAsJsonObject()
                    .get("content").getAsString();
            int outputTokens = resObject.get("usage").getAsJsonObject().get("completion_tokens").getAsInt();
            return new StreamToken(result, outputTokens, true, false);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stream(GenAiEvents.Input input, OutputParams outputParams, Consumer<StreamToken> consumer) {
        RequestBody requestBody = super.createRequestBody(streamTemplate, null, input, outputParams);
        Request request = new Request.Builder()
                .url(API_URL.formatted(determineModel(input)))
                .header("Authorization", "Bearer %s".formatted(apiKey))
                .header("x-wait-for-model", "true")
                .post(requestBody)
                .build();
        AtomicInteger outputTokens = new AtomicInteger();
        OkHttpUtils.sse(client, request, (Consumer<String>) data -> {
            if (log.isTraceEnabled()) log.trace(data);
            if ("[DONE]".equals(data)) {
                return;
            }
            JsonObject resObject = JsonParser.parseString(data).getAsJsonObject();
            JsonObject choices = resObject.get("choices").getAsJsonArray().get(0).getAsJsonObject();
            String result = choices
                    .get("delta").getAsJsonObject()
                    .get("content").getAsString();

            boolean isStop = super.determineStreamStop(choices, "finish_reason");
            if (isStop) {
                outputTokens.set(resObject.get("usage").getAsJsonObject().get("completion_tokens").getAsInt());
                consumer.accept(new StreamToken(StringUtils.EMPTY, outputTokens.get(), true, false));
            }
            else {
                consumer.accept(new StreamToken(result, false, false));
            }
        }, (msg, throwable) -> {
//            log.error(msg, throwable);
            log.error("deepseek api response error", throwable);
            String message = "ERROR";
            if (StringUtils.isNotBlank(msg)) {
                try {
                    message = JsonParser.parseString(msg).getAsJsonObject().get("error").getAsString();
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
}
