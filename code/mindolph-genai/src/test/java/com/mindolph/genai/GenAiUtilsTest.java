package com.mindolph.genai;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GenAiUtilsTest {

    @Test
    public void displayGenAiTokens() {
        Assertions.assertEquals("100", GenAiUtils.displayGenAiTokens(100));
        Assertions.assertEquals("100.00 K", GenAiUtils.displayGenAiTokens(100 * 1024));
        Assertions.assertEquals("100.00 M", GenAiUtils.displayGenAiTokens(100 * 1024 * 1024));
        Assertions.assertEquals("100.00 G", GenAiUtils.displayGenAiTokens(100L * 1024 * 1024 * 1024));
    }
}
