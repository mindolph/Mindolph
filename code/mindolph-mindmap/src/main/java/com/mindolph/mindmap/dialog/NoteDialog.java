package com.mindolph.mindmap.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.i18n.I18nHelper;
import com.mindolph.mindmap.event.MindmapEvents;
import com.mindolph.mindmap.model.NoteEditorData;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.view.AttributesMode;
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

    private final NotePanel notePanel;

    private final TopicNode topic;

    /**
     * @param topic
     * @param title
     * @param noteEditorData
     */
    public NoteDialog(TopicNode topic, String title, NoteEditorData noteEditorData) {
        super(noteEditorData);
        this.topic = topic;
        this.result = new NoteEditorData(origin.getText(), origin.isEncrypted(), origin.getPassword(), origin.getHint());
        this.notePanel = new NotePanel();
        this.notePanel.setMode(AttributesMode.MODE_DIALOG);

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
        MindmapEvents.subscribeNoteSaveEvent(this.topic, data -> {
            this.origin = data; // reset the origin for closing dialog negatively.
            builder.defaultValue(data); // reset the default value for builder to convert the dialog result when any button (or ESC) clicks.
        });
        dialog = builder.build();
        dialog.setOnShowing(event -> {
            if (!this.notePanel.loadData(topic, this.result, true)) {
                // Closing dialog here causes flashing. TODO
                Platform.runLater(() -> dialog.close());
            }
            else {
                notePanel.requestInputFocus();
            }
        });
        dialog.setOnCloseRequest(dialogEvent -> {
            this.notePanel.handleNoteData();
            if (this.origin.hasChanges(this.result)) {
                if (!super.confirmClosing(I18nHelper.getInstance().get("mindmap.dialog.note.changed"))) {
                    dialogEvent.consume(); // keep the dialog open
                }
            }
        });
    }

    @Override
    public void onPositive(NoteEditorData result) {
        // export data to mindmap when click the positive button.
        this.notePanel.exportNoteData();
    }

    @Override
    public void onNegative(NoteEditorData result) {
        this.result = this.origin;
    }
}
