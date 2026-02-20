package com.mindolph.base.genai.llm;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

/**
 * Adapter to OKHttp's HttpClientBuilder.
 *
 * @see HttpClientBuilder
 * @since 1.13.0
 */
public class OkHttpClientBuilder implements HttpClientBuilder {

    private static final Logger log = LoggerFactory.getLogger(OkHttpClientBuilder.class);

    private final OkHttpClient.Builder builder;
    // Since the LangChain doesn't support cancel/stop the running http request(sometimes SSE)
    // the OkHttpClientAdapter (who implemented the stop method) must be hold to force to cancel/stop the http request.
    private OkHttpClientAdapter okHttpClientAdapter;

    private String proxyType;
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private Duration connectTimeout;
    private Duration readTimeout;

    public OkHttpClientBuilder() {
        builder = new OkHttpClient.Builder();
    }

    @Override
    public Duration connectTimeout() {
        return connectTimeout;
    }

    @Override
    public HttpClientBuilder connectTimeout(Duration duration) {
        this.connectTimeout = duration;
        builder.connectTimeout(duration);
        return this;
    }

    @Override
    public Duration readTimeout() {
        return readTimeout;
    }

    @Override
    public HttpClientBuilder readTimeout(Duration duration) {
        this.readTimeout = duration;
        builder.readTimeout(duration);
        return this;
    }

    public String getProxyType() {
        return proxyType;
    }

    public OkHttpClientBuilder setProxyType(String proxyType) {
        this.proxyType = proxyType;
        return this;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public OkHttpClientBuilder setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
        return this;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public OkHttpClientBuilder setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        return this;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public OkHttpClientBuilder setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
        return this;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public OkHttpClientBuilder setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
        return this;
    }

    public OkHttpClientAdapter getOkHttpClientAdapter() {
        return okHttpClientAdapter;
    }

    @Override
    public HttpClient build() {
        if (!StringUtils.isAnyBlank(this.proxyType, this.proxyHost) && this.proxyPort > 0) {
            log.debug("use proxy");
            Proxy.Type proxyType = Proxy.Type.valueOf(this.proxyType.toUpperCase());
            Proxy proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
            builder.proxy(proxy);
            builder.proxyAuthenticator((route, response) -> {
                if (response.request().header("Proxy-Authorization") != null) {
                    return null; //
                }
                String credential = Credentials.basic(this.proxyUsername, this.proxyPassword);
                return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            });
            log.info("Build HTTP client to with proxy '%s'".formatted(Proxy.Type.valueOf(this.proxyType.toUpperCase())));
        }
        builder.retryOnConnectionFailure(false);
        OkHttpClient httpClient = builder.build();
        okHttpClientAdapter = new OkHttpClientAdapter(httpClient);
        return okHttpClientAdapter;
    }

}
