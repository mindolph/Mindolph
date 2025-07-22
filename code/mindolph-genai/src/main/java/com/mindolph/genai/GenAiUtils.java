package com.mindolph.genai;

/**
 *
 * @since 1.11.4
 */
public class GenAiUtils {

    public static String displayGenAiTokens(int tokenCount) {
        return "%sK".formatted(tokenCount/1024);
    }
}
