package com.mindolph.base.genai.event;

import com.mindolph.base.constant.Stage;

import java.io.File;

/**
 * @since 1.13.2
 */
public class ProgressEvent extends Event {
    //    Stage stage;
    File file;
    int successCount;
    float ratio;

    public ProgressEvent(Stage stage, String message, boolean success, File file, int successCount, float ratio) {
        super(stage, message, success);
//        this.stage = stage;
        this.file = file;
        this.successCount = successCount;
        this.ratio = ratio;
    }

    /**
     * Successful event with progress details.
     *
     * @param stage
     * @param message
     * @param file
     * @param successCount
     * @param ratio
     */
    public ProgressEvent(Stage stage, String message, File file, int successCount, float ratio) {
        super(stage, message, true);
        this.stage = stage;
        this.file = file;
        this.successCount = successCount;
        this.ratio = ratio;
    }

    public ProgressEvent(Stage stage, String message, boolean success) {
        super(stage, message, success);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
