package com.mindolph.base.genai.llm;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public interface LlmProvider {

    /**
     *
     * @param input
     * @param temperature
     * @param outputParams
     * @return
     */
    String predict(String input, float temperature, OutputParams outputParams);

}
