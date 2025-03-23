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
    StreamToken predict(Input input, OutputParams outputParams);

    /**
     * @param input
     * @param outputParams
     * @param consumer
     */
    void stream(Input input, OutputParams outputParams, Consumer<StreamToken> consumer);

    void stopStreaming();
}
