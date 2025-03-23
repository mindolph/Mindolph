package com.mindolph.base.genai.llm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.util.OkHttpUtils;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * @since 1.7.5
 */
public class ChatGlmProvider extends BaseOpenAiLikeApiLlmProvider {

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
                "max_tokens": %d
            }
            """;

    String streamTemplate = """
            {
                "model": "%s",
                "messages": [
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ],
                "stream": true,
                "temperature": %s,
                "top_p": 0.8,
                "max_tokens": %d
            }
            """;

    public ChatGlmProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    @Override
    public void stream(Input input, OutputParams outputParams, Consumer<StreamToken> consumer) {
        RequestBody requestBody = super.createRequestBody(streamTemplate, determineModel(input), input, outputParams);
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer %s".formatted(apiKey))
                .post(requestBody)
                .build();
        streamEventSource = OkHttpUtils.sse(client, request, (Consumer<String>) data -> {
            log.debug(data);
            JsonObject resObject = JsonParser.parseString(data).getAsJsonObject();
            JsonObject choices = resObject.get("choices").getAsJsonArray().get(0).getAsJsonObject();
            String result = choices
                    .get("message").getAsJsonObject()
                    .get("content").getAsString();
            boolean isStop = determineStreamStop(choices, "finish_reason");
            if (isStop) {
                // TODO count actual output tokens
                consumer.accept(new StreamToken(StringUtils.EMPTY, 0, true, false));
            }
            else {
                consumer.accept(new StreamToken(result, false, false));
            }
        }, (msg, throwable) -> {
            log.error(msg, throwable);
            JsonElement jsonElement = JsonParser.parseString(msg);
            if (jsonElement.isJsonObject()) {
                JsonObject asJsonObject = jsonElement.getAsJsonObject();
                consumer.accept(new StreamToken(asJsonObject.get("error").getAsJsonObject().get("message").getAsString(), true, true));
            }
            else {
                log.debug(jsonElement.toString());
            }
        }, () -> {
//            log.info("completed");
//            consumer.accept(new StreamToken(StringUtils.EMPTY, true, false));
        });
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
