package com.mindolph.base.genai.llm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.ServerSentEvent;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.mindolph.base.genai.llm.BaseApiLlmProvider.JSON;

/**
 * Adapter to OKHttpClient.
 *
 * @since 1.13.0
 * @see OkHttpClient
 */
public class OkHttpClientAdapter implements HttpClient {

    private static final Logger log = LoggerFactory.getLogger(OkHttpClientAdapter.class);

    private final OkHttpClient okHttpClient;

    public OkHttpClientAdapter(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Override
    public SuccessfulHttpResponse execute(HttpRequest httpRequest) throws HttpException, RuntimeException {
        Request request = createOkRequestFromLangchainRequest(httpRequest);
        log.debug(request.toString());
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Request failed, code: {}", response.code());
                throw new HttpException(response.code(), this.extractErrorMessageFromResponse(response));
            }
            SuccessfulHttpResponse.Builder respBuilder = SuccessfulHttpResponse.builder();
            respBuilder.statusCode(response.code());
            respBuilder.body(response.body().toString());
            respBuilder.headers(response.headers().toMultimap());
            return respBuilder.build();
        } catch (HttpException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Support OpenAI only api for now.
     *
     * @param response
     * @return
     */
    private String extractErrorMessageFromResponse(Response response) {
        if (response.isSuccessful() || response.body() == null ) {
            return response.message();
        }
        try {
            String jsonMessage = response.body().string();
            JsonElement bodyElement = JsonParser.parseString(jsonMessage);
            if (bodyElement.isJsonObject()) {
                JsonObject messageElement = bodyElement.getAsJsonObject().get("error").getAsJsonObject();
                return messageElement.get("message").getAsString().trim();
            }
            return response.message();
        } catch (IOException e) {
            return response.message();
        }
    }


    @Override
    public void execute(HttpRequest httpRequest, ServerSentEventParser serverSentEventParser, ServerSentEventListener serverSentEventListener) {
        EventSource.Factory factory = EventSources.createFactory(okHttpClient);
        Request okHttpRequest = createOkRequestFromLangchainRequest(httpRequest);
        EventSource eventSource = factory.newEventSource(okHttpRequest, new EventSourceListener() {
            @Override
            public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                log.debug("Open SSE connection");
            }

            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                if (log.isTraceEnabled()) log.trace("onEvent id:%s type:%s payload:%s%n".formatted(id, type, data));
                if (serverSentEventListener != null) {
                    try {
                        ServerSentEvent event = new ServerSentEvent(type, data);
                        serverSentEventListener.onEvent(event);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        eventSource.cancel(); // STOP the event source if exception is captured from callback. onFailure() will be called.
                    }
                }
            }

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                log.debug("SSE connection closed");
                if (serverSentEventListener != null) serverSentEventListener.onClose();
            }

            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                log.debug("SSE failure");
                if (response != null) {
                    try {
                        String resMsg = response.body().string();
                        if (StringUtils.isBlank(resMsg) && t != null) {
                            resMsg = t.getLocalizedMessage();
                        }
                        log.error("SSE failure with response: %s".formatted(resMsg));
                        if (serverSentEventListener != null) serverSentEventListener.onError(t);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    if (t == null) {
                        log.error("SSE failure without any information");
                        if (serverSentEventListener != null) serverSentEventListener.onError(t);
                    }
                    else {
                        log.error("SSE failure with exception", t);
                        if (serverSentEventListener != null) serverSentEventListener.onError(t);
                    }
                }
            }
        });
        eventSource.request();
    }


    @NotNull
    private static Request createOkRequestFromLangchainRequest(HttpRequest httpRequest) {
        RequestBody requestBody = RequestBody.create(httpRequest.body(), JSON);
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(httpRequest.url());
        for (String key : httpRequest.headers().keySet()) {
            List<String> headers = httpRequest.headers().get(key);
            for (String header : headers) {
                requestBuilder.header(key, header);
            }
        }
        requestBuilder.post(requestBody);
        return requestBuilder.build();
    }
}
