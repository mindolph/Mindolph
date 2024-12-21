package com.mindolph.base.dialog;

import com.mindolph.core.model.Snippet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ResourceBundle;

import static com.mindolph.core.constant.SupportFileTypes.TYPE_MARKDOWN;

/**
 * Launch from DemoMain.
 *
 * @author mindolph.com@gmail.com
 */
public class DialogTest implements Initializable {


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    public void onFontSelectDialog(ActionEvent event) {
        FontSelectDialog fontSelectDialog = new FontSelectDialog(Font.getDefault());
        fontSelectDialog.showAndWait();
    }

    @FXML
    public void onSnippetDialog(ActionEvent event) {
        SnippetDialog snippetDialog = new SnippetDialog(TYPE_MARKDOWN,null);
        Snippet<?> snippet = snippetDialog.showAndWait();
        if (snippet != null) {
            System.out.println(snippet.getClass());
            System.out.println(snippet.getTitle());
            System.out.println(snippet.getCode());
        }
    }
}
