package com.mindolph.mindmap.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.SearchableCodeArea;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Toolbar in the note editor of Mind Map.
 *
 * @since 1.14
 */
public class NoteToolbar extends AnchorPane {

    private static final Logger log = LoggerFactory.getLogger(NoteToolbar.class);

    private SearchableCodeArea codeArea;

    @FXML
    private ToggleButton tbtnSearch;
    @FXML
    private ToggleButton tbtnReplace;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnUndo;
    @FXML
    private Button btnRedo;
    @FXML
    private Button btnClear;
    @FXML
    private Button btnBrowse;
    @FXML
    private ToggleButton tbtnProtect;
    @FXML
    private Button btnImport;
    @FXML
    private Button btnExport;
    @FXML
    private HBox hBox;

    public NoteToolbar(SearchableCodeArea codeArea) {
        this.codeArea = codeArea;
        FxmlUtils.loadUri("/dialog/note_toolbar.fxml", this);
        this.getStyleClass().add("no-border");
        FontIconManager fim = FontIconManager.getIns();
        btnSave.setGraphic(fim.getIcon(IconKey.SAVE));
        btnUndo.setGraphic(fim.getIcon(IconKey.UNDO));
        btnRedo.setGraphic(fim.getIcon(IconKey.REDO));
        tbtnSearch.setGraphic(fim.getIcon(IconKey.SEARCH));
        tbtnReplace.setGraphic(fim.getIcon(IconKey.REPLACE));
        btnClear.setGraphic(fim.getIcon(IconKey.CLEAR));
        btnBrowse.setGraphic(fim.getIcon(IconKey.BROWSE));
        tbtnProtect.setGraphic(fim.getIcon(IconKey.LOCK));
        btnImport.setGraphic(fim.getIcon(IconKey.IMPORT));
        btnExport.setGraphic(fim.getIcon(IconKey.EXPORT));
    }

    public void setup(boolean hideSaveUndoRedo) {
        if (hideSaveUndoRedo) {
            hBox.getChildren().removeAll(btnSave, btnUndo, btnRedo);
        }
    }

    public void disable() {
        NodeUtils.disable(tbtnSearch, tbtnReplace, btnSave, btnUndo, btnRedo, btnClear, btnBrowse, tbtnProtect, btnImport, btnExport);

    }

    public void enable() {
        NodeUtils.enable(tbtnSearch, tbtnReplace, btnSave, btnUndo, btnRedo, btnClear, btnBrowse, tbtnProtect, btnImport, btnExport);
    }


    public ToggleButton getTbtnSearch() {
        return tbtnSearch;
    }

    public ToggleButton getTbtnReplace() {
        return tbtnReplace;
    }

    public Button getBtnSave() {
        return btnSave;
    }

    public Button getBtnUndo() {
        return btnUndo;
    }

    public Button getBtnRedo() {
        return btnRedo;
    }

    public Button getBtnClear() {
        return btnClear;
    }

    public Button getBtnBrowse() {
        return btnBrowse;
    }

    public ToggleButton getTbtnProtect() {
        return tbtnProtect;
    }

    public Button getBtnImport() {
        return btnImport;
    }

    public Button getBtnExport() {
        return btnExport;
    }
}
