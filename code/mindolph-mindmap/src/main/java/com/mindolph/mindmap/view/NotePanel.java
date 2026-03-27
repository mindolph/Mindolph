package com.mindolph.mindmap.view;

import com.mindolph.base.BaseView;
import com.mindolph.base.constant.FontConstants;
import com.mindolph.base.control.SearchBar;
import com.mindolph.base.editor.MarkdownCodeArea;
import com.mindolph.base.editor.MarkdownToolbar;
import com.mindolph.base.util.CssUtils;
import com.mindolph.core.search.SearchParams;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.i18n.I18nHelper;
import com.mindolph.mfx.util.BrowseUtils;
import com.mindolph.mindmap.dialog.NoteToolbar;
import com.mindolph.mindmap.dialog.PasswordSettingDialog;
import com.mindolph.mindmap.event.MindmapEvents;
import com.mindolph.mindmap.model.NoteEditorData;
import com.mindolph.mindmap.model.PasswordData;
import com.mindolph.mindmap.model.TopicNode;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.UrlUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static com.mindolph.base.control.ExtCodeArea.FEATURE.*;
import static com.mindolph.base.control.ExtCodeArea.FEATURE.LINES_MOVE;
import static com.mindolph.base.control.ExtCodeArea.FEATURE.LINE_DELETE;

/**
 * Panel displays the note editor of Mind Map.
 *
 * @since 1.14
 */
public class NotePanel extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(NotePanel.class);

    @FXML
    private AnchorPane paneCode;
    @FXML
    private MarkdownCodeArea textArea;
    private MarkdownToolbar editorToolBar;
    @FXML
    private HBox hbToolbar;
    @FXML
    private VBox vbox;

    private final SearchBar searchBar = new SearchBar();
    private final NoteToolbar noteToolbar;

    private NoteEditorData data;

    private Consumer<NoteEditorData> onSaveListener;

    public NotePanel(TopicNode topic, NoteEditorData noteEditorData) {
        super("/view/note_panel.fxml");
        this.data = noteEditorData;
        this.noteToolbar = new NoteToolbar(textArea);

        Button btnSave = noteToolbar.getBtnSave();
        btnSave.setOnAction(event -> {
            MindmapEvents.notifyNoteSave(topic, this.data);
            noteToolbar.getBtnSave().setDisable(true);
            this.onSaveListener.accept(this.data);
            textArea.getUndoManager().forgetHistory();
        });
        Button btnUndo = noteToolbar.getBtnUndo();
        Button btnRedo = noteToolbar.getBtnRedo();
        Button btnBrowse = noteToolbar.getBtnBrowse();
        btnUndo.setOnAction(event -> textArea.undo());
        btnRedo.setOnAction(event -> textArea.redo());
        btnBrowse.setOnAction(event -> {
            String selectedText = textArea.getSelectedText();
            try {
                URI url = new URI(selectedText);
                Node source = (Node) event.getSource();
                BrowseUtils.browseURI(source.getScene().getWindow(), url, false);
            } catch (URISyntaxException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        });
        noteToolbar.getBtnClear().setOnAction(event -> textArea.clear());
        ToggleButton tbtnProtect = noteToolbar.getTbtnProtect();
        if (StringUtils.isNotBlank(noteEditorData.getPassword())) {
            tbtnProtect.setSelected(true);
        }
        // listen on mouse click event instead of on selected state change event for
        // setting the selection state triggers the listener wrongly being invoked.
        tbtnProtect.setOnMouseClicked(mouseEvent -> {
            log.warn("onMouseClicked");
            if (tbtnProtect.isSelected()) {
                PasswordSettingDialog passwordDialog = new PasswordSettingDialog(null);
                PasswordData passwordData = passwordDialog.showAndWait();
                if (passwordData != null) {
                    this.data.setPassword(passwordData.getPassword());
                    this.data.setHint(passwordData.getHint());
                    if (StringUtils.isBlank(this.data.getPassword())) {
                        tbtnProtect.setSelected(false);
                    }
                }
                else {
                    tbtnProtect.setSelected(false);
                }
            }
            else {
                if (DialogFactory.okCancelConfirmDialog(I18nHelper.getInstance().get("mindmap.password.reset.title"), I18nHelper.getInstance().get("mindmap.password.reset.confirm"))) {
                    this.data.setPassword(null);
                    this.data.setHint(null);
                }
                else {
                    tbtnProtect.setSelected(true);
                }
            }
            mouseEvent.consume();
        });
        ToggleButton tbtnSearch = noteToolbar.getTbtnSearch();
        ToggleButton tbtnReplace = noteToolbar.getTbtnReplace();
        tbtnSearch.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                tbtnReplace.setSelected(false);
                searchBar.setShowReplace(false);
                vbox.getChildren().add(1, searchBar); // 1 is the place between button bar and text area.
                searchBar.requestFocus();
            }
            else {
                vbox.getChildren().remove(searchBar);
            }
        });
        tbtnReplace.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                tbtnSearch.setSelected(false);
                searchBar.setShowReplace(true);
                vbox.getChildren().add(1, searchBar); // 1 is the place between button bar and text area.
                searchBar.requestFocus();
            }
            else {
                vbox.getChildren().remove(searchBar);
            }
        });
        noteToolbar.getBtnImport().setOnAction(actionEvent -> {

            File selectedFile = DialogFactory.openFileDialog(NotePanel.this.getScene().getWindow(), SystemUtils.getUserHome());
            if (selectedFile != null) {
                try {
                    String text = FileUtils.readFileToString(selectedFile, StandardCharsets.UTF_8);
                    textArea.setText(text);
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            }
        });
        noteToolbar.getBtnExport().setOnAction(actionEvent -> {
            File file = DialogFactory.openSaveFileDialog(NotePanel.this.getScene().getWindow(), SystemUtils.getUserHome()
                    , null, new FileChooser.ExtensionFilter(I18nHelper.getInstance().get("mindmap.note.export.filter"), "*.txt"));
            if (file != null && !file.exists()) {
                try {
                    FileUtils.writeStringToFile(file, textArea.getText(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                    DialogFactory.errDialog(I18nHelper.getInstance().get("mindmap.note.export.error"));
                }
            }
        });
        hbToolbar.getChildren().add(noteToolbar);

        editorToolBar = new MarkdownToolbar(textArea);
        hbToolbar.getChildren().add(editorToolBar);

        CssUtils.applyFontCss(textArea, "/style/markdown_syntax_template.css",
                FontConstants.KEY_MMD_NOTE_FONT, FontConstants.KEY_MMD_NOTE_FONT_MONO);

        textArea.setText(noteEditorData.getText());
        textArea.addFeatures(TAB_INDENT, QUOTE, DOUBLE_QUOTE, LINE_DELETE, LINES_MOVE);
        textArea.scrollYToPixel(0);
        textArea.moveTo(0);
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
                    this.data.setText(newValue);
                    btnSave.setDisable(oldValue.equals(newValue));
                    if (!Strings.CS.equals(oldValue, newValue)) {
                        this.textArea.doHistory();
                    }
                    textArea.refreshAsync();
                }
        );
        textArea.undoAvailableProperty().addListener((observableValue, aBoolean, newValue) -> btnUndo.setDisable(!newValue));
        textArea.redoAvailableProperty().addListener((observableValue, aBoolean, newValue) -> btnRedo.setDisable(!newValue));
        textArea.selectionProperty().addListener((observable, oldValue, newValue) -> {
            log.debug("%s-%s within %d".formatted(newValue.getStart(), newValue.getEnd(), textArea.getText().length()));
            if (newValue.getEnd() > textArea.getText().length()) {
                return; // there is a bug in RichTextFx, which is, when selection to the end of text, this listener will be called twice, and the first one is wrong.
            }
            String selectedText = textArea.getText(newValue);
            btnBrowse.setDisable(!UrlUtils.isValid(selectedText));
        });
        textArea.setParentPane(paneCode);

        searchBar.setSearchPrevEventHandler(searchParams -> {
            TextSearchOptions textSearchOptions = new TextSearchOptions();
            textSearchOptions.setCaseSensitive(searchParams.isCaseSensitive());
            textArea.searchPrev(searchParams.getKeywords(), textSearchOptions);
        });
        searchBar.setSearchNextEventHandler(searchParams -> {
            TextSearchOptions textSearchOptions = new TextSearchOptions();
            textSearchOptions.setCaseSensitive(searchParams.isCaseSensitive());
            textArea.searchNext(searchParams.getKeywords(), textSearchOptions);
        });
        searchBar.subscribeReplace(searchParams -> {
            TextSearchOptions searchOptions = createTextSearchOptions(searchParams);
            log.debug("replace selected text with '%s'".formatted(searchParams.getReplacement()));
            searchOptions.setForReplacement(true);
            textArea.searchAndReplaceSelection(searchParams.getKeywords(), searchOptions, searchParams.getReplacement());
        });
        searchBar.subscribeReplaceAll(searchParams -> {
            log.debug("replace all matched text with '%s'".formatted(searchParams.getReplacement()));
            textArea.replaceAllMatch(searchParams.getKeywords(), createTextSearchOptions(searchParams), searchParams.getReplacement());
        });
        searchBar.subscribeExit(unused -> {
            vbox.getChildren().remove(searchBar);
            tbtnSearch.setSelected(false);
            tbtnReplace.setSelected(false);
            textArea.requestFocus();
        });
        textArea.refresh();
    }

    public void requestInputFocus() {
        this.textArea.requestFocus();
    }

    private TextSearchOptions createTextSearchOptions(SearchParams searchParams) {
        TextSearchOptions options = new TextSearchOptions();
        options.setCaseSensitive(searchParams.isCaseSensitive());
        return options;
    }

    public void setOnSaveListener(Consumer<NoteEditorData> onSaveListener) {
        this.onSaveListener = onSaveListener;
    }
}
