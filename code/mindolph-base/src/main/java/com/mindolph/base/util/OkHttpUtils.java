package com.mindolph.base.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Invoke http SSE endpoint by OkHTTP client.
 *
 * @since 1.11
 */
public class OkHttpUtils {

    private static final Logger log = LoggerFactory.getLogger(OkHttpUtils.class);

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
