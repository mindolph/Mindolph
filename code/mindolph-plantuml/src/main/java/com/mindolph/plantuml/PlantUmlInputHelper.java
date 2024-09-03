package com.mindolph.plantuml;

import com.mindolph.base.plugin.InputHelper;
import com.mindolph.plantuml.constant.PlantUmlConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public class PlantUmlInputHelper implements InputHelper {
    private static final Logger log = LoggerFactory.getLogger(PlantUmlInputHelper.class);

    private final List<String> preDefinedWords = new ArrayList<>();


    public PlantUmlInputHelper() {
        preDefinedWords.addAll(List.of(PlantUmlConstants.KEYWORDS));
        preDefinedWords.addAll(List.of(PlantUmlConstants.DIAGRAM_KEYWORDS_START));
        preDefinedWords.addAll(List.of(PlantUmlConstants.CONTAINING_KEYWORDS));
        preDefinedWords.addAll(List.of(PlantUmlConstants.DIRECTIVE));
    }

    @Override
    public List<String> getHelpWords(Object editorId) {
        return preDefinedWords;
    }


    @Override
    public void updateContextText(Object editorId, String text) {
        // DO NOTHING.
    }

}
