package com.mindolph.core.search;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;


/**
 * @author mindolph.com@gmail.com
 * @since 1.3
 */
public abstract class BaseSearchMatcher implements SearchMatcher {

    private final Logger log = LoggerFactory.getLogger(BaseSearchMatcher.class);

    protected boolean returnContextEnabled;

    protected List<String> matched;

    public BaseSearchMatcher(boolean returnContextEnabled) {
        this.returnContextEnabled = returnContextEnabled;
    }

    @Override
    public boolean matches(File file, SearchParams searchParams) {
        this.matched = new ArrayList<>();
        return false;
    }


    protected String extractInText(String text, int start, int end, int extraSize) {
        String extracted = StringUtils.strip(StringUtils.substring(text, Math.max(0, start - extraSize), end + extraSize));
        log.debug("extract from %d to %d: '%s'".formatted(start, end, extracted));
        StringBuilder buf = new StringBuilder();
        if (start - extraSize > 0) {
            buf.append("...");
        }
        buf.append(extracted);
        if (end + extraSize < text.length()) {
            buf.append("...");
        }
        return buf.toString();
    }

    /**
     * Extract more context text in provided text with searching keyword.
     *
     * @param searchParams
     * @param text
     * @param extraSize
     * @return
     */
    protected String extractInText(SearchParams searchParams, String text, int extraSize) {
        // normalize space before extracting rather than after because the context might contain with massive blank chars,
        // which is meaningless for displaying them.
        String normalText = SearchUtils.normalizeSpace(text);
        String normalKeyword = searchParams.getNormalizedKeyword();
        int start = indexOf(searchParams, normalText);
        int end = start + normalKeyword.length();
        return extractInText(normalText, start, end, extraSize);
    }

    protected void addMatched(String matched) {
        if (this.matched == null) {
            this.matched = new ArrayList<>();
        }
        this.matched.add(matched);
    }

    public List<String> getMatched() {
        return matched;
    }


    /**
     * @param searchParams
     * @param text
     * @return
     */
    public static int indexOf(SearchParams searchParams, String text) {
        BiFunction<CharSequence, CharSequence, Integer> indexOf = searchParams.isCaseSensitive() ? StringUtils::indexOf : StringUtils::indexOfIgnoreCase;
        String normalText = SearchUtils.normalizeSpace(text);
        String normalKeyword = searchParams.getNormalizedKeyword();;
        return indexOf.apply(normalText, normalKeyword);
    }

    public static int lastIndexOf(SearchParams searchParams, String text) {
        BiFunction<CharSequence, CharSequence, Integer> indexOf = searchParams.isCaseSensitive() ? StringUtils::lastIndexOf : StringUtils::lastIndexOfIgnoreCase;
        String normalText = SearchUtils.normalizeSpace(text);
        String normalKeyword = searchParams.getNormalizedKeyword();;
        return indexOf.apply(normalText, normalKeyword);
    }


}
