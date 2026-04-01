package com.mindolph.mindmap.view;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.mindolph.base.BaseView;
import com.mindolph.base.constant.FontConstants;
import com.mindolph.base.control.SearchBar;
import com.mindolph.base.editor.MarkdownCodeArea;
import com.mindolph.base.editor.MarkdownToolbar;
import com.mindolph.base.util.CssUtils;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.core.search.SearchParams;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.i18n.I18nHelper;
import com.mindolph.mfx.util.BrowseUtils;
import com.mindolph.mindmap.dialog.NoteToolbar;
import com.mindolph.mindmap.dialog.PasswordDialog;
import com.mindolph.mindmap.dialog.PasswordSettingDialog;
import com.mindolph.mindmap.event.MindmapEvents;
import com.mindolph.mindmap.model.NoteEditorData;
import com.mindolph.mindmap.model.PasswordData;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.CryptoUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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

import static com.mindolph.base.control.ExtCodeArea.FEATURE.*;

/**
 * Panel displays the note editor of Mind Map.
 * The onSaveListener should be set to handle saving note.
 *
 * @since 1.14
 */
public class NotePanel extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(NotePanel.class);

    private final StringProperty mode = new SimpleStringProperty(AttributesMode.MODE_DIALOG);

    @FXML
    private AnchorPane paneCode;
    @FXML
    private MarkdownCodeArea textArea;
    private MarkdownToolbar editorToolBar;
    @FXML
    private HBox hbToolbar;
    @FXML
    private VBox vbox;

    private Button btnSave;
    private Button btnUndo;
    private Button btnRedo;
    private Button btnBrowse;
    private ToggleButton tbtnProtect;
    private ToggleButton tbtnSearch;
    private ToggleButton tbtnReplace;
    private Button btnImport;
    private Button btnExport;

    private final SearchBar searchBar = new SearchBar();
    private NoteToolbar noteToolbar;

    private TopicNode topic;
    private NoteEditorData data;

    public NotePanel() {
        super("/view/note_panel.fxml");
        // the mode property hasn't been set when the constructor is called, so the listener will be invoked after the constructor,
        // and the note toolbar and editor toolbar will be initialized after the mode is set.
        this.mode.addListener((observable, oldValue, newValue) -> {
            log.debug("Mode is changed to %s".formatted(newValue));
            this.noteToolbar.setup(AttributesMode.MODE_SIDE_PANE.equals(this.mode.get()));
            if (AttributesMode.MODE_SIDE_PANE.equals(this.mode.get())) {
                // avoid conflict with global undo/redo in mind map when note editor is used as side pane, so disable the undo/redo feature in this case.
                textArea.setDisableUndo(true);
                textArea.setDisableRedo(true);
            }
        });
        this.noteToolbar = new NoteToolbar(textArea);
        this.hbToolbar.getChildren().add(this.noteToolbar);
        this.editorToolBar = new MarkdownToolbar(textArea);
        this.hbToolbar.getChildren().add(this.editorToolBar);
        this.init();
    }

    private void init() {
        btnSave = noteToolbar.getBtnSave();
        btnSave.setOnAction(event -> {
            log.debug("Notify to save note form the note panel");
            if (this.handleNoteData()) {
                this.exportNoteData();
                noteToolbar.getBtnSave().setDisable(true);
                MindmapEvents.notifyNoteSaveEvent(this.topic, this.data);
                textArea.getUndoManager().forgetHistory();
            }
        });

        btnUndo = noteToolbar.getBtnUndo();
        btnRedo = noteToolbar.getBtnRedo();
        btnBrowse = noteToolbar.getBtnBrowse();
        btnExport = noteToolbar.getBtnExport();
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
        tbtnProtect = noteToolbar.getTbtnProtect();

        // listen on mouse click event instead of on selected state change event for
        // setting the selection state triggers the listener wrongly being invoked.
        tbtnProtect.setOnMouseClicked(mouseEvent -> {
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
            this.handleNoteData();
            this.exportNoteData();
            mouseEvent.consume();
        });
        tbtnSearch = noteToolbar.getTbtnSearch();
        tbtnReplace = noteToolbar.getTbtnReplace();
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
        this.btnImport = noteToolbar.getBtnImport();
        this.btnExport = noteToolbar.getBtnExport();
        btnImport.setOnAction(actionEvent -> {
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
        this.btnExport.setOnAction(actionEvent -> {
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

        CssUtils.applyFontCss(textArea, "/style/markdown_syntax_template.css",
                FontConstants.KEY_MMD_NOTE_FONT, FontConstants.KEY_MMD_NOTE_FONT_MONO);

        textArea.addFeatures(TAB_INDENT, QUOTE, DOUBLE_QUOTE, LINE_DELETE, LINES_MOVE);
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (loading) return;
                    btnSave.setDisable(oldValue.equals(newValue));
                    if (!Strings.CS.equals(oldValue, newValue)) {
                        if (AttributesMode.MODE_DIALOG.equals(this.mode.get())) {
                            this.textArea.doHistory();
                        }
                        else if (AttributesMode.MODE_SIDE_PANE.equals(this.mode.get())) {
                            this.handleNoteData();
                            this.exportNoteData();
                            MindmapEvents.notifyAttributesChangeEvent(this.topic);
                        }
                    }
                }
        );
        textArea.undoAvailableProperty().addListener((observableValue, aBoolean, newValue) -> btnUndo.setDisable(!newValue));
        textArea.redoAvailableProperty().addListener((observableValue, aBoolean, newValue) -> btnRedo.setDisable(!newValue));
        textArea.selectionProperty().addListener((observable, oldValue, newValue) -> {
            log.debug("%d-%d within %d".formatted(newValue.getStart(), newValue.getEnd(), textArea.getText().length()));
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
    }

    /**
     *
     * @param topic
     * @param data
     * @param decrypt decrypt the note text if it's encrypted.
     * @return false if loading data failed, like decrypting encrypted note with wrong password, otherwise true.
     */
    public boolean loadData(TopicNode topic, NoteEditorData data, boolean decrypt) {
        this.loading = true;
        this.topic = topic;
        this.data = data;
        if (topic == null) {
            this.textArea.setDisable(true);
            this.noteToolbar.disable();
            this.editorToolBar.disable();
        }
        else {
            this.textArea.setDisable(false);
            this.editorToolBar.enable();
            if (data == null) {
                this.noteToolbar.disable();
                NodeUtils.enable(btnImport); // only enable the import button.
            }
            else {
                this.noteToolbar.enable();
                NodeUtils.disable(btnSave, btnUndo, btnRedo);
                tbtnProtect.setSelected(data.isEncrypted());
            }
        }
        // loading the note.
        if (this.data == null) {
            log.trace("Set note editor to be empty");
            textArea.setText(StringUtils.EMPTY);
            this.data = new NoteEditorData();
        }
        else {
            if (!this.data.isEncrypted()) {
                textArea.setDisable(false);
                log.trace("Set note editor with text: %s".formatted(StringUtils.abbreviate(this.data.getText(), 50)));
                textArea.setText(this.data.getText());
                textArea.scrollYToPixel(0);
                textArea.moveTo(0);
            }
            else {
                // only try to decrypt if not in the Note dialog.
                if (decrypt) {
                    log.debug("Show password dialog to decrypt the note");
                    PasswordData passwordData = new PasswordData("", this.data.getHint());
                    passwordData = new PasswordDialog(passwordData).showAndWait();
                    if (passwordData != null && StringUtils.isNotBlank(passwordData.getPassword())) {
                        StringBuilder buf = new StringBuilder();
                        String pass = passwordData.getPassword().trim();
                        try {
                            if (CryptoUtils.decrypt(pass, this.data.getText(), buf)) {
                                String strDecrypted = buf.toString();
                                log.trace(strDecrypted);
                                textArea.setText(strDecrypted);
                                // password is required for saving anyway.
                                this.data.setPassword(pass);
                            }
                            else {
                                DialogFactory.errDialog("Wrong password!");
                                this.displaySecretNote();
                            }
                        } catch (RuntimeException ex) {
                            DialogFactory.errDialog("Can't decode encrypted text for error!\nEither broken data or current JDK security policy doesn't support AES-256!");
                            log.error("Can't decode encrypted note", ex);
                            this.displaySecretNote();
                        }
                    }
                    else {
                        // Canceled. notify to close the dialog(if it's in dialog)
                        return false;
                    }
                }
                else {
                    this.displaySecretNote();
                }
            }
            textArea.refresh();
        }
        this.loading = false;
        return true;
    }

    private void displaySecretNote() {
        log.debug("Selected note is protected, disable the editor and show the hint if exists");
        tbtnProtect.setSelected(true);
        textArea.setDisable(true);
        textArea.setText("[Password protected note]");
    }

    public boolean handleNoteData() {
        if (this.data != null) {
            // clear the note attribute
            if (StringUtils.isEmpty(this.textArea.getText())) {
                topic.removeExtra(Extra.ExtraType.NOTE);
            }
            else {
                // save the note attribute if changed.
                String newNoteText;
                if (StringUtils.isBlank(this.data.getPassword())) {
                    newNoteText = this.textArea.getText();
                    this.data.setEncrypted(false);
                }
                else {
                    // encrypt first if there is password.
                    try {
                        newNoteText = CryptoUtils.encrypt(this.data.getPassword(), this.textArea.getText());
                        this.data.setEncrypted(true);
                    } catch (RuntimeException ex) {
                        DialogFactory.warnDialog("Can't encrypt note!\nCheck JDK security policy for AES-256 support");
                        log.error("Can't encrypt note", ex);
                        return false;
                    }
                }
                log.debug("save data: " + this.data);
                this.data.setText(newNoteText);
            }
            return true;
        }
        else {
            log.warn("No data returned");
            return false;
        }
    }

    public void exportNoteData() {
        this.topic.setExtra(new ExtraNote(this.data.getText(), this.data.isEncrypted(), this.data.getHint()));
    }

    public void requestInputFocus() {
        this.textArea.requestFocus();
    }

    private TextSearchOptions createTextSearchOptions(SearchParams searchParams) {
        TextSearchOptions options = new TextSearchOptions();
        options.setCaseSensitive(searchParams.isCaseSensitive());
        return options;
    }

    public String getMode() {
        return mode.get();
    }

    public StringProperty modeProperty() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode.set(mode);
    }
}
