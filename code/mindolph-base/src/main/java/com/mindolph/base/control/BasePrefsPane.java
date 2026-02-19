package com.mindolph.base.control;

import com.mindolph.mfx.preference.FxPreferences;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author mindolph.com@gmail.com
 */
public abstract class BasePrefsPane extends AnchorPane implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(BasePrefsPane.class);

    protected FxPreferences fxPreferences = FxPreferences.getInstance();

    // Binding of a preference key and Pref object.
    private final MultiValuedMap<String, Pref> bondPrefMap = new HashSetValuedHashMap<>();

    /**
     * Whether preferences are loaded, all listeners or handlers are available only this is true.
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
            log.error("Failed to load fxml file" + fxmlResourceUri, exception);
            throw new RuntimeException("Failed to load preference pane " + this.getClass());
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
     * @deprecated this will throw exception because the default value can't be null.
     */
    protected <T> void bindPreference(Property<T> property, String prefName) {
        this.bindPreference(property, prefName, null);
    }

    /**
     * Bind a {@link Property} with a preference and default value bidirectionally.
     * This method works for like TextField, ChoiceBox, CheckBox, Single RadioButton, etc.
     *
     * @param property
     * @param prefName
     * @param defaultValue
     * @param <T>
     */
    protected <T> void bindPreference(Property<T> property, String prefName, T defaultValue) {
        bondPrefMap.put(prefName, new Pref<T, T>(property, defaultValue, t -> t)); // the converter just return what it is for there is no need to convert.
        // save to preference when value changes.
        property.addListener((ChangeListener<Object>) (observable, oldValue, newValue) -> {
            if (isLoaded) {
                log.debug("Save preference: %s".formatted(prefName));
                // save all (call save()) or single? TODO
                fxPreferences.savePreference(prefName, newValue);
                this.onSave(true, newValue); // new value as payload
            }
        });
    }

    /**
     * Bind one or multiple {@link Property}s with a preference and default value bidirectionally.
     * This method works for cases that property value type is different from the preference value type, like RadioButton group.
     * Use saveConverter and loadConverter to convert between property value type and preference value type.
     * NOTE: can't be used for ReadOnlyProperty.
     *
     * @param property
     * @param prefName      one preference name can bind multiple properties (for like a group of radio buttons)
     * @param defaultValue
     * @param saveConverter
     * @param loadConverter
     * @param <T>
     * @param <R>
     * @since 1.7
     */
    protected <T, R> void bindPreference(Property<T> property, String prefName, R defaultValue,
                                         Function<T, R> saveConverter, Function<R, T> loadConverter) {
        this.bindPreference(property, prefName, defaultValue, saveConverter, loadConverter, null);
    }

    /**
     * Bind a {@link Property} with a preference and default value bidirectionally.
     * Works for the property value type is the same as preference value type but needs to do some extra work when saving.
     *
     * @param property
     * @param prefName
     * @param defaultValue
     * @param onPropertyChange This will be called before saving value to preference storage.
     * @param <T>
     */
    protected <T> void bindPreference(Property<T> property, String prefName, T defaultValue, Consumer<T> onPropertyChange) {
        this.bindPreference(property, prefName, defaultValue, t -> t, r -> r, onPropertyChange);
    }

    /**
     * Bind one {@link Property} with a preference and default value bidirectionally.
     * NOTE: Some components like ChoiceBox, in the initial state, giving a default value will cause the component to be automatically selected,
     * but this change will not trigger saving to preference.
     * So for such components, if a default item needs to be automatically selected and be saved, it should be handled separately.
     *
     * @param property         The property value of the component
     * @param prefName         One preference name can bind multiple properties (for like a group of radio buttons)
     * @param defaultValue     The default value if no preference exists by prefName
     * @param saveConverter    Convert T to R before saving to preference, if returns null, no preference will be saved.
     * @param loadConverter    Convert R to T before loading to Property of the component.
     * @param onPropertyChange This will be called when property of the component changes and before saving changes to preference.
     * @param <T>
     * @param <R>
     * @since 1.7
     */
    protected <T, R> void bindPreference(Property<T> property, String prefName, R defaultValue,
                                         Function<T, R> saveConverter, Function<R, T> loadConverter, Consumer<T> onPropertyChange) {
        bondPrefMap.put(prefName, new Pref<>(property, defaultValue, loadConverter));
        ChangeListener<T> changeListener = (observable, oldValue, newValue) -> {
            if (onPropertyChange != null) {
                onPropertyChange.accept(newValue);
            }
            if (isLoaded) {
                log.debug("Save preference: %s".formatted(prefName));
                R converted = saveConverter.apply(property.getValue());
                if (converted != null) {
                    fxPreferences.savePreference(prefName, converted);
                    this.onSave(true, newValue); // new value as payload
                }
            }
        };
        property.addListener(changeListener);
    }

    /**
     * Bind a {@link Spinner} with a preference.
     *
     * @param spinner
     * @param min
     * @param max
     * @param step
     * @param prefName
     * @param defaultValue
     * @since 1.7
     */
    protected void bindSpinner(Spinner<Integer> spinner, int min, int max, int step, String prefName, int defaultValue) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, fxPreferences.getPreference(prefName, defaultValue), step));
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            fxPreferences.savePreference(prefName, newValue);
            this.onSave(true, newValue); // new value as payload
        });
    }

    public void loadPreferences() {
        for (String prefName : bondPrefMap.keySet()) {
            Collection<Pref> prefs = bondPrefMap.get(prefName);
            for (Pref pref : prefs) {
                if (log.isTraceEnabled()) log.trace("Load preference: %s=%s".formatted(prefName, pref));
                Property property = pref.getProperty();
                Object defaultValue = pref.getDefaultValue();
                Object prefValue = this.loadPreferenceValueByDefault(prefName, property, defaultValue);
                if (prefValue != null) {
                    if (log.isTraceEnabled())
                        log.trace("Apply preference value to property of component: %s".formatted(prefValue));
                    Object v = pref.getConverter().apply(prefValue);
                    property.setValue(v);
                }
            }
        }
        this.initComponentsFromPreferences();
        this.isLoaded = true;
    }


    /**
     * Reset all preferences to default and save, UI of this panel also will be refreshed.
     */
    public void resetToDefault() {
        for (String prefName : bondPrefMap.keySet()) {
            Collection<Pref> prefs = bondPrefMap.get(prefName);
            for (Pref<?, ?> pref : prefs) {
                Property property = pref.getProperty();
                Object defaultValue = pref.getDefaultValue();
                Object v = pref.getConverter().apply(genericValue(defaultValue));
                property.setValue(v);
            }
        }
    }

    // @since 1.7
    private <T> T genericValue(Object defaultValue) {
        if (defaultValue instanceof Boolean) {
            return (T) defaultValue;
        }
        else if (defaultValue instanceof Integer) {
            return (T) defaultValue;
        }
        else if (defaultValue instanceof String) {
            return (T) defaultValue;
        }
        else if (defaultValue instanceof Double) {
            return (T) defaultValue;
        }
        else if (defaultValue instanceof List) {
            return (T) defaultValue;
        }
        else {
            return null;
        }
    }


    private <T> T loadPreferenceValueByDefault(String prefName, Property<?> property, Object defaultValue) {
        if (defaultValue == null) {
            Object preference = fxPreferences.getPreference(prefName);
            if (preference != null) {
                return (T) preference;
            }
            return null;
        }
        if (defaultValue instanceof Boolean) {
            Boolean preference = fxPreferences.getPreference(prefName, Boolean.class, (Boolean) defaultValue);
            return (T) preference;
        }
        else if (defaultValue instanceof Integer) {
            Integer preference = fxPreferences.getPreference(prefName, Integer.class, (Integer) defaultValue);
            return (T) preference;
        }
        else if (defaultValue instanceof String) {
            String preference = fxPreferences.getPreference(prefName, String.class, (String) defaultValue);
            return (T) preference;
        }
        else if (defaultValue instanceof Double) {
            Double preference = fxPreferences.getPreference(prefName, Double.class, (Double) defaultValue);
            return (T) preference;
        }
        else if (defaultValue instanceof List) {
            List<String> preferenceList = (List<String>) fxPreferences.getPreference(prefName, (List) defaultValue);
            ListProperty<String> value = new SimpleListProperty<>();
            value.addAll(preferenceList);
            return (T) value;
        }
        else if (property instanceof ObjectProperty) {
            // TODO
            return null;
        }
        else {
            return null;
        }
    }

//    private <T> T loadPreferenceValue(String prefName, Property property, Object defaultValue) {
//        if (property instanceof BooleanProperty) {
//            Boolean preference = fxPreferences.getPreference(prefName, Boolean.class, (Boolean) defaultValue);
//            return (T) preference;
////            property.setValue(preference);
//        }
//        else if (property instanceof IntegerProperty) {
//            Integer preference = fxPreferences.getPreference(prefName, Integer.class, (Integer) defaultValue);
//            return (T) preference;
////            property.setValue(preference);
//        }
//        else if (property instanceof StringProperty) {
//            String preference = fxPreferences.getPreference(prefName, String.class, (String) defaultValue);
//            return (T) preference;
////            property.setValue(preference);
//        }
//        else if (property instanceof DoubleProperty) {
//            Double preference = fxPreferences.getPreference(prefName, Double.class, (Double) defaultValue);
//            return (T) preference;
////            property.setValue(preference);
//        }
//        else if (property instanceof ListProperty) {
//            List<String> preferenceList = (List<String>) fxPreferences.getPreference(prefName, (List) defaultValue);
//            ListProperty<String> value = new SimpleListProperty<>();
//            value.addAll(preferenceList);
//            return (T) value;
////            property.setValue(value);
//        }
//        else if (property instanceof ObjectProperty) {
//            return null;
//        }
//        else {
//            return null;
//        }
//    }

    /**
     * Override this method to load your own customized preferences.
     */
    protected void initComponentsFromPreferences() {
        log.info("No customized preferences loaded for %s".formatted(this.getClass()));
    }

    /**
     * Be called after preference has been saved to storage.
     *
     * @param notify
     * @param payload
     */
    protected abstract void onSave(boolean notify, Object payload);

    public static class Pref<T, R> {
        Property<T> property;
        Object defaultValue;
        Function<R, T> converter; // convert from R to T for loading preference.

        public Pref(Property<T> property, Object defaultValue, Function<R, T> converter) {
            this.property = property;
            this.defaultValue = defaultValue;
            this.converter = converter;
        }

        public Property<T> getProperty() {
            return property;
        }

        public void setProperty(Property<T> property) {
            this.property = property;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Function<R, T> getConverter() {
            return converter;
        }

        public void setConverter(Function<R, T> converter) {
            this.converter = converter;
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
