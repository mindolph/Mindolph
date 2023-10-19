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
     * If true, the input helper should parse the whole file and extract all words for helping.
     * @return
     */
    boolean isSupportContextWords();

    void updateContextWords(String text);
}
