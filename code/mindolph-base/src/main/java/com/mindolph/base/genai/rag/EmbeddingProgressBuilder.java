package com.mindolph.base.genai.rag;

import com.mindolph.base.constant.Stage;

import java.io.File;

/**
 * @since 1.13.2
 */
public class EmbeddingProgressBuilder {
    private String msg;
    private File file = null;
    private boolean success = false;
    private int successCount = 0; // start from 1
    private Stage stage = null;
    private float ratio = 0;

    public EmbeddingProgressBuilder msg(String msg) {
        this.msg = msg;
        return this;
    }

    public EmbeddingProgressBuilder file(File file) {
        this.file = file;
        return this;
    }

    public EmbeddingProgressBuilder success(boolean success) {
        this.success = success;
        return this;
    }

    public EmbeddingProgressBuilder success() {
        this.success = true;
        return this;
    }

    public EmbeddingProgressBuilder fail() {
        this.success = false;
        return this;
    }

    public EmbeddingProgressBuilder successCount(int successCount) {
        this.successCount = successCount;
        return this;
    }

    public EmbeddingProgressBuilder stage(Stage stage) {
        this.stage = stage;
        return this;
    }

    public EmbeddingProgressBuilder ratio(float ratio) {
        this.ratio = ratio;
        return this;
    }

    public EmbeddingProgress build() {
        return new EmbeddingProgress(file, success, successCount, msg, stage, ratio);
    }
}