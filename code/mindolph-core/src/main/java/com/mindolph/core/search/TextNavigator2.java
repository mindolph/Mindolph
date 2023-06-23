package com.mindolph.core.search;

import org.apache.commons.lang3.Range;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO to be refactored
 * @author mindolph.com@gmail.com
 * @since 1.3.4
 */
public class TextNavigator2 extends TextNavigator {

    /**
     * TODO consider replace parent's implementation with this one and remove this class.
     *
     * @param text
     * @param resetCursor false if not reset cursor for some cases like text replacement.
     */
    @Override
    public void setText(String text, boolean resetCursor) {
        Pattern linePattern = Pattern.compile("\n");
        Matcher matcher = linePattern.matcher(text);
        int last = 0;
        while (matcher.find()) {
            ranges.add(Range.between(last, matcher.start()));
            last = matcher.end();
        }
        ranges.add(Range.between(last, text.length()));
    }

}
