package com.mindolph.base;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

/**
 * @author mindolph.com@gmail.com
 */
public abstract class BaseView extends AnchorPane {

    public BaseView(String fxmlUri) {
        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource(fxmlUri));
        fxmlloader.setRoot(this);
        fxmlloader.setController(this);

        try {
            fxmlloader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

//    protected abstract String fxmlUri();

}
