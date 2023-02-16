package com.mindolph.fx.preference;

import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.mfx.preference.FxPreferences;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.util.Pair;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.mindolph.base.constant.PrefConstants.GENERAL_EDITOR_ORIENTATION_MD;
import static com.mindolph.base.constant.PrefConstants.GENERAL_EDITOR_ORIENTATION_PUML;

/**
 * @author mindolph.com@gmail.com
 */
public class GeneralPreferencesPane extends BasePrefsPane implements Initializable {

    private static final String PLANT_UML = "Plant UML";
    private static final String MARKDOWN = "Markdown";
    @FXML
    private CheckBox cbConfirmBeforeQuitting;
    @FXML
    private CheckBox cbOpenLastFiles;
//    @FXML
//    private CheckBox ckbEnableAutoCreateProjectFolder;
    @FXML
    private CheckBox ckbShowHiddenFiles;
    //    @FXML
//    private CheckBox ckbAutoBackupLastEdit;
    @FXML
    private TableView<OrientationItem> tvOrientation;


    public GeneralPreferencesPane() {
        super("/preference/general_preferences_pane.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.bindPreference(cbConfirmBeforeQuitting.selectedProperty(), PrefConstants.GENERAL_CONFIRM_BEFORE_QUITTING, true);
        super.bindPreference(cbOpenLastFiles.selectedProperty(), PrefConstants.GENERAL_OPEN_LAST_FILES, true);
//        super.bindPreference(ckbEnableAutoCreateProjectFolder.selectedProperty(), PrefConstants.GENERAL_KNOWLEDGE_FOLDER_GENERATION_ALLOWED, false);
        super.bindPreference(ckbShowHiddenFiles.selectedProperty(), PrefConstants.GENERAL_SHOW_HIDDEN_FILES, false);
//        super.bindPreference(ckbAutoBackupLastEdit.selectedProperty(), "general.autoBackupBeforeSaving", true);

        TableColumn<OrientationItem, Object> colEditor = new TableColumn<>("Editor");
        TableColumn<OrientationItem, Object> colOrientation = new TableColumn<>("Orientation");
        colEditor.setSortable(false);
        colEditor.setEditable(false);
        colEditor.setPrefWidth(120);
        colOrientation.setSortable(false);
        colOrientation.setEditable(true);
        colOrientation.setPrefWidth(180);

        colEditor.setCellValueFactory(param -> {
            Label label = new Label(param.getValue().getEditor());
            label.setPadding(new Insets(4));
            return new SimpleObjectProperty<>(label);
        });

        colOrientation.setCellValueFactory(param -> {
            ChoiceBox<Pair<Orientation, String>> choiceBoxCell = new ChoiceBox<>();
            choiceBoxCell.setMinWidth(120);
            choiceBoxCell.setPadding(new Insets(4));
            choiceBoxCell.setConverter(new StringConverter<>() {
                @Override
                public String toString(Pair<Orientation, String> object) {
                    return (object == null) ? "" : object.getValue();
                }

                @Override
                public Pair<Orientation, String> fromString(String string) {
                    return null;
                }
            });
            choiceBoxCell.getItems().addAll(Arrays.stream(Orientation.values()).map(e -> new Pair<>(e, e.toString())).collect(Collectors.toList()));
            choiceBoxCell.getSelectionModel().select(new Pair<>(param.getValue().orientation, param.getValue().orientation.toString()));
            choiceBoxCell.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (PLANT_UML.equals(param.getValue().editor)) {
                    fxPreferences.savePreference(GENERAL_EDITOR_ORIENTATION_PUML, newValue.getKey());
                }
                else if (MARKDOWN.equals(param.getValue().editor)) {
                    fxPreferences.savePreference(GENERAL_EDITOR_ORIENTATION_MD, newValue.getKey());
                }
            });
            return new SimpleObjectProperty<>(choiceBoxCell);
        });
        tvOrientation.getColumns().add(colEditor);
        tvOrientation.getColumns().add(colOrientation);
        FxPreferences fxPreferences = FxPreferences.getInstance();
        tvOrientation.getItems().add(new OrientationItem(PLANT_UML, fxPreferences.getPreference(GENERAL_EDITOR_ORIENTATION_PUML, Orientation.class, Orientation.VERTICAL)));
        tvOrientation.getItems().add(new OrientationItem(MARKDOWN, fxPreferences.getPreference(GENERAL_EDITOR_ORIENTATION_MD, Orientation.class, Orientation.HORIZONTAL)));
    }

    @Override
    public void loadPreferences() {
        super.loadPreferences();
    }

    @Override
    protected void save() {

    }

    private static class OrientationItem {
        String editor;
        Orientation orientation;

        public OrientationItem(String editor, Orientation orientation) {
            this.editor = editor;
            this.orientation = orientation;
        }

        public String getEditor() {
            return editor;
        }

        public void setEditor(String editor) {
            this.editor = editor;
        }

        public Orientation getOrientation() {
            return orientation;
        }

        public void setOrientation(Orientation orientation) {
            this.orientation = orientation;
        }
    }
}
