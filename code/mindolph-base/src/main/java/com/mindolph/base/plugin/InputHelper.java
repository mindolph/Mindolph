package com.mindolph.base.plugin;

import java.util.List;

/**
 * @author mindolph.com@gmail.com
 * @see Plugin
 * @since 1.6
 */
public interface InputHelper {

    /**
     * Get all help words for the editor identified by editorId.
     *
     * @param editorId
     * @return
     */
    List<String> getHelpWords(Object editorId);

    /**
     * Call this method when context text has been changed to update the help words.
     *
     * @param editorId
     * @param text
     */
    void updateContextText(Object editorId, String text);
}
