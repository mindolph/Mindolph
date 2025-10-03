package com.mindolph.base.genai.rag;

import com.mindolph.base.constant.Stage;

import java.io.File;
import java.io.Serializable;

/**
 * @param file
 * @param success
 * @param successCount
 * @param msg
 * @param stage        the stage of whole embedding.
 * @param ratio        0-1 the percent to complete
 * @since 1.13
 * @see Stage
 */
public record EmbeddingProgress(File file,
                                boolean success,
                                int successCount,
                                String msg,
                                Stage stage,
                                float ratio) implements Serializable {
}
