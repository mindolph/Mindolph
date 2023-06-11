package com.mindolph.core.search;

import org.apache.commons.lang3.StringUtils;

import java.util.function.BiFunction;


/**
 * @author mindolph.com@gmail.com
 * @since 1.3
 */
public abstract class BaseSearchMatcher implements SearchMatcher {

    private static final String GRAPHICAL_LINE_BREAKER = "⏎";
    
    protected boolean returnContextEnabled;

    /**
     * Context string for matched text
     */
    protected String matchContext;

    public BaseSearchMatcher(boolean returnContextEnabled) {
        this.returnContextEnabled = returnContextEnabled;
    }

    @Override
    public String getMatchContext() {
        return matchContext;
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
        String normalText = normalizeSpace(text);
        String normalKeyword = normalizeSpace(searchParams.getKeywords());
        int start = indexOf(searchParams, normalText);

        String extracted = StringUtils.strip(StringUtils.substring(normalText, Math.max(0, start - extraSize), start + normalKeyword.length() + extraSize));
        StringBuilder buf = new StringBuilder();
        if (start - extraSize > 0) {
            buf.append("...");
        }
        buf.append(extracted);
        if (start + normalKeyword.length() + extraSize < normalText.length()) {
            buf.append("...");
        }
        return buf.toString();
    }

    /**
     * @param searchParams
     * @param text
     * @return
     */
    public static int indexOf(SearchParams searchParams, String text) {
        BiFunction<CharSequence, CharSequence, Integer> indexOf = searchParams.isCaseSensitive() ? StringUtils::indexOf : StringUtils::indexOfIgnoreCase;
        String normalText = normalizeSpace(text);
        String normalKeyword = normalizeSpace(searchParams.getKeywords());
        return indexOf.apply(normalText, normalKeyword);
    }

    public static int lastIndexOf(SearchParams searchParams, String text) {
        BiFunction<CharSequence, CharSequence, Integer> indexOf = searchParams.isCaseSensitive() ? StringUtils::lastIndexOf : StringUtils::lastIndexOfIgnoreCase;
        String normalText = normalizeSpace(text);
        String normalKeyword = normalizeSpace(searchParams.getKeywords());
        return indexOf.apply(normalText, normalKeyword);
    }

    public static String normalizeSpace(String text) {
        String replaced = StringUtils.replace(text, "\n", GRAPHICAL_LINE_BREAKER);
        return StringUtils.normalizeSpace(replaced);
    }
}
