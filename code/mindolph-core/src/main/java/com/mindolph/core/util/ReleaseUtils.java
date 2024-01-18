package com.mindolph.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.BufferedIoUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 */
public class ReleaseUtils {
    private static final Logger log = LoggerFactory.getLogger(ReleaseUtils.class);

    public static final String GITHUB_API_LATEST_VERSION =
            "https://api.github.com/repos/mindolph/Mindolph/releases/latest";


    public static void getLatestReleaseVersion(Consumer<ReleaseInfo> consumer) {
        new Thread(() -> {
            ReleaseInfo latestVersion = getLatestReleaseVersion();
            consumer.accept(latestVersion);
        }).start();
    }

    public static ReleaseInfo getLatestReleaseVersion() {
        log.info("Check latest release from: " + GITHUB_API_LATEST_VERSION);
        URL u;
        String json = null;
        try {
            u = new URL(GITHUB_API_LATEST_VERSION);
            HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
            InputStream inputStream = httpConn.getInputStream();

            // TODO replace with BufferedIoUtils.readAllAsString() later
            StringBuffer buf = new StringBuffer();
            BufferedIoUtils.readInputStream(inputStream, 1024, bytes -> {
                String s = new String(bytes);
                //log.debug("'%s'".formatted(s));
                buf.append(s);
            });
            json = buf.toString();

            JsonObject root = (JsonObject) JsonParser.parseString(json);
            JsonElement version = root.get("tag_name");
            JsonElement url = root.get("html_url");
            ReleaseInfo ri = new ReleaseInfo();
            if (version != null && url != null
                    && StringUtils.isNotBlank(version.getAsString())
                    && StringUtils.isNotBlank(url.getAsString())) {
                ri.setVersion(version.getAsString());
                ri.setUrl(url.getAsString());
                log.info("Got latest release: " + ri);
                return ri;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            log.info(json);
            log.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public static class ReleaseInfo {
        private String version;
        private String url;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return "ReleaseInfo{" +
                    "version='" + version + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}
