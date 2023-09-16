package com.mindolph.mindmap.util;

import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mindmap.theme.BorderType;
import com.mindolph.mindmap.theme.ConnectorStyle;
import com.mindolph.mindmap.theme.MindMapTheme;
import com.mindolph.mindmap.theme.ThemeType;
import org.swiftboot.util.BeanUtils;
import org.swiftboot.util.pref.StringConverter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author mindolph
 */
public class PrefObjectUtils {

    static {
        FxPreferences.getInstance().addConverter(ThemeType.class, new StringConverter<ThemeType>() {
            @Override
            public ThemeType deserialize(String prefValue) {
                return ThemeType.valueOf(prefValue);
            }

            @Override
            public String serialize(ThemeType valueObject) {
                return valueObject.name();
            }
        });
        FxPreferences.getInstance().addConverter(ConnectorStyle.class, new StringConverter<ConnectorStyle>() {
            @Override
            public ConnectorStyle deserialize(String prefValue) {
                return ConnectorStyle.valueOf(String.valueOf(prefValue));
            }

            @Override
            public String serialize(ConnectorStyle valueObject) {
                return valueObject.name();
            }
        });
        FxPreferences.getInstance().addConverter(BorderType.class, new StringConverter<BorderType>() {
            @Override
            public BorderType deserialize(String prefValue) {
                return BorderType.valueOf(String.valueOf(prefValue));
            }

            @Override
            public String serialize(BorderType valueObject) {
                return valueObject.name();
            }
        });
    }

    /**
     * Load Java Preferences to prefObject, if it doesn't exist, get it from parentPrefObject.
     * NOTE: Fields in preference object will not be loaded.
     *
     * @param prefObject       object to store preferences
     * @param parentPrefObject parent object for default values
     * @param clazz            the class that defines all preference keys.
     * @param prefix
     */
    public static void load(Object prefObject, Object parentPrefObject, Class<?> clazz, String prefix) {
        // reflect fields from parent object, otherwise there might be new fields declared in the sub-object that don't exist in parent object,
        // which means no fields in preference will be loaded.
        for (Field f : clazz.getDeclaredFields()) {
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
                continue;
            }
            String key = "%s.%s".formatted(prefix, f.getName());
            try {
                // if parent preference object exists, otherwise the default value is as the same with preference object(usually is null)
                Object defaultFromObject = parentPrefObject != null ? parentPrefObject : prefObject;
                Object val = FxPreferences.getInstance().getPreference(key, f, defaultFromObject);
                if (val == null) {
                    continue;
                }
                f.set(prefObject, val);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Save all values to Java Preference from the prefObject,
     * excludes the static, transient, final fields
     * and any field that is the instance of MindMapTheme or ThemeType.
     *
     * @param prefObject the object stores preferences.
     * @param clazz      the class must be the Class that defines the fields that you want to save.
     * @param prefix
     */
    public static void save(Object prefObject, Class<?> clazz, String prefix) {
        for (Field f : clazz.getDeclaredFields()) {
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
                continue;
            }
            if (f.getType() == MindMapTheme.class) {
                continue;
            }
            String key = "%s.%s".formatted(prefix, f.getName());
            try {
                Object v = BeanUtils.forceGetProperty(prefObject, f);
                // System.out.printf("Save %s to %s%n", v, key);
                FxPreferences.getInstance().savePreference(key, v);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        FxPreferences.getInstance().flush();
    }

    /**
     * TODO This doesn't really delete the preference from storage.
     *
     * @param clazz
     * @param prefix
     */
    public static void delete(Class<?> clazz, String prefix) {
        for (Field f : clazz.getDeclaredFields()) {
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
                continue;
            }
            if (f.getType() == MindMapTheme.class) {
                continue;
            }
            String key = "%s.%s".formatted(prefix, f.getName());
            try {
                // System.out.println("remove: " + key);
                FxPreferences.getInstance().removePreference(key);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        FxPreferences.getInstance().flush();
    }
}
