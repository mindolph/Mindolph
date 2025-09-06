package com.mindolph.base.genai.llm;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;
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
 * @since 1.13.0
 * @see HttpClientBuilder
 */
public class OkHttpClientBuilderAdapter implements HttpClientBuilder {

    private static final Logger log = LoggerFactory.getLogger(OkHttpClientBuilderAdapter.class);

    private final OkHttpClient.Builder builder;

    private String proxyType;
    private String proxyHost;
    private int proxyPort;
    private Duration connectTimeout;
    private Duration readTimeout;

    public OkHttpClientBuilderAdapter() {
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

    public OkHttpClientBuilderAdapter setProxyType(String proxyType) {
        this.proxyType = proxyType;
        return this;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public OkHttpClientBuilderAdapter setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
        return this;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public OkHttpClientBuilderAdapter setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        return this;
    }

    @Override
    public HttpClient build() {
        if (!StringUtils.isAnyBlank(this.proxyType, this.proxyHost) && this.proxyPort > 0) {
            log.debug("use proxy");
            Proxy.Type proxyType = Proxy.Type.valueOf(this.proxyType.toUpperCase());
            Proxy proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
            builder.proxy(proxy);
            log.info("Build HTTP client to with proxy '%s'".formatted(Proxy.Type.valueOf(this.proxyType.toUpperCase())));
        }
        builder.retryOnConnectionFailure(false);
        OkHttpClient client = builder.build();
        return new OkHttpClientAdapter(client);
    }

}
