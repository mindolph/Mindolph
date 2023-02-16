package com.mindolph.mindmap;

import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 */
public class I18n {

    private static final I18n ins = new I18n();

    private final ResourceBundle bundle;

    private I18n() {
        bundle = ResourceBundle.getBundle("i18n/mmd");
    }

    public static synchronized I18n getIns() {
        return ins;
    }

    public String getString(String key) {
        return bundle.getString(key);
    }
}
