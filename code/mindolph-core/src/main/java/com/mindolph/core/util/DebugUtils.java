package com.mindolph.core.util;

public class DebugUtils {

    /**
     * Make the invisible blank chars to be visible, just for debugging.
     *
     * @return
     * @since 1.12.7
     */
    public static String visible(String text) {
        return text.replace("\n", "\\n")
                .replace(" ", "\\s")
                .replace("\t", "\\t");
    }
}
