package com.mindolph.fx.control;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.Text;
import org.controlsfx.control.SegmentedButton;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mindolph.com@gmail.com
 */
public class FileFilterButtonGroup extends SegmentedButton {

    public static final String FILE_OPTION_ALL = "all";
    private final StringProperty selectedFileType = new SimpleStringProperty();
    private final BooleanProperty allowFolder = new SimpleBooleanProperty(true);

    private static final Map<String, IconKey> fileIconMap = new HashMap<>() {
        {
            put(SupportFileTypes.TYPE_FOLDER, IconKey.FOLDER);
            put(SupportFileTypes.TYPE_MIND_MAP, IconKey.FILE_MMD);
            put(SupportFileTypes.TYPE_MARKDOWN, IconKey.FILE_MD);
            put(SupportFileTypes.TYPE_PLANTUML, IconKey.FILE_PUML);
            put(SupportFileTypes.TYPE_PLAIN_TEXT, IconKey.FILE_TXT);
            put(SupportFileTypes.TYPE_CSV, IconKey.FILE_CSV);
        }
    };

    @FXML
    private ToggleButton tbAll;
    @FXML
    private ToggleButton tbFolder;

    public FileFilterButtonGroup() {
        init();
    }

    private void init() {
        FxmlUtils.loadUri("/control/file_filter_button_group.fxml", this);
        Integer iconSize = FxPreferences.getInstance().getPreference(PrefConstants.GENERAL_GLOBAL_ICON_SIZE, 16);
        tbAll.setPrefHeight(iconSize + 12);
        tbAll.setMinWidth(iconSize + 12);
        for (ToggleButton button : this.getButtons()) {
            // by ID from user data
            Text icon = FontIconManager.getIns().getIcon(fileIconMap.get(button.getUserData()));
            button.setPrefHeight(iconSize + 12);
            button.setGraphic(icon);
            button.setToggleGroup(null);
            button.selectedProperty().addListener((observableValue, aBoolean, selected) -> {
                if (selected) {
                    selectedFileType.set(String.valueOf(button.getUserData()));
                }
            });
        }

        selectedFileType.addListener((observableValue, s, newValue) -> {
            for (ToggleButton button : getButtons()) {
                button.setSelected(button.getUserData().equals(newValue));
            }
        });

        allowFolder.addListener((observable, oldValue, newValue) -> {
            tbFolder.setDisable(!newValue);
            tbFolder.setVisible(newValue);
            tbFolder.setManaged(newValue);
        });
    }

    public String getSelectedFileType() {
        return selectedFileType.get();
    }

    public void setSelectedFileType(String selectedFileType) {
        this.selectedFileType.set(selectedFileType);
    }

    public StringProperty selectedFileTypeProperty() {
        return selectedFileType;
    }

    public boolean isAllowFolder() {
        return allowFolder.get();
    }

    public BooleanProperty allowFolderProperty() {
        return allowFolder;
    }

    public void setAllowFolder(boolean isAllowFolder) {
        allowFolder.set(isAllowFolder);
    }
}
