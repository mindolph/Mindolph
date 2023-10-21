package com.mindolph.core.util;

/**
 * @author mindolph.com@gmail.com
 * @deprecated
 */
public class SystemPropertyUtils {

    public static boolean isSystemPropValTrue(String evnName) {
        String v = System.getProperty(evnName);
        if (v == null) {
            return false;
        }
        return Boolean.parseBoolean(v);
    }
}
