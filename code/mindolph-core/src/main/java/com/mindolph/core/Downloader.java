package com.mindolph.core;


import com.mindolph.core.config.ProxyMeta;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

/**
 * TODO authentication
 *
 * @since 1.13.0
 */
public class Downloader {

    private static final Logger log = LoggerFactory.getLogger(Downloader.class);

    /**
     * Download a file from URL (without a proxy)
     *
     * @param fileUrl
     * @param saveFile
     * @return
     */
    public void download(String fileUrl, File saveFile) {
        this.downloadWithProxy(fileUrl, saveFile, null, null);
    }

    /**
     * Download a file from URL with proxy meta (proxy)
     *
     * @param fileUrl
     * @param saveFile
     * @param proxyMeta
     * @return
     */
    public void downloadWithProxyMeta(String fileUrl, File saveFile, ProxyMeta proxyMeta) {
        Proxy proxy = null;
        Authenticator authenticator = null;
        if (proxyMeta != null) {
            Proxy.Type proxyType;
            try {
                proxyType = Proxy.Type.valueOf(proxyMeta.type());
            } catch (IllegalArgumentException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException("Not supported proxy type: %s".formatted(proxyMeta.type()));
            }
            proxy = new Proxy(proxyType, new InetSocketAddress(proxyMeta.host(), proxyMeta.port()));
            authenticator = (route, response) -> {
                if (response.request().header("Proxy-Authorization") != null) {
                    return null; // 如果已经尝试过认证但失败了，返回null避免无限循环
                }

                // 代理认证信息
                String credential = Credentials.basic(proxyMeta.username(), proxyMeta.password());
                return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            };
        }
        this.downloadWithProxy(fileUrl, saveFile, proxy, authenticator);
    }

    /**
     * Download a file from URL with proxy
     *
     * @param fileUrl
     * @param saveFile
     * @param proxy
     * @return
     */
    public void downloadWithProxy(String fileUrl, File saveFile, Proxy proxy, Authenticator authenticator) {
        OkHttpClient client;
        if (proxy != null) {
            client = new OkHttpClient.Builder()
                    .proxy(proxy)
                    .proxyAuthenticator(authenticator)
                    .connectTimeout(Duration.ofSeconds(30))
                    .readTimeout(Duration.ofMinutes(10))
                    .writeTimeout(Duration.ofMinutes(5))
                    .build();
        }
        else {
            client = new OkHttpClient.Builder().build();
        }

        Request request = new Request.Builder()
                .url(fileUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error from server: " + response);
            }

            InputStream inputStream = response.body().byteStream();

            if (saveFile.getParentFile() != null) {
                saveFile.getParentFile().mkdirs();
            }
            FileOutputStream outputStream = new FileOutputStream(saveFile);

            byte[] buffer = new byte[4096]; // 4KB buffer
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
//            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage(), e);
//            return false;
        }
    }

}
