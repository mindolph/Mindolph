package com.mindolph.fx.control;

import com.mindolph.mfx.util.FxmlUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ToggleButton;
import org.controlsfx.control.SegmentedButton;

/**
 * @author mindolph.com@gmail.com
 */
public class FileFilterButtonGroup extends SegmentedButton {

    public static final String FILE_OPTION_ALL = "all";
    private final StringProperty selectedFileType = new SimpleStringProperty();

    public FileFilterButtonGroup() {
        init();
    }

    private void init() {
        FxmlUtils.loadUri("/control/file_filter_button_group.fxml", this);
        for (ToggleButton button : this.getButtons()) {
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

}
