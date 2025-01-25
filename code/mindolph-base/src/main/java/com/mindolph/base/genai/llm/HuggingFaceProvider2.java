package com.mindolph.base.genai.llm;

import com.google.gson.*;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.util.OkHttpUtils;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * https://huggingface.co/docs/api-inference/en/index
 *
 * @author mindolph.com@gmail.com
 * @since 1.7.4
 */
public class HuggingFaceProvider2 extends BaseApiLlmProvider {
    private static final Logger log = LoggerFactory.getLogger(HuggingFaceProvider2.class);

    String API_URL = "https://api-inference.huggingface.co/models/%s";

    String template = """
            {
                "inputs": "%s",
                "parameters":{
                    "temperature": %s,
                    "max_new_tokens": %d,
                    "max_time": 60,
                    "top_k": 10,
                    "top_p": 0.8,
                    "details": true
                },
                "options": {
                    "wait_for_model":true
                }
            }
            """;

    String streamTemplate = """
            {
                "inputs": "%s",
                "stream": true,
                "parameters":{
                    "temperature": %s,
                    "max_new_tokens": %s,
                    "max_time": 60,
                    "top_k": 10,
                    "top_p": 0.8
                },
                "options": {
                    "wait_for_model":true
                }
            }
            """;

    public HuggingFaceProvider2(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    public StreamToken predict(Input input, OutputParams outputParams) {
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
                throw new RuntimeException("%d %s".formatted(response.code(), respBody.get("error").getAsString()));
            }
            String resBodyInJson = response.body().string();
            JsonArray resArray = JsonParser.parseString(resBodyInJson).getAsJsonArray();
            JsonObject resJson = resArray.get(0).getAsJsonObject();
            String result = resJson.get("generated_text").getAsString();
            int outputTokens = resJson.get("details").getAsJsonObject().get("generated_tokens").getAsInt();
            return new StreamToken(result, outputTokens, true, false);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stream(Input input, OutputParams outputParams, Consumer<StreamToken> consumer) {
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
            JsonObject resBody = new Gson().fromJson(data, JsonObject.class);
            JsonObject candidate = resBody.get("token").getAsJsonObject();
            String result = candidate.get("text").getAsString();
            boolean isStop = candidate.get("special").getAsBoolean();
            outputTokens.set(resBody.get("index").getAsInt());
            if (isStop) {
                consumer.accept(new StreamToken(StringUtils.EMPTY, true, false));
            }
            else {
                consumer.accept(new StreamToken(result, false, false));
            }
        }, (msg, throwable) -> {
//            log.error(msg, throwable);
            log.error("huggingface api response error", throwable);
            String message = "ERROR";
            if (StringUtils.isNotBlank(msg)) {
                try {
                    message = JsonParser.parseString(msg).getAsJsonObject().get("error").getAsString();
                } catch (JsonSyntaxException e) {
                    log.warn("Not exception from HuggingFace API: " + e.getLocalizedMessage());
                    // skip parsing exception
                    message = msg;
                }
            }
            consumer.accept(new StreamToken(message, true, true));
        }, () -> {
            consumer.accept(new StreamToken(StringUtils.EMPTY, outputTokens.get(), true, false));
        });
    }

}
