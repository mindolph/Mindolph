package com.mindolph.mindmap.theme;

import com.mindolph.mindmap.constant.MindMapConstants;
import com.mindolph.mindmap.util.PrefObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author allen
 */
public class CustomTheme extends MindMapTheme {

    private static final Logger log = LoggerFactory.getLogger(CustomTheme.class);

    /**
     * The name given by user. TBD
     */
    protected String themeName;

    public CustomTheme(String themeName) {
        this.themeName = themeName;
    }

    /**
     * Copy values from other theme object.
     *
     * @param theme
     */
    public void copyFromTheme(MindMapTheme theme) {
        for (Field f : MindMapTheme.class.getDeclaredFields()) {
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
                continue;
            }
            if (f.getType() == MindMapTheme.class) {
                continue;
            }
            try {
                f.set(this, f.get(theme));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void loadFromPreferences() {
        log.debug("Load from preferences");
        PrefObjectUtils.load(this, null, MindMapTheme.class, makeKey());
    }

    public void saveToPreferences() {
        PrefObjectUtils.save(this, MindMapTheme.class, makeKey());
    }

    /**
     * Delete the custom theme from preference.
     */
    public void deleteFromPreference() {
        PrefObjectUtils.delete(MindMapTheme.class, makeKey());
    }

    private String makeKey() {
        return "%s.%s.%s".formatted(MindMapConstants.CFG_PREFIX, "theme", themeName);
    }

}
