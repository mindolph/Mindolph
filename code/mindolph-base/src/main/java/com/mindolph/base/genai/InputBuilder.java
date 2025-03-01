package com.mindolph.base.genai;

import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.core.constant.GenAiConstants.OutputAdjust;
import org.apache.commons.lang3.StringUtils;

/**
 * @since 1.11.1
 */
public class InputBuilder {
    private String text;
    private float temperature;
    private int maxTokens = 0;
    private OutputAdjust outputAdjust;
    private String outputLanguage;
    private boolean isRetry;
    private boolean isStreaming;
    private String model = StringUtils.EMPTY;

    public InputBuilder text(String text) {
        this.text = text;
        return this;
    }

    public InputBuilder temperature(float temperature) {
        this.temperature = temperature;
        return this;
    }

    public InputBuilder maxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public InputBuilder outputAdjust(OutputAdjust outputAdjust) {
        this.outputAdjust = outputAdjust;
        return this;
    }

    public InputBuilder outputLanguage(String outputLanguage) {
        this.outputLanguage = outputLanguage;
        return this;
    }

    public InputBuilder isRetry(boolean isRetry) {
        this.isRetry = isRetry;
        return this;
    }

    public InputBuilder isStreaming(boolean isStreaming) {
        this.isStreaming = isStreaming;
        return this;
    }

    public InputBuilder model(String model) {
        this.model = model;
        return this;
    }

    public Input createInput() {
        return new Input(model, text, temperature, maxTokens, outputAdjust, outputLanguage, isRetry, isStreaming);
    }

}