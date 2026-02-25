package com.mindolph.mindmap.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mindmap.model.NoteEditorData;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.view.NotePanel;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog to edit note for mind map.
 *
 * @author mindolph.com@gmail.com
 */
public class NoteDialog extends BaseDialogController<NoteEditorData> {

    private static final Logger log = LoggerFactory.getLogger(NoteDialog.class);

    private NotePanel notePanel;

    /**
     * @param title
     * @param noteEditorData
     */
    public NoteDialog(TopicNode topic, String title, NoteEditorData noteEditorData) {
        super(noteEditorData);
        this.result = new NoteEditorData(origin.getText(), origin.isEncrypted(), origin.getPassword(), origin.getHint());
        this.notePanel = new NotePanel(topic, this.result);

        CustomDialogBuilder<NoteEditorData> builder = new CustomDialogBuilder<NoteEditorData>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(title, 32)
//                .fxmlUri("dialog/note_dialog.fxml")
                .fxContent(notePanel)
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .defaultValue(origin)
                .resizable(true)
                .controller(NoteDialog.this);
        this.notePanel.setOnSaveListener(data -> {
            origin = data; // reset the origin for closing dialog negatively.
            builder.defaultValue(data); // reset the default value for builder to convert the dialog result when any button (or ESC) clicks.
        });
        dialog = builder.build();
        dialog.setOnShown(event -> {
            Platform.runLater(() -> notePanel.requestInputFocus());
        });
        dialog.setOnCloseRequest(dialogEvent -> {
            if (!super.confirmClosing("Note has been changed, are you sure to close the dialog")) {
                dialogEvent.consume(); // keep the dialog open
            }
        });
    }

}
