package com.mindolph.core;


import com.mindolph.core.config.ProxyMeta;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * TODO authentication
 *
 * @since unknown
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
    public boolean download(String fileUrl, File saveFile) {
        return this.downloadWithProxy(fileUrl, saveFile, null);
    }

    /**
     * Download a file from URL with proxy meta (proxy)
     *
     * @param fileUrl
     * @param saveFile
     * @param proxyMeta
     * @return
     */
    public boolean downloadWithProxyMeta(String fileUrl, File saveFile, ProxyMeta proxyMeta) {
        Proxy proxy = null;
        if (proxyMeta != null) {
            Proxy.Type proxyType;
            try {
                proxyType = Proxy.Type.valueOf(proxyMeta.type());
            } catch (IllegalArgumentException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException("Not supported proxy type: %s".formatted(proxyMeta.type()));
            }
            proxy = new Proxy(proxyType, new InetSocketAddress(proxyMeta.host(), proxyMeta.port()));
        }
        return this.downloadWithProxy(fileUrl, saveFile, proxy);
    }

    /**
     * Download a file from URL with proxy
     *
     * @param fileUrl
     * @param saveFile
     * @param proxy
     * @return
     */
    public boolean downloadWithProxy(String fileUrl, File saveFile, Proxy proxy) {
        OkHttpClient client;
        if (proxy != null) {
            client = new OkHttpClient.Builder()
                    .proxy(proxy) //
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
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

}
