package com.mindolph.base.genai.llm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 1.7.5
 */
public class ChatGlmProvider extends BaseApiLlmProvider {
    private static final Logger log = LoggerFactory.getLogger(ChatGlmProvider.class);

    String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    String template = """
            {
                "model": "%s",
                "messages": [
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ],
                "temperature": %s,
                "top_p": 0.8,
                "max_tokens": 1024
            }
            """;

    public ChatGlmProvider(String apiKey, String aiModel) {
        super(apiKey, aiModel);
    }

    @Override
    public String predict(String input, float temperature, OutputParams outputParams) {
        RequestBody requestBody = super.createRequestBody(template, aiModel, input, temperature, outputParams);
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer %s".formatted(apiKey))
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
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
