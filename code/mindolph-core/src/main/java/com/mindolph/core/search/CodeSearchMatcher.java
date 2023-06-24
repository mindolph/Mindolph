package com.mindolph.core.search;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

/**
 * @author mindolph.com@gmail.com
 */
public class CodeSearchMatcher extends BaseSearchMatcher {
    private static final Logger log = LoggerFactory.getLogger(CodeSearchMatcher.class);

    public CodeSearchMatcher(boolean returnContextEnabled) {
        super(returnContextEnabled);
    }

    @Override
    public boolean matches(File file, SearchParams searchParams) {
        super.matches(file, searchParams);
        log.debug("try match in file: " + file);
        try {
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
//            String normalText = SearchUtils.normalizeSpace(text);
//            String normalKeyword = searchParams.getNormalizedKeyword();
            Matcher matcher = searchParams.getPattern().matcher(text);
            boolean contains = false;
            int last = 0;// to skip matched positions that already be extracted in previous.
            TextNavigator navigator = new TextNavigator();
            navigator.setText(text, false);
            while (matcher.find()) {
                contains = true;
                if (!returnContextEnabled) {
                    return true;
                }
                int start = matcher.start();
                int end = matcher.end();
                if (start < last) {
                    continue; // skip because it's already been extracted.
                }
                log.debug(String.format("matched: %d - %d", start, end));
                String matched = super.extractInText(text, start, end, 64);
                super.addMatched(new MatchedItem(SearchUtils.normalizeSpace(matched), new TextAnchor(navigator.convert(start, end))));
                last = end + 64 - searchParams.getKeywords().length(); // 3 is for `...`
            }
            return contains;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
