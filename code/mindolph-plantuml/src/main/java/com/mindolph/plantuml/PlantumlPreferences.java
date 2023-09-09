package com.mindolph.plantuml;

import com.mindolph.base.control.BasePrefsPane;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 */
public class PlantumlPreferences extends BasePrefsPane implements Initializable {

    @FXML
    private TextField tfGraphvizPath;
    @FXML
    private Button btnGraphvizPath;

    public PlantumlPreferences() {
        super("/preference/plantuml_preferences.fxml");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        btnGraphvizPath.setOnAction(event -> {
            File file = DialogFactory.openFileDialog(((Node) event.getSource()).getScene().getWindow(), SystemUtils.getUserHome());
            if (file != null) {
                tfGraphvizPath.setText(file.getPath());
            }
        });
    }

    @Override
    public void loadPreferences() {
        tfGraphvizPath.setText(fxPreferences.getPreference("plantuml.dotpath", ""));
    }

    @Override
    public void save(boolean notify) {
        final String pathToGraphVizDot = tfGraphvizPath.getText();
        if (pathToGraphVizDot.trim().isEmpty()) {
            fxPreferences.removePreference("plantuml.dotpath");
        }
        else {
            fxPreferences.savePreference("plantuml.dotpath", pathToGraphVizDot);
        }

    }
}
