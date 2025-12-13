package com.mindolph.base.util;


import com.mindolph.core.util.AppUtils;

import java.io.File;
import java.io.IOException;

/**
 * @since 1.13.3
 */
public class ConfigUtils {

    // moved from LlmConfig
    public static File datasetConfigFile() {
        String path = "%s/conf/datasets.json".formatted(AppUtils.getAppBaseDir());
        File f = new File(path);
        if (!f.exists()) {
            try {
                if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
                if (f.createNewFile()) {
                    return f;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return f;
    }

    public static File collectionConfigFile() {
        String path = "%s/conf/collections.json".formatted(AppUtils.getAppBaseDir());
        File f = new File(path);
        if (!f.exists()) {
            try {
                if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
                if (f.createNewFile()) {
                    return f;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return f;
    }
}
