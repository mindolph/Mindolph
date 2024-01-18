package com.mindolph.base.editor;

import com.mindolph.base.EditorContext;
import com.mindolph.base.constant.FontConstants;
import com.mindolph.core.constant.SupportFileTypes;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.fxmisc.richtext.CharacterHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * @author mindolph.com@gmail.com
 */
public class PlainTextEditor extends BaseCodeAreaEditor {

    private static final Logger log = LoggerFactory.getLogger(PlainTextEditor.class);

    @FXML
    private AnchorPane paneEditor;

    public PlainTextEditor(EditorContext editorContext) {
        super("/editor/plain_text_editor.fxml", editorContext, true);
        super.fileType = SupportFileTypes.TYPE_PLAIN_TEXT;
        // consume here otherwise the parent container will receive the double click event
        codeArea.setOnMouseClicked(Event::consume);
        codeArea.setParentPane(paneEditor);
        this.refresh();
    }

    @Override
    protected void onFilesDropped(CharacterHit hit, File file, String filePath) {
        codeArea.insertText(hit.getInsertionIndex(), filePath);
    }

    @Override
    public String getFontPrefKey() {
        return FontConstants.KEY_TXT_EDITOR;
    }

    @Override
    protected void refresh(String text) {
        // DO NOTHING FOR NOW
    }

    @Override
    public void export() {

    }

    public Image getImage() {
        Text text = new Text(codeArea.getText());
        return text.snapshot(null, null);
    }

}
