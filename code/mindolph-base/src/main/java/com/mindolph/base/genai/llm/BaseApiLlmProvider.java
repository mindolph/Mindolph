package com.mindolph.base.genai.llm;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7.4
 */
public abstract class BaseApiLlmProvider extends BaseLlmProvider {

    public static final MediaType JSON = MediaType.get("application/json");

    private static final Logger log = LoggerFactory.getLogger(GeminiProvider.class);

    protected OkHttpClient client;

    public BaseApiLlmProvider(String apiKey, String aiModel) {
        super(apiKey, aiModel);
        this.initHttpClient();
    }

    protected void initHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(timeout, TimeUnit.SECONDS);
        if (super.proxyEnabled) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            builder.proxy(proxy);
        }
        log.info("Build HTTP client to access '%s' %s".formatted(this.aiModel,
                super.proxyEnabled ? "with %s proxy '%s'".formatted(Proxy.Type.valueOf(super.proxyType), this.proxyUrl) : "without proxy"));
        client = builder.build();
    }

    protected RequestBody createRequestBody(String template, String input, float temperature, OutputParams outputParams) {
        input = new String(JsonStringEncoder.getInstance().encodeAsUTF8(input));
        input = StringEscapeUtils.escapeJson(input);
        Map<String, Object> args = super.formatParams(input, outputParams);

        String formatted = args.entrySet().stream().reduce(TEMPLATE,
                (s, e) -> s.replace("{{" + e.getKey() + "}}", e.getValue().toString()),
                (s, s2) -> s);
        String jsonParams = template.formatted(formatted.trim(), temperature);
        RequestBody requestBody = RequestBody.create(jsonParams, JSON);
        return requestBody;
    }

}
