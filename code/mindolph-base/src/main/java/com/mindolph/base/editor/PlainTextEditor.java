package com.mindolph.base.editor;

import com.mindolph.base.EditorContext;
import com.mindolph.base.constant.FontConstants;
import com.mindolph.core.constant.SupportFileTypes;
import javafx.event.Event;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author mindolph.com@gmail.com
 */
public class PlainTextEditor extends BaseCodeAreaEditor {

    private final Logger log = LoggerFactory.getLogger(PlainTextEditor.class);

    public PlainTextEditor(EditorContext editorContext) {
        super("/editor/plain_text_editor.fxml", editorContext);
        super.fileType = SupportFileTypes.TYPE_PLAIN_TEXT;
        // consume here otherwise the parent container will receive the double click event
        codeArea.setOnMouseClicked(Event::consume);

        this.reload();
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
