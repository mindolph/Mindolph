package com.mindolph.core.constant;

import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;

/**
 * @since 1.8.5
 */
public interface SyntaxConstants {

    // blank char excludes line break
    String BLANK_CHAR = "[ \\f\\t]";

    // one or more blank chars excludes line break
    String BLANK_CHARS = "[ \\f\\t]+";

    String LINE_START = "(^|%s)".formatted(LINE_SEPARATOR);

    String LINE_END = "($|%s)".formatted(LINE_SEPARATOR);

    String EMOJI_CHAR = "\\u2600-\\u27BF\\uD83C\\uD000-\\uD83C\\uDFFF\\uD83D\\uDC00-\\uD83D\\uDDFF\\uD83E\\uDD00-\\uD83E\\uDDFF";

}
