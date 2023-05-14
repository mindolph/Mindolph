package com.mindolph.core.search;

import org.apache.commons.lang3.StringUtils;

import java.util.function.BiFunction;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author mindolph.com@gmail.com
 * @since 1.3
 */
public abstract class BaseSearchMatcher implements SearchMatcher{

    protected boolean returnContextEnabled;

    protected String matchContext;

    public BaseSearchMatcher(boolean returnContextEnabled) {
        this.returnContextEnabled = returnContextEnabled;
    }

    @Override
    public String getMatchContext() {
        return matchContext;
    }

    /**
     *
     * @param searchParams
     * @param text
     * @param padding
     * @return
     */
    protected String extractInText(SearchParams searchParams, String text, int padding) {
        String normalText = normalizeSpace(text);
        String normalKeyword = normalizeSpace(searchParams.getKeywords());
        int start = indexOf(searchParams, normalText);

        String extracted = strip(substring(normalText, Math.max(0, start - padding), start + normalKeyword.length() + padding));
        StringBuilder buf = new StringBuilder();
        if (start - padding > 0) {
            buf.append("...");
        }
        buf.append(extracted);
        if (start + normalKeyword.length() + padding < normalText.length()){
            buf.append("...");
        }
        return buf.toString();
    }

    /**
     *
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
}
