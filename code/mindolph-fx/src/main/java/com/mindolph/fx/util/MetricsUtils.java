package com.mindolph.fx.util;

import com.mindolph.base.util.OkHttpUtils;
import com.mindolph.core.util.TemplateUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @since 1.14.1
 */
public class MetricsUtils {

    private static final Logger log = LoggerFactory.getLogger(MetricsUtils.class);

    public static void launch() {
        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            executorService.submit(() -> {
                log.debug("Try to send metrics..");
                String template = """
                            {
                                "api_key": "${api_key}",
                                "event": "${event_name}",
                                "distinct_id": "Anonymous",
                                "properties": {
                                  "$process_person_profile": false
                                }
                            }
                        """;
                String json = TemplateUtils.format(template,
                        new String[]{"api_key", "event_name"},
                        new String[]{"phc_BgtKQaRdRWsduqwaTabPFm7EDpq95CxbRQGpERYxbmMj", "app-launch"});
                log.debug(json);
                RequestBody body = RequestBody.create(json, OkHttpUtils.JSON);
                Request.Builder builder = new Request.Builder().url("https://us.i.posthog.com/i/v0/e/").post(body);
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(Duration.ofSeconds(30))
                        .readTimeout(Duration.ofMinutes(10))
                        .writeTimeout(Duration.ofMinutes(5))
                        .build();
                if (OkHttpUtils.execute(client, builder.build()) == null) {
                    log.warn("Failed to send metrics");
                    return;
                }
                log.info("Metrics sent");
            });
        }
    }
}
