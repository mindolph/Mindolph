package com.mindolph.base.genai.llm;

import com.google.gson.JsonObject;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.mfx.util.TextUtils;
import okhttp3.*;
import okhttp3.sse.EventSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collections;
import java.util.List;
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

    // to cancel/stop the running http request.
    protected EventSource streamEventSource;

    public BaseApiLlmProvider(ProviderMeta providerMeta, ModelMeta modelMeta) {
        super(providerMeta, modelMeta);
        this.initHttpClient();
    }

    protected void initHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .callTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS);
        if (super.proxyEnabled && providerMeta.useProxy()) {
            log.debug("use proxy");
            Proxy.Type proxyType = Proxy.Type.valueOf(proxyMeta.type().toUpperCase());
            Proxy proxy = new Proxy(proxyType, new InetSocketAddress(proxyMeta.host(), proxyMeta.port()));
            builder.proxy(proxy);
            builder.proxyAuthenticator((route, response) -> {
                if (response.request().header("Proxy-Authorization") != null) {
                    return null; //
                }
                String credential = Credentials.basic(proxyMeta.username(), proxyMeta.password());
                return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            });
        }
        log.info("Build HTTP client to access '%s' %s".formatted(modelMeta.getName(),
                super.proxyEnabled && super.providerMeta.useProxy() ? "with %s proxy '%s'".formatted(Proxy.Type.valueOf(proxyMeta.type().toUpperCase()), proxyMeta.url()) : "without proxy"));
        builder.retryOnConnectionFailure(false);
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_1, TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                .build();
        builder.connectionSpecs(Collections.singletonList(spec));
        client = builder.build();
    }

    /**
     * @param template     the template should contain only user input, temperature and max output tokens variables.
     * @param model
     * @param input
     * @param input
     * @param outputParams
     * @return
     */
    protected RequestBody createRequestBody(String template, String model, Input input, OutputParams outputParams) {
        log.debug("Input: %s".formatted(input));
        log.debug("Create request body by model: %s, temperature: %s, %s".formatted(model, input.temperature(), outputParams));
        // compose user prompt first
//        String encoded = new String(JsonStringEncoder.getInstance().encodeAsUTF8(input.text()));
        String encoded = input.text();
        Map<String, Object> args = super.formatParams(encoded, outputParams);
        String formattedPrompt = args.entrySet().stream().reduce(PROMPT_FORMAT_TEMPLATE,
                (s, e) -> s.replace("{{%s}}".formatted(e.getKey()), e.getValue().toString()),
                (s, s2) -> s);
//        formattedPrompt = StringUtils.strip(formattedPrompt);
        formattedPrompt = TextUtils.replaceLineBreaksWithWhitespace(formattedPrompt);
        formattedPrompt = StringEscapeUtils.escapeJson(formattedPrompt);
        log.debug(formattedPrompt);

        // format the JSON params
        String jsonParams;
        if (StringUtils.isNotBlank(model)) {
            jsonParams = template.formatted(model, formattedPrompt.trim(), input.temperature(), input.maxTokens());
        }
        else {
            jsonParams = template.formatted(formattedPrompt.trim(), input.temperature(), input.maxTokens());
        }
        log.debug(jsonParams);
        RequestBody requestBody = RequestBody.create(jsonParams, JSON);
        if (log.isTraceEnabled()) log.trace(String.valueOf(requestBody.contentType()));
        return requestBody;
    }

    /**
     * Whether streaming stops from the result of LLM.
     *
     * @param jsonObject
     * @param key
     * @return
     */
    protected boolean determineStreamStop(JsonObject jsonObject, String key) {
        if (jsonObject.has(key)
                && jsonObject.get(key) != null
                && !jsonObject.get(key).isJsonNull()) {
            String finishReason = jsonObject.get(key).getAsString();
            return getFinishReasons().stream().anyMatch(reason -> reason.equals(finishReason));
        }
        return false;
    }

    @Override
    public void stopStreaming() {
        if (streamEventSource != null) {
            streamEventSource.cancel();
        }
        else {
            log.debug("No stream event source available");
        }
    }

    /**
     * Inherit to support other more finish reasons.
     *
     * @return
     */
    protected List<String> getFinishReasons() {
        return Collections.singletonList("stop");
    }

    protected abstract String apiUrl();

    protected abstract String predictPromptTemplate();

    protected abstract String streamPromptTemplate();

}
