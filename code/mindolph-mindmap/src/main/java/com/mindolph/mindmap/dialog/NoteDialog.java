package com.mindolph.mindmap.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.SearchBar;
import com.mindolph.base.editor.MarkdownCodeArea;
import com.mindolph.base.editor.MarkdownToolbar;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.BrowseUtils;
import com.mindolph.mfx.util.FontUtils;
import com.mindolph.mindmap.event.MindmapEvents;
import com.mindolph.mindmap.model.NoteEditorData;
import com.mindolph.mindmap.model.PasswordData;
import com.mindolph.mindmap.model.TopicNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
 * Dialog to edit note for mind map.
 *
 * @author mindolph.com@gmail.com
 */
public class NoteDialog extends BaseDialogController<NoteEditorData> {

    private static final Logger log = LoggerFactory.getLogger(NoteDialog.class);

    @FXML
    private Button btnSave;
    @FXML
    private Button btnUndo;
    @FXML
    private Button btnRedo;
    @FXML
    private Button btnBrowse;
    @FXML
    private Button btnClearAll;
    @FXML
    private ToggleButton tbtnProtect;
    @FXML
    private ToggleButton tbtnSearch;
    @FXML
    private ToggleButton tbtnReplace;
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

//    private final ButtonType protectButtonType = new ButtonType("Protect", ButtonBar.ButtonData.LEFT);

    private final ButtonType importButtonType = new ButtonType("Import", ButtonBar.ButtonData.LEFT);
    private final ButtonType exportButtonType = new ButtonType("Export", ButtonBar.ButtonData.LEFT);

    /**
     * @param title
     * @param noteEditorData
     * @param font
     */
    public NoteDialog(TopicNode topic, String title, NoteEditorData noteEditorData, Font font) {
        super(noteEditorData);
        this.result = new NoteEditorData(origin.getText(), origin.isEncrypted(), origin.getPassword(), origin.getHint());
//        Platform.runLater(() -> {
        CustomDialogBuilder<NoteEditorData> builder = new CustomDialogBuilder<NoteEditorData>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(title, 32)
                .fxmlUri("dialog/note_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .button(importButtonType, dlg -> {
                    File selectedFile = DialogFactory.openFileDialog(dialog.getOwner(), SystemUtils.getUserHome());
                    if (selectedFile != null) {
                        try {
                            String text = FileUtils.readFileToString(selectedFile, StandardCharsets.UTF_8);
                            textArea.setText(text);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .button(exportButtonType, dlg -> {
                    File file = DialogFactory.openSaveFileDialog(dialog.getOwner(), SystemUtils.getUserHome()
                            , null, new FileChooser.ExtensionFilter("Text File(*.txt)", "*.txt"));
                    if (file != null && !file.exists()) {
                        try {
                            FileUtils.writeStringToFile(file, textArea.getText(), StandardCharsets.UTF_8);
                        } catch (IOException e) {
                            e.printStackTrace();
                            DialogFactory.errDialog("Failed to export note to a txt file");
                        }
                    }
                })
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .defaultValue(origin)
                .resizable(true)
                .controller(NoteDialog.this);
        dialog = builder.build();
        dialog.setOnShown(event -> {
            Platform.runLater(() -> textArea.requestFocus());
        });
        dialog.setOnCloseRequest(dialogEvent -> {
            if (!super.confirmClosing("Note has been changed, are you sure to close the dialog")) {
                dialogEvent.consume(); // keep the dialog open
            }
        });

        btnSave.setGraphic(FontIconManager.getIns().getIcon(IconKey.SAVE));
        btnBrowse.setGraphic(FontIconManager.getIns().getIcon(IconKey.BROWSE));
        btnClearAll.setGraphic(FontIconManager.getIns().getIcon(IconKey.CLEAR));
//        btnImport.setGraphic(FontIconManager.getIns().getIcon(IconKey.FOLD));
//        btnExport.setGraphic(FontIconManager.getIns().getIcon(IconKey.FOLD));
        btnUndo.setGraphic(FontIconManager.getIns().getIcon(IconKey.UNDO));
        btnRedo.setGraphic(FontIconManager.getIns().getIcon(IconKey.REDO));
        tbtnProtect.setGraphic(FontIconManager.getIns().getIcon(IconKey.LOCK));
        tbtnSearch.setGraphic(FontIconManager.getIns().getIcon(IconKey.SEARCH));
        tbtnReplace.setGraphic(FontIconManager.getIns().getIcon(IconKey.REPLACE));

        btnSave.setOnAction(event -> {
            MindmapEvents.notifyNoteSave(topic, result);
            btnSave.setDisable(true);
            origin = result; // reset the origin for closing dialog negatively.
            builder.defaultValue(result); // reset the default value for builder to convert dialog result when any button (or ESC) clicks.
            textArea.getUndoManager().forgetHistory();
        });
        btnUndo.setOnAction(event -> textArea.undo());
        btnRedo.setOnAction(event -> textArea.redo());
        btnBrowse.setOnAction(event -> {
            String selectedText = textArea.getSelectedText();
            try {
                URI url = new URI(selectedText);
                Node source = (Node) event.getSource();
                BrowseUtils.browseURI(source.getScene().getWindow(), url, true);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });
        btnClearAll.setOnAction(event -> textArea.clear());
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
                    result.setPassword(passwordData.getPassword());
                    result.setHint(passwordData.getHint());
                    if (StringUtils.isBlank(result.getPassword())) {
                        tbtnProtect.setSelected(false);
                    }
                }
                else {
                    tbtnProtect.setSelected(false);
                }
            }
            else {
                if (DialogFactory.okCancelConfirmDialog("Reset password", "Do you really want reset password for the note?")) {
                    result.setPassword(null);
                    result.setHint(null);
                }
                else {
                    tbtnProtect.setSelected(true);
                }
            }
            mouseEvent.consume();
        });
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

        editorToolBar = new MarkdownToolbar(textArea);
        hbToolbar.getChildren().add(editorToolBar);

        textArea.setStyle(FontUtils.fontToCssStyle(font));
        textArea.setText(origin.getText());
        textArea.addFeatures(TAB_INDENT, QUOTE, DOUBLE_QUOTE, LINE_DELETE, LINES_MOVE);
        textArea.scrollYToPixel(0);
        textArea.moveTo(0);
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
                    result.setText(newValue);
                    btnSave.setDisable(oldValue.equals(newValue));
                    if (!StringUtils.equals(oldValue, newValue)) {
                        this.textArea.doHistory();
                    }
                    textArea.refresh();
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
        searchBar.subscribeExit(unused -> {
            vbox.getChildren().remove(searchBar);
            tbtnSearch.setSelected(false);
            tbtnReplace.setSelected(false);
            textArea.requestFocus();
        });

        textArea.refresh();

    }
}
