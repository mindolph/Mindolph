package com.mindolph.base.plugin;

import java.util.List;

/**
 * @author mindolph.com@gmail.com
 * @see Plugin
 * @since 1.6
 */
public interface InputHelper {

    List<String> getHelpWords();

    /**
     * Call this method when context text has been changed to update the help words.
     *
     * @param text
     */
    void updateContextText(String text);
}
