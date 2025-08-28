package com.mindolph.core.util;

import com.mindolph.core.Env;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;

/**
 * @since unknown
 */
public class AppUtils {

    public static File getAppBaseDir() {
        if (Env.isDevelopment) {
            return new File(SystemUtils.getUserHome(), ".mindolph.dev");
        }
        else {
            return new File(SystemUtils.getUserHome(), ".mindolph");
        }
    }
}
