package com.mindolph.genai;

import com.mindolph.genai.GenaiUiConstants.MessageType;

/**
 * Represent the partial of one chat message for streaming output.
 *
 * @since 1.13.0
 */
public class ChatPartial {

    private String text;
    // is the last partial of the completed chat message.
    private boolean isLast = false;
    private MessageType type;

    public ChatPartial(String text, MessageType type) {
        this.text = text;
        this.type = type;
    }

    public ChatPartial(String text, MessageType type, boolean isLast) {
        this.text = text;
        this.type = type;
        this.isLast = isLast;
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

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
