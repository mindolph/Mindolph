package com.mindolph.base.plugin;

import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

/**
 * Generator that can generate text for editor.
 *
 * @author mindolph.com@gmail.com
 */
public interface Generator {

    MenuItem contextMenuItem(String selectedText);

    StackPane inputDialog(Object editorId);

    /**
     *
     * @param consumer with true if cancel normally(by user), false if cancel by exceptions.
     */
    void onCancel(Consumer<Boolean> consumer);

    /**
     * On completed with whether keep the generated text or not.
     * @param consumer
     */
    void onComplete(Consumer<Boolean> consumer);

    // should be called in a new thread
    void onGenerated(Consumer<String> consumer);

    StackPane reframeDialog(Object editorId);

}
