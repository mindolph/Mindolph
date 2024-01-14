package com.mindolph.base.plugin;

import com.mindolph.base.genai.GenAiEvents;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

/**
 * Generator that can generate text for editor.
 *
 * @author mindolph.com@gmail.com
 */
public interface Generator {

    MenuItem contextMenuItem(String selectedText);


    StackPane showInputPanel(String defaultInput);

    /**
     * @param consumer with true if cancel normally(by user), false if cancel by exceptions.
     */
    void setOnCancel(Consumer<Boolean> consumer);

    /**
     * On completed with whether keep the generated text or not.
     *
     * @param consumer
     */
    void setOnComplete(Consumer<Boolean> consumer);

    // should be called in a new thread
    void setOnGenerated(Consumer<GenAiEvents.Output> consumer);

    void setOnPanelShowing(Consumer<StackPane> consumer);

    void setParentPane(Pane pane);

    void setParentSkin(SkinBase<?> parentSkin);
}
