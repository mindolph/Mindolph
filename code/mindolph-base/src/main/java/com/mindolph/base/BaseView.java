package com.mindolph.base;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

/**
 * @author mindolph.com@gmail.com
 */
public abstract class BaseView extends AnchorPane {

    /**
     * Be used to control the refreshing of the view.
     *
     * @since 1.10
     */
    protected BooleanProperty active = new SimpleBooleanProperty(true);

    public BaseView(String fxmlUri) {
        this(fxmlUri, true);
    }

    public BaseView(String fxmlUri, boolean active) {
        this.active.set(active);
        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource(fxmlUri));
        fxmlloader.setRoot(this);
        fxmlloader.setController(this);

        try {
            fxmlloader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public boolean getActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }
}
