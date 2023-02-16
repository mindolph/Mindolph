package com.mindolph.core.util;

import org.swiftboot.util.ClasspathResourceUtils;
import org.swiftboot.util.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ResourceUtils {

    public static String readResourceToString(String resUri) {
        URL cssUri = ClasspathResourceUtils.getResourceURI(resUri);
        if (cssUri == null) {
            return null;
        }
        try {
            InputStream inputStream = cssUri.openStream();
            return IoUtils.readAllToString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
