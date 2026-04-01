package com.mindolph.base;

import com.mindolph.mfx.i18n.I18nHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.ResourceBundle;

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

    protected boolean loading = false;

    public BaseView(String fxmlUri) {
        this(fxmlUri, true);
    }

    public BaseView(String fxmlUri, boolean active) {
        this.active.set(active);
        ResourceBundle resourceBundle = I18nHelper.getInstance().getResourceBundle();
        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource(fxmlUri));
        fxmlloader.setResources(resourceBundle);
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
