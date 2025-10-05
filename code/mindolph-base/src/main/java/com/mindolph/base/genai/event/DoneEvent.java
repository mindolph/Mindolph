package com.mindolph.base.genai.event;

import com.mindolph.base.constant.Stage;

/**
 * @since 1.13.2
 */
public class DoneEvent extends Event {

    int successCount;

    public DoneEvent(Stage stage, String msg) {
        super(stage, msg, true); // default is success
        this.stage = stage;
    }

    public DoneEvent(Stage stage, String msg, boolean success) {
        super(stage, msg, success);
        this.stage = stage;
    }

    public DoneEvent(Stage stage, String msg, int successCount) {
        super(stage, msg, true);
        this.stage = stage;
        this.successCount = successCount;
    }

    public DoneEvent(Stage stage, String msg, boolean success, int successCount) {
        super(stage, msg, success);
        this.stage = stage;
        this.successCount = successCount;
    }


    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

}
