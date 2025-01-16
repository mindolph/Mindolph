package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents.Input;

import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public interface LlmProvider {

    /**
     * @param input
     * @param outputParams
     * @return
     */
    String predict(Input input, OutputParams outputParams);

    /**
     * @param input
     * @param outputParams
     * @param consumer
     */
    void stream(Input input, OutputParams outputParams, Consumer<StreamToken> consumer);

    /**
     *
     * @param text
     * @param isStop if true, no text data will be included.
     * @param isError
     */
    record StreamToken(String text, int outputTokens, boolean isStop, boolean isError) {
        public StreamToken(String text, boolean isStop, boolean isError) {
            this(text, 0, isStop, isError);
        }
    }
}
