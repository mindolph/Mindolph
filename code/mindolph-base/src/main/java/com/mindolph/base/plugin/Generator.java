package com.mindolph.base.plugin;

import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 */
public interface Generator {

    MenuItem contextMenuItem(String selectedText);

    StackPane inputDialog(Object editorId);

    void onCancel(Consumer<Object> consumer);

    void onComplete(Consumer<Object> consumer);

    // should be called in a new thread
    void onGenerated(Consumer<String> consumer);

    StackPane reframeDialog(Object editorId);

}
