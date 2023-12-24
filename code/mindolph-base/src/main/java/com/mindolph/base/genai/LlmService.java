package com.mindolph.base.genai;

import com.mindolph.base.genai.GenAiEvents.OutputLength;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author mindolph.com@gmail.com
 */
public class LlmService {

    private static final LlmService ins = new LlmService();

    private LlmService() {

    }

    public static synchronized LlmService getIns() {
        return ins;
    }

    public String predict(String input, float temperature, OutputLength outputLength) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String chatId = RandomStringUtils.randomAlphabetic(10);
        return """
                [%s]
                Hi, I'm AI assistant,
                ask me anything you want.
                """.formatted(chatId);
    }
}
