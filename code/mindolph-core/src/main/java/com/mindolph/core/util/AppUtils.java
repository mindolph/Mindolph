package com.mindolph.core.util;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;

/**
 * @since unknown
 */
public class AppUtils {

    public static File getAppBaseDir() {
        return new File(SystemUtils.getUserHome(), ".mindolph");
    }
}
