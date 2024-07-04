package com.mindolph.base.editor;

import com.mindolph.base.EditorContext;
import com.mindolph.base.util.CssUtils;
import com.mindolph.core.constant.SupportFileTypes;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CharacterHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static com.mindolph.base.constant.FontConstants.KEY_TXT_EDITOR;
import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;


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
    protected void onFileDropped(CharacterHit hit, File file, String filePath) {
        codeArea.insertText(hit.getInsertionIndex(), filePath);
    }

    @Override
    protected void onFilesDropped(CharacterHit hit, List<File> files) {
        List<String> paths = files.stream().map(file -> super.getRelatedPathInCurrentWorkspace(file).orElseGet(file::getPath)).toList();
        codeArea.insertText(hit.getInsertionIndex(), StringUtils.join(paths, LINE_SEPARATOR));
    }

    @Override
    public void applyStyles() {
        CssUtils.applyFontCss(codeArea,KEY_TXT_EDITOR);
    }

    @Override
    protected void refresh(String text) {
        this.refresh();
    }

    @Override
    public void export() {

    }

    public Image getImage() {
        Text text = new Text(codeArea.getText());
        return text.snapshot(null, null);
    }

}
