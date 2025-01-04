package com.mindolph.base.genai.llm;

import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public interface LlmProvider {

    /**
     * @param input
     * @param temperature
     * @param outputParams
     * @return
     */
    String predict(String input, float temperature, OutputParams outputParams);

    /**
     * @param input
     * @param temperature
     * @param outputParams
     * @param consumer
     */
    void stream(String input, float temperature, OutputParams outputParams, Consumer<StreamToken> consumer);

    /**
     *
     * @param text
     * @param isStop if true, no text data will be included.
     * @param isError
     */
    record StreamToken(String text, boolean isStop, boolean isError) {
    }
}
