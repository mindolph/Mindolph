package com.mindolph.core.util;

import org.apache.commons.lang3.StringUtils;

import java.util.function.BiFunction;

/**
 * @author mindolph.com@gmail.com
 * @since 1.3.4
 */
public class FunctionUtils {

    public static BiFunction<String, String, Boolean> textContains(boolean caseSens) {
        return caseSens ? StringUtils::contains : StringUtils::containsIgnoreCase;
    }
}
