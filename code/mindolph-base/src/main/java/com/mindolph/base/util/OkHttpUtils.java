package com.mindolph.base.util;

import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Invoke http endpoint by OkHTTP client, including SSE support.
 *
 * @since 1.11
 */
public class OkHttpUtils {

    private static final Logger log = LoggerFactory.getLogger(OkHttpUtils.class);

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    /**
     * Execute a GET request and return the response body as a string.
     *
     * @param client  the OkHttpClient instance
     * @param url     the request URL
     * @param headers optional headers
     * @return the response body string, or null if the request failed
     */
    public static String get(OkHttpClient client, String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url).get();
        if (headers != null) {
            headers.forEach(builder::header);
        }
        return execute(client, builder.build());
    }

    /**
     * Execute a GET request without extra headers.
     */
    public static String get(OkHttpClient client, String url) {
        return get(client, url, null);
    }

    /**
     * Execute a POST request with a JSON body.
     *
     * @param client  the OkHttpClient instance
     * @param url     the request URL
     * @param json    the JSON body string
     * @param headers optional headers
     * @return the response body string, or null if the request failed
     */
    public static String post(OkHttpClient client, String url, String json, Map<String, String> headers) {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder builder = new Request.Builder().url(url).post(body);
        if (headers != null) {
            headers.forEach(builder::header);
        }
        return execute(client, builder.build());
    }

    /**
     * Execute a POST request with a JSON body and no extra headers.
     */
    public static String post(OkHttpClient client, String url, String json) {
        return post(client, url, json, null);
    }

    /**
     * Execute a POST request with form data.
     *
     * @param client  the OkHttpClient instance
     * @param url     the request URL
     * @param form    the form parameters
     * @param headers optional headers
     * @return the response body string, or null if the request failed
     */
    public static String postForm(OkHttpClient client, String url, Map<String, String> form, Map<String, String> headers) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (form != null) {
            form.forEach(formBuilder::add);
        }
        Request.Builder builder = new Request.Builder().url(url).post(formBuilder.build());
        if (headers != null) {
            headers.forEach(builder::header);
        }
        return execute(client, builder.build());
    }

    /**
     * Execute a POST request with form data and no extra headers.
     */
    public static String postForm(OkHttpClient client, String url, Map<String, String> form) {
        return postForm(client, url, form, null);
    }

    /**
     * Execute a PUT request with a JSON body.
     *
     * @param client  the OkHttpClient instance
     * @param url     the request URL
     * @param json    the JSON body string
     * @param headers optional headers
     * @return the response body string, or null if the request failed
     */
    public static String put(OkHttpClient client, String url, String json, Map<String, String> headers) {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder builder = new Request.Builder().url(url).put(body);
        if (headers != null) {
            headers.forEach(builder::header);
        }
        return execute(client, builder.build());
    }

    /**
     * Execute a PUT request with a JSON body and no extra headers.
     */
    public static String put(OkHttpClient client, String url, String json) {
        return put(client, url, json, null);
    }

    /**
     * Execute a DELETE request.
     *
     * @param client  the OkHttpClient instance
     * @param url     the request URL
     * @param headers optional headers
     * @return the response body string, or null if the request failed
     */
    public static String delete(OkHttpClient client, String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url).delete();
        if (headers != null) {
            headers.forEach(builder::header);
        }
        return execute(client, builder.build());
    }

    /**
     * Execute a DELETE request without extra headers.
     */
    public static String delete(OkHttpClient client, String url) {
        return delete(client, url, null);
    }

    /**
     * Execute a request and return the response body as a string.
     *
     * @param client  the OkHttpClient instance
     * @param request the prepared request
     * @return the response body string, or null if the request failed
     */
    public static String execute(OkHttpClient client, Request request) {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Request failed: {} {}", response.code(), response.message());
                log.debug(response.body() != null ? response.body().string(): null);
                return null;
            }
            return response.body() != null ? response.body().string() : null;
        } catch (IOException e) {
            log.error("Request execution failed", e);
            return null;
        }
    }

    /**
     * Execute a request and return the raw Response object.
     * <p>
     * The caller is responsible for closing the response body.
     *
     * @param client  the OkHttpClient instance
     * @param request the prepared request
     * @return the Response object, or null if the request failed
     */
    public static Response executeRaw(OkHttpClient client, Request request) {
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            log.error("Request execution failed", e);
            return null;
        }
    }

    /**
     * @param client
     * @param request
     * @param dataConsumer  consuming data until exception occurred.
     * @param errorConsumer
     * @param <T>
     */
    public static <T> void sse(OkHttpClient client, Request request,
                               Consumer<T> dataConsumer, BiConsumer<String, Throwable> errorConsumer) {
        sse(client, request, dataConsumer, errorConsumer, null);
    }

    /**
     * @param client
     * @param request
     * @param dataConsumer  consuming data until exception occurred.
     * @param errorConsumer
     * @param onComplete    be called when completed (in case there is no specific indication on data event raised)
     * @param <T>
     * @return EventSource
     */
    public static <T> EventSource sse(OkHttpClient client, Request request,
                                      Consumer<T> dataConsumer, BiConsumer<String, Throwable> errorConsumer, Runnable onComplete) {
        EventSource.Factory factory = EventSources.createFactory(client);
        EventSource eventSource = factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                log.debug("Open SSE connection");
            }

            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                if (log.isTraceEnabled()) log.trace("onEvent id:%s type:%s payload:%s%n".formatted(id, type, data));
                if (dataConsumer != null) {
                    try {
                        dataConsumer.accept((T) data);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        eventSource.cancel(); // STOP the event source if exception is captured from callback. onFailure() will be called.
                    }
                }
            }

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                log.debug("SSE connection closed");
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                log.debug("SSE failure");
                if (t != null) log.error(t.getLocalizedMessage(), t);
                String resMsg = "ERROR";
                if (response != null) {
                    try {
                        if (response.isSuccessful()) {
                            log.warn("Weired, fail but still returns successful response?");
                        }
                        else {
                            log.error("SSE failure: %s".formatted(response.code()));
                            // JSON or plain text
                            resMsg = response.body().string();
                            if (StringUtils.isBlank(resMsg)) {
                                if (t != null) {
                                    // just in case.
                                    resMsg = t.getLocalizedMessage();
                                }
                                else {
                                    resMsg = response.message();
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    if (t == null) {
                        log.error("SSE failure without any error information");
                    }
                    else {
                        log.error("SSE failure with exception", t);
                        resMsg = "Call API fail: " + t.getLocalizedMessage();
                    }
                }
                log.debug(resMsg);
                if (errorConsumer != null) errorConsumer.accept(resMsg, t);
            }
        });
        eventSource.request();
        return eventSource;
    }
}
