package com.mindolph.fx.util;

import com.mindolph.base.util.OkHttpUtils;
import com.mindolph.core.util.TemplateUtils;
import com.mindolph.mfx.preference.FxPreferences;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.IdUtils;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mindolph.base.constant.PrefConstants.USER_METRICS_ID;

/**
 * @since 1.14.1
 */
public class MetricsUtils {

    private static final Logger log = LoggerFactory.getLogger(MetricsUtils.class);

    public static void launch() {
        String metricsId = FxPreferences.getInstance().getPreferenceSave(USER_METRICS_ID, IdUtils.makeUUID());
        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            executorService.submit(() -> {
                log.info("Try to send metrics..");
                String template = """
                            {
                                "api_key": "${api_key}",
                                "event": "${event_name}",
                                "distinct_id": "${metrics_id}",
                                "properties": {
                                  "$process_person_profile": false,
                                  "os_name": "${os_name}",
                                  "os_arch": "${os_arch}",
                                  "os_version": "${os_version}"
                                }
                            }
                        """;
                String osName = System.getProperty("os.name");
                String osVersion = System.getProperty("os.version");
                String osArch = System.getProperty("os.arch");
                String json = TemplateUtils.format(template,
                        new String[]{"api_key", "event_name", "metrics_id", "os_name", "os_arch", "os_version"},
                        new String[]{"phc_BgtKQaRdRWsduqwaTabPFm7EDpq95CxbRQGpERYxbmMj", "app-launch", metricsId, osName, osArch, osVersion});
                log.debug(json);
                try {
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
                } catch (Exception e) {
                    log.warn(e.getLocalizedMessage(), e);
                }
                log.info("Metrics sent");
            });
        }
    }
}
