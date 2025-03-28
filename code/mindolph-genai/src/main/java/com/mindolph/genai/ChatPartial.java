package com.mindolph.genai;

/**
 *
 * @since unknown
 */
public class ChatPartial {

    private String text;
    private boolean isLast = false;
    private GenaiUiConstants.MessageType type;

    public ChatPartial(String text, GenaiUiConstants.MessageType type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public GenaiUiConstants.MessageType getType() {
        return type;
    }

    public void setType(GenaiUiConstants.MessageType type) {
        this.type = type;
    }
}
