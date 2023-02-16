package com.mindolph.fx.dialog;

import com.mindolph.base.EditorContext;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.ShortcutManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.editor.Editable;
import com.mindolph.core.model.NodeData;
import com.mindolph.markdown.MarkdownEditor;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ShortcutsDialog extends BaseDialogController<Void> {

    public ShortcutsDialog() {

        dialog = new CustomDialogBuilder<Void>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Key Reference")
                .fxmlUri("dialog/shortcuts_dialog.fxml")
                .buttons(ButtonType.CLOSE)
                .icon(ButtonType.CLOSE, FontIconManager.getIns().getIcon(IconKey.CLOSE))
                .defaultValue(null)
                .resizable(true)
                .controller(this)
                .build();

        File tmpDir = SystemUtils.getJavaIoTmpDir();
        File shortcutsFile = new File(tmpDir, "mindolph_shortcuts.md");
        System.out.println("write to file: " + shortcutsFile);
//        if (!shortcutsFile.exists()) {
            String markdown = ShortcutManager.getIns().exportToMarkdown();
            try {
                FileUtils.writeStringToFile(shortcutsFile, markdown, Charset.defaultCharset());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//        }

        EditorContext editorContext = new EditorContext();
        editorContext.setFileData(new NodeData(shortcutsFile));
        MarkdownEditor markdownEditor = new MarkdownEditor(editorContext);
        markdownEditor.setEditorReadyEventHandler(() -> {
            System.out.println("ready");
        });
        new Thread(() -> {
            try {
                markdownEditor.loadFile(() -> {
                    System.out.println("loaded");
                    markdownEditor.changeViewMode(Editable.ViewMode.PREVIEW_ONLY);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        dialog.getDialogPane().setContent(markdownEditor);
    }
}
