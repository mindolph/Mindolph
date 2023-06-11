package com.mindolph.core.search;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author mindolph.com@gmail.com
 */
public class SearchUtils {

    private static final String GRAPHICAL_LINE_BREAKER = "⏎";

    public static String normalizeSpace(String text) {
        String replaced = StringUtils.replace(text, "\n", GRAPHICAL_LINE_BREAKER);
        return StringUtils.normalizeSpace(replaced);
    }

    /**
     * Create pattern from string.
     *
     * @param text         text to be converted into pattern.
     * @param patternFlags flags to be used
     * @return formed pattern
     */
    public static Pattern string2pattern(String text, int patternFlags) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            result.append("\\u");
            String code = Integer.toHexString(c).toUpperCase(Locale.ENGLISH);
            result.append("0000", 0, 4 - code.length()).append(code);
        }

        return Pattern.compile(result.toString(), patternFlags);
    }

}
