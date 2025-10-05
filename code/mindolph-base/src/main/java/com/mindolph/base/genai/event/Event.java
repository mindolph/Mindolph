package com.mindolph.base.genai.event;

import com.mindolph.base.constant.Stage;

import java.io.Serializable;

/**
 * @since 1.13.2
 */
public class Event implements Serializable {
    private String message;

    private boolean success;

    Stage stage;

    public Event(Stage stage, String message, boolean success) {
        this.stage = stage;
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
