package com.mindolph.base.genai.llm;

import java.util.Objects;

/**
 *
 */
public class StreamPartial {
    private String text;
    private int outputTokens;
    private boolean isStop;
    private boolean isError;

    /**
     * @param text
     * @param outputTokens
     * @param isStop       if true, no text data will be included.
     * @param isError
     */
    public StreamPartial(String text, int outputTokens, boolean isStop, boolean isError) {
        this.text = text;
        this.outputTokens = outputTokens;
        this.isStop = isStop;
        this.isError = isError;
    }

    public StreamPartial(String text, boolean isStop, boolean isError) {
        this(text, 0, isStop, isError);
    }

    public String text() {
        return text;
    }

    public int outputTokens() {
        return outputTokens;
    }

    public boolean isStop() {
        return isStop;
    }

    public boolean isError() {
        return isError;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setOutputTokens(int outputTokens) {
        this.outputTokens = outputTokens;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    public void setError(boolean error) {
        isError = error;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StreamPartial) obj;
        return Objects.equals(this.text, that.text) &&
                this.outputTokens == that.outputTokens &&
                this.isStop == that.isStop &&
                this.isError == that.isError;
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, outputTokens, isStop, isError);
    }

    @Override
    public String toString() {
        return "StreamToken[" +
                "text=" + text + ", " +
                "outputTokens=" + outputTokens + ", " +
                "isStop=" + isStop + ", " +
                "isError=" + isError + ']';
    }

}
