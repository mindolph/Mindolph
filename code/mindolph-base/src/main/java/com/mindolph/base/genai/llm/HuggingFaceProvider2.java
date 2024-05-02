package com.mindolph.base.genai.llm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public HuggingFaceProvider2(String apiKey, String aiModel) {
        super(apiKey, aiModel);
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
            String result = resArray.get(0).getAsJsonObject().get("generated_text").getAsString();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
