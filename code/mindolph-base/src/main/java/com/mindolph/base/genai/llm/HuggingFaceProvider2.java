package com.mindolph.base.genai.llm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mindolph.base.util.OkHttpUtils;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
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
                    "max_new_tokens": 1024,
                    "max_time": 60,
                    "top_k": 10,
                    "top_p": 0.8
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
                    "max_new_tokens": 1024,
                    "max_time": 60,
                    "top_k": 10,
                    "top_p": 0.8
                }
            }
            """;

    public HuggingFaceProvider2(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    public String predict(String input, float temperature, OutputParams outputParams) {
        RequestBody requestBody = super.createRequestBody(template, null, input, temperature, outputParams);
        Request request = new Request.Builder()
                .url(API_URL.formatted(aiModel))
                .header("Authorization", "Bearer %s".formatted(apiKey))
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
            return resArray.get(0).getAsJsonObject().get("generated_text").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stream(String input, float temperature, OutputParams outputParams, Consumer<StreamToken> consumer) {
        RequestBody requestBody = super.createRequestBody(streamTemplate, null, input, temperature, outputParams);
        Request request = new Request.Builder()
                .url(API_URL.formatted(aiModel))
                .header("Authorization", "Bearer %s".formatted(apiKey))
                .header("x-wait-for-model", "true")
                .post(requestBody)
                .build();
        OkHttpUtils.sse(client, request, (Consumer<String>) data -> {
            log.debug(data);
            if (log.isTraceEnabled()) log.trace(data);
            JsonObject resBody = new Gson().fromJson(data, JsonObject.class);
            JsonObject candidate = resBody.get("token").getAsJsonObject();
            String result = candidate.get("text").getAsString();
            boolean isStop = candidate.get("special").getAsBoolean();
            if (isStop) {
                consumer.accept(new StreamToken(StringUtils.EMPTY, true, false));
            }
            else {
                consumer.accept(new StreamToken(result, false, false));
            }
        }, (msg, throwable) -> {
            log.error(msg, throwable);
            log.error("huggingface api response error", throwable);
            consumer.accept(new StreamToken(JsonParser.parseString(msg).getAsJsonObject().get("error").getAsString(), true, true));
        }, () -> {
            consumer.accept(new StreamToken(StringUtils.EMPTY, true, false));
        });
    }
}
