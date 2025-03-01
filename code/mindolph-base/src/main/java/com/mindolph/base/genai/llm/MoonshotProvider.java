package com.mindolph.base.genai.llm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mindolph.base.genai.GenAiEvents;
import com.mindolph.base.util.OkHttpUtils;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * <a href="https://platform.moonshot.cn/docs/guide/start-using-kimi-api">...</a>
 *
 * @since 1.12
 */
public class MoonshotProvider extends BaseOpenAiLikeApiLlmProvider {
    private static final Logger log = LoggerFactory.getLogger(MoonshotProvider.class);

    String API_URL = "https://api.moonshot.cn/v1/chat/completions";

    String template = """
            {
                "model": "%s",
                "messages": [
                    {"role": "system", "content": "You are a helpful assistant."},
                    {"role": "user", "content": "%s"}
                ],
                "stream": false,
                "temperature": %s,
                "top_p": 0.8
            }
            """; // max token can not post to api

    String streamTemplate = """
            {
                "model": "%s",
                "messages": [
                    {"role": "system", "content": "You are a helpful assistant."},
                    {"role": "user", "content": "%s"}
                ],
                "stream": true,
                "temperature": %s,
                "top_p": 0.8,
                "include_usage": true
            }
            """;// max token can not post to api

    public MoonshotProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
    }

    /**
     * Moonshot's streaming api is different from OpenAI api that the 'usage' field is returned with the 'choices' node instead of the root node.
     * (the error message is also different)
     * @param input
     * @param outputParams
     * @param consumer
     */
    @Override
    public void stream(GenAiEvents.Input input, OutputParams outputParams, Consumer<StreamToken> consumer) {
        RequestBody requestBody = super.createRequestBody(streamTemplate, determineModel(input), input, outputParams);
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer %s".formatted(apiKey))
                .header("x-wait-for-model", "true")
                .post(requestBody)
                .build();
        AtomicInteger outputTokens = new AtomicInteger();
        OkHttpUtils.sse(client, request, (Consumer<String>) data -> {
            if (log.isTraceEnabled()) log.trace(data);
            if (StringUtils.isBlank(data) || "[DONE]".equals(data)) {
                return;
            }
            JsonObject resObject = JsonParser.parseString(data).getAsJsonObject();
            JsonObject choices = resObject.get("choices").getAsJsonArray().get(0).getAsJsonObject();

            boolean isStop = super.determineStreamStop(choices, "finish_reason");
            if (isStop) {
                outputTokens.set(choices.get("usage").getAsJsonObject().get("completion_tokens").getAsInt());
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
            log.error("moonshot api response error", throwable);
            String message = "ERROR";
            if (StringUtils.isNotBlank(msg)) {
                try {
                    message = JsonParser.parseString(msg).getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString();
                } catch (JsonSyntaxException e) {
                    log.warn("Not exception from Moonshot API: " + e.getLocalizedMessage());
                    // skip parsing exception
                    message = msg;
                }
            }
            consumer.accept(new StreamToken(message, true, true));
        }, () -> {
//            consumer.accept(new StreamToken(StringUtils.EMPTY, outputTokens.get(), true, false));
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
