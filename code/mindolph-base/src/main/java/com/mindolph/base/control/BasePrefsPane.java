package com.mindolph.base.control;

import com.mindolph.mfx.preference.FxPreferences;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 */
public abstract class BasePrefsPane extends AnchorPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(BasePrefsPane.class);;

    protected FxPreferences fxPreferences = FxPreferences.getInstance();;

    // Bond preferences
    protected final Map<String, Pref<?>> prefMap = new HashMap<>();

    /**
     * Whether preferences is loaded, all listeners or handlers are available only this is true.
     */
    protected boolean isLoaded = false;

    public BasePrefsPane(String fxmlResourceUri) {
        URL resource = getClass().getResource(fxmlResourceUri);
        if (resource == null) {
            throw new RuntimeException("Resource not found: " + fxmlResourceUri);
        }
        FXMLLoader fxmlloader = new FXMLLoader(resource);
        fxmlloader.setRoot(this);
        fxmlloader.setController(this);

        try {
            fxmlloader.load();
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new RuntimeException("Failed to load fxml file: " + fxmlResourceUri);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * Bind a {@link Property} with a preference bidirectionally.
     *
     * @param property
     * @param prefName
     * @deprecated this will throw exception because default value can't be null.
     */
    protected <T> void bindPreference(Property<T> property, String prefName) {
        this.bindPreference(property, prefName, null);
    }

    /**
     * Bind a {@link Property} with a preference and default value bidirectionally.
     *
     * @param property
     * @param prefName
     * @param defaultValue
     * @param <T>
     */
    protected <T> void bindPreference(Property<T> property, String prefName, T defaultValue) {
        prefMap.put(prefName, new Pref<T>(property, defaultValue));
        // save to preference when value changes.
        property.addListener((ChangeListener<Object>) (observable, oldValue, newValue) -> {
            if (isLoaded) {
                log.debug("Save preference: " + prefName);
                // save all (call save()) or single? TODO
                fxPreferences.savePreference(prefName, newValue);
            }
        });
    }

    public void loadPreferences() {
        for (String prefName : prefMap.keySet()) {
            Pref<?> pref = prefMap.get(prefName);
            log.debug("Load preference: %s=%s".formatted(prefName, pref));
            Property property = pref.getProperty();
            Object defaultValue = pref.getDefaultValue();
            if (property instanceof BooleanProperty) {
                Boolean preference = fxPreferences.getPreference(prefName, Boolean.class, (Boolean) defaultValue);
                property.setValue(preference);
            }
            else if (property instanceof IntegerProperty) {
                Integer preference = fxPreferences.getPreference(prefName, Integer.class, (Integer) defaultValue);
                property.setValue(preference);
            }
            else if (property instanceof StringProperty) {
                String preference = fxPreferences.getPreference(prefName, String.class, (String) defaultValue);
                property.setValue(preference);
            }
            else if (property instanceof DoubleProperty) {
                Double preference = fxPreferences.getPreference(prefName, Double.class, (Double) defaultValue);
                property.setValue(preference);
            }
            else if (property instanceof ListProperty) {
                List<String> preferenceList = (List<String>) fxPreferences.getPreference(prefName, (List) defaultValue);
                ListProperty<String> value = new SimpleListProperty<>();
                value.addAll(preferenceList);
                property.setValue(value);
            }
            else if (property instanceof ObjectProperty) {

            }
        }
        this.initControlsFromPreferences();
        this.isLoaded = true;
    }

    /**
     * Override this method to load your own customized preferences.
     */
    protected void initControlsFromPreferences() {
        log.info("No customize preferences loaded");
    }

    /**
     * Reset all preferences to default and save, UI of this panel also will be refreshed.
     */
    public void resetToDefault() {
        for (String prefName : prefMap.keySet()) {
            Pref<?> pref = prefMap.get(prefName);
            Property property = pref.getProperty();
            Object defaultValue = pref.getDefaultValue();
            // preferencesProvider.savePreference(prefName, defaultValue);
            property.setValue(defaultValue);
        }
    }

    // probably be onSave?
    protected abstract void save(boolean notify);

    public static class Pref<T> {
        Property<T> property;
        T defaultValue;

        public Pref(Property<T> property) {
            this.property = property;
        }

        public Pref(Property<T> property, T defaultValue) {
            this.property = property;
            this.defaultValue = defaultValue;
        }

        public Property<T> getProperty() {
            return property;
        }

        public void setProperty(Property<T> property) {
            this.property = property;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public String toString() {
            return "Pref{" +
                    "property=" + property.getName() +
                    ", defaultValue=" + defaultValue +
                    '}';
        }
    }
}
