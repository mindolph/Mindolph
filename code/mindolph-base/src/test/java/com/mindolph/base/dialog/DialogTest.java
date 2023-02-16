package com.mindolph.base.dialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 */
public class DialogTest implements Initializable {


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    public void onFontSelectDialog(ActionEvent event){
        FontSelectDialog fontSelectDialog = new FontSelectDialog(Font.getDefault());
        fontSelectDialog.showAndWait();
    }
}
