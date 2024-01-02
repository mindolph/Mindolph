package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public interface LlmProvider {

    String predict(String input, float temperature, GenAiEvents.OutputAdjust outputAdjust);
}
