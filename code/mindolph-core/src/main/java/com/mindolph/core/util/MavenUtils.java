package com.mindolph.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author mindolph.com@gmail.com
 */
public class MavenUtils {

    /**
     * Only works in packaged application.
     *
     * @return
     */
    public static String getVersionInPomProperties() {
        return getVersionInPomProperties("com.mindolph", "mindolph-desktop");
    }

    /**
     * Ge the version
     *
     * @param group
     * @param artifact
     * @return
     * @throws IOException
     */
    public static String getVersionInPomProperties(String group, String artifact) {
        ClassLoader cl = MavenUtils.class.getClassLoader();
        String path = "META-INF/maven/%s/%s/pom.properties".formatted(group, artifact);
        try (InputStream resourceAsStream = cl.getResourceAsStream(path);) {
            if (resourceAsStream != null) {
                Properties propKeys = new Properties();
                try {
                    propKeys.load(resourceAsStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                return String.valueOf(propKeys.get("version"));
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
