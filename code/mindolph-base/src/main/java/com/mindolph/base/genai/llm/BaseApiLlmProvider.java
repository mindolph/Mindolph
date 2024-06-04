package com.mindolph.base.genai.llm;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
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

    private static final Logger log = LoggerFactory.getLogger(BaseApiLlmProvider.class);

    protected OkHttpClient client;

    public BaseApiLlmProvider(String apiKey, String aiModel, boolean useProxy) {
        super(apiKey, aiModel, useProxy);
        this.initHttpClient();
    }

    protected void initHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .callTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS);
        if (super.proxyEnabled && super.useProxy) {
            Proxy.Type proxyType = Proxy.Type.valueOf(super.proxyType.toUpperCase());
            Proxy proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
            builder.proxy(proxy);
        }
        log.info("Build HTTP client to access '%s' %s".formatted(this.aiModel,
                super.proxyEnabled ? "with %s proxy '%s'".formatted(Proxy.Type.valueOf(super.proxyType.toUpperCase()), this.proxyUrl) : "without proxy"));
        builder.retryOnConnectionFailure(false);
        client = builder.build();
    }

    /**
     * @param template     the template should contain only user input and temperature variables.
     * @param model
     * @param input
     * @param temperature
     * @param outputParams
     * @return
     */
    protected RequestBody createRequestBody(String template, String model, String input, float temperature, OutputParams outputParams) {
        // compose user prompt first
        input = new String(JsonStringEncoder.getInstance().encodeAsUTF8(input));
        Map<String, Object> args = super.formatParams(input, outputParams);
        String formattedPrompt = args.entrySet().stream().reduce(TEMPLATE,
                (s, e) -> s.replace("{{" + e.getKey() + "}}", e.getValue().toString()),
                (s, s2) -> s);

        formattedPrompt = StringEscapeUtils.escapeJson(formattedPrompt);
        log.debug(formattedPrompt);

        // format the JSON params
        String jsonParams;
        if (StringUtils.isNotBlank(model)) {
            jsonParams = template.formatted(model, formattedPrompt.trim(), temperature);
        }
        else {
            jsonParams = template.formatted(formattedPrompt.trim(), temperature);
        }
        log.debug(jsonParams);
        RequestBody requestBody = RequestBody.create(jsonParams, JSON);
        return requestBody;
    }

}
