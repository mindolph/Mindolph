package com.mindolph.core.search;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

/**
 * @author mindolph.com@gmail.com
 */
public class CodeSearchMatcher extends BaseSearchMatcher {

    public CodeSearchMatcher(boolean returnContextEnabled) {
        super(returnContextEnabled);
    }

    @Override
    public boolean matches(File file, SearchParams searchParams) {
        super.matches(file, searchParams);
        try {
            String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            String normalText = SearchUtils.normalizeSpace(text);
            String normalKeyword = searchParams.getNormalizedKeyword();
            Matcher matcher = searchParams.getPattern().matcher(normalText);
            boolean contains = false;
            int last = 0;// to skip matched positions that already be extracted in previous.
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
                String matched = this.extractInText(normalText, start, end, 64);
                super.addMatched(matched);
                last = end + 64 - normalKeyword.length(); // 3 is for `...`
            }
            return contains;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
