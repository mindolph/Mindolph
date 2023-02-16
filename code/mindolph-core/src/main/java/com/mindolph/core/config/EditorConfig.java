package com.mindolph.core.config;

/**
 * Configuration of editor, should be shared between same type of editor instances.
 *
 * @author mindolph.com@gmail.com
 */
public interface EditorConfig {

    /**
     * Load config from preferences.
     */
    void loadFromPreferences();
}
