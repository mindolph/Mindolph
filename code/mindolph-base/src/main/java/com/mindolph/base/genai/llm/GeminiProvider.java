package com.mindolph.base.genai.llm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
                "maxOutputTokens": 800,
                "topP": 0.8,
                "topK": 10
              }
            }
            """;


    public GeminiProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    public String predict(String input, float temperature, OutputParams outputParams) {
        RequestBody requestBody = super.createRequestBody(template, null, input, temperature, outputParams);
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s".formatted(aiModel, apiKey))
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
            JsonObject resBody = (JsonObject) JsonParser.parseString(resBodyInJson);
            String result = resBody.get("candidates").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("content").getAsJsonObject()
                    .get("parts").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
