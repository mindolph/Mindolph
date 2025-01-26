package com.mindolph.base.genai.llm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.util.OkHttpUtils;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7.4
 */
public class GeminiProvider extends BaseApiLlmProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiProvider.class);

    String template = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "%s"
                    }
                  ]
                }
              ],
              "safetySettings": [
                {
                  "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                  "threshold": "BLOCK_ONLY_HIGH"
                }
              ],
              "generationConfig": {
                "stopSequences": [
                  "Title"
                ],
                "temperature": %s,
                "maxOutputTokens": %d,
                "topP": 0.8,
                "topK": 10
              }
            }
            """;

    String streamTemplate = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "%s"
                    }
                  ]
                }
              ],
              "generationConfig": {
                "stopSequences": [
                  "Title"
                ],
                "temperature": %s,
                "maxOutputTokens": %d,
                "topP": 0.8,
                "topK": 10
              }
            }
            """;

    public GeminiProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    public StreamToken predict(Input input, OutputParams outputParams) {
        RequestBody requestBody = super.createRequestBody(template, null, input, outputParams);
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s".formatted(determineModel(input), apiKey))
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error(response.code() + response.message());
                String strBody = response.body().string();
                log.error(strBody);
                JsonObject respBody = JsonParser.parseString(strBody).getAsJsonObject();
                throw new RuntimeException("%d %s".formatted(response.code(),
                        respBody.get("error").getAsJsonObject().get("message").getAsString()));
            }

            String resBodyInJson = response.body().string();
            log.trace(resBodyInJson);
            JsonObject resBody = (JsonObject) JsonParser.parseString(resBodyInJson);
            String result = resBody.get("candidates").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("content").getAsJsonObject()
                    .get("parts").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            int outputTokens = resBody.get("usageMetadata").getAsJsonObject().get("candidatesTokenCount").getAsInt();
            return new StreamToken(result, outputTokens, true, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stream(Input input, OutputParams outputParams, Consumer<StreamToken> consumer) {
        RequestBody requestBody = super.createRequestBody(streamTemplate, null, input, outputParams);
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/%s:streamGenerateContent?alt=sse&key=%s".formatted(determineModel(input), apiKey))
                .post(requestBody)
                .build();
        log.info("Generate content screamingly by model %s ...".formatted(determineModel(input)));
        if (log.isTraceEnabled()) log.trace(request.url().toString());

        OkHttpUtils.sse(client, request,
                (Consumer<String>) data -> {
                    JsonObject resBody = new Gson().fromJson(data, JsonObject.class);
                    JsonObject candidate = resBody.get("candidates").getAsJsonArray().get(0).getAsJsonObject();
                    String result = candidate
                            .get("content").getAsJsonObject()
                            .get("parts").getAsJsonArray()
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                    boolean isStop = candidate.has("finishReason") && "STOP".equals(candidate.get("finishReason").getAsString());
                    int outputTokens = 0;
                    if (isStop) {
                        outputTokens = resBody.get("usageMetadata").getAsJsonObject().get("candidatesTokenCount").getAsInt();
                        log.debug("outputTokens: {}", outputTokens);
                    }
                    consumer.accept(new StreamToken(result, outputTokens, isStop, false));
                }, (msg, throwable) -> {
                    String message = "ERROR";
                    if (StringUtils.isNotBlank(msg)) {
                        try {
                            message = JsonParser.parseString(msg).getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString();
                        } catch (JsonSyntaxException e) {
                            log.warn("Not exception from Gemini API", e);
                            // skip exception
                            message = msg;
                        }
                    }
                    log.error(message, throwable);
                    consumer.accept(new StreamToken(message, true, true));
                }, () -> {
                    // NO NEED
                });
    }
}
