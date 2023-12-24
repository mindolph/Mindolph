package com.mindolph.base.genai.llm;

/**
 * @author mindolph.com@gmail.com
 */
public class DummyLlmProvider implements LlmProvider {

    @Override
    public String predict(String input) {
        return """
                Hi, I'm AI assistant,
                ask me anything you want.
                """;
    }
}
