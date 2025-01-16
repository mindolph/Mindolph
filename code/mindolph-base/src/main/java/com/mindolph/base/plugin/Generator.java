package com.mindolph.base.plugin;

import com.mindolph.base.genai.GenAiEvents.Output;
import com.mindolph.base.genai.GenAiEvents.StreamOutput;
import com.mindolph.core.constant.GenAiConstants.ProviderInfo;
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

    ProviderInfo getProviderInfo();

    MenuItem generationMenuItem(String selectedText);

    /**
     *
     * @return
     * @since 1.11
     */
    MenuItem summaryMenuItem();

    StackPane showInputPanel(String defaultInput);

    /**
     *
     * @return
     * @since 1.11
     */
    StackPane showSummarizePanel(String input);

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

    /**
     * Callback before generating text.
     *
     * @param consumer
     */
    void setBeforeGenerate(Consumer<Void> consumer);

    /**
     * Callback when streaming generated text.
     *
     * @param consumer
     */
    void setOnStreaming(Consumer<StreamOutput> consumer);

    /**
     * Callback when text generated.
     * should be called in a new thread.
     * @param consumer
     */
    void setOnGenerated(Consumer<Output> consumer);

    /**
     * Listen on panel showing.
     *
     * @param consumer
     */
    void setOnPanelShowing(Consumer<StackPane> consumer);

    /**
     * Set parent pane to display user input panel.
     *
     * @param pane
     */
    void setParentPane(Pane pane);

    void setParentSkin(SkinBase<?> parentSkin);
}
