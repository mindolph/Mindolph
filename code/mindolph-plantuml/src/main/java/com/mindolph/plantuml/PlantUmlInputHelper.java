package com.mindolph.plantuml;

import com.mindolph.base.plugin.InputHelper;
import com.mindolph.plantuml.constant.PlantUmlConstants;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public class PlantUmlInputHelper implements InputHelper {
    private static final Logger log = LoggerFactory.getLogger(PlantUmlInputHelper.class);

    private final List<String> preDefinedWords = new ArrayList<>();
    private final List<String> contextWords = new ArrayList<>();

    public PlantUmlInputHelper() {
        preDefinedWords.addAll(List.of(PlantUmlConstants.KEYWORDS));
        preDefinedWords.addAll(List.of(PlantUmlConstants.DIAGRAM_KEYWORDS));
        preDefinedWords.addAll(List.of(PlantUmlConstants.CONTAINING_KEYWORDS));
        preDefinedWords.addAll(List.of(PlantUmlConstants.DIRECTIVE));
    }

    @Override
    public List<String> getHelpWords() {
        List<String> ret = ListUtils.union(preDefinedWords, contextWords);
        return ret;
    }

    @Override
    public boolean isSupportContextWords() {
        return true;
    }

    @Override
    public void updateContextWords(String text) {
        // TODO
        String[] split = StringUtils.split(text, " \t\r");
        contextWords.addAll(Arrays.stream(split).map(StringUtils::trim).toList());
        log.debug("%d context words updated.".formatted(split.length));
    }

}
