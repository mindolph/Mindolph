package com.mindolph.base.genai.llm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mindolph.base.genai.GenAiEvents;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 1.11.3
 */
public abstract class BaseOpenAiLikeApiLlmProvider extends BaseApiLlmProvider {

    private static final Logger log = LoggerFactory.getLogger(BaseOpenAiLikeApiLlmProvider.class);


    public BaseOpenAiLikeApiLlmProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    public StreamToken predict(GenAiEvents.Input input, OutputParams outputParams) {
        RequestBody requestBody = super.createRequestBody(predictPromptTemplate(), determineModel(input), input, outputParams);
        Request request = new Request.Builder()
                .url(apiUrl())
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

}
