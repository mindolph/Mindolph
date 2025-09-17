package com.mindolph.base.constant;

import com.mindolph.core.constant.SyntaxConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;

/**
 * @author mindolph
 * @since 1.4
 */
public interface MarkdownConstants extends SyntaxConstants {

    String EMPHASIS_KW = "[^*_\\r\\n\\t\\f\\v ]"; // visible letters but without '*' and '_
    String EMPHASIS_CONTENT = "[^*_\\r\\n\\f\\v]";
    String EMPHASIS = "(()|(" + EMPHASIS_KW + EMPHASIS_CONTENT + "*?" + EMPHASIS_KW + "))"; // this can't be used to match directly

    String HEADING_PATTERN = "%s(#+\\s+[\\s\\S]*?(?=%s))".formatted(LINE_START, LINE_END);
    String LIST_PATTERN = "%s[\\t ]*((\\* )|(\\+ )|(- )|(\\d+. ))".formatted(LINE_START);
    // "(\\|[\\s\\S]*?)+\\|" +
    String TABLE_SEPARATOR = LINE_SEPARATOR + "(\\|:?-{3,}:?)+\\|" + LINE_SEPARATOR;
    String TABLE_PATTERN = "(\\|)|(" + TABLE_SEPARATOR + ")";

    String BOLD_ITALIC_PATTERN = "((\\*\\*\\*)" + EMPHASIS + "(\\*\\*\\*))"
            + "|((\\*\\*_)" + EMPHASIS + "(_\\*\\*))"
            + "|((\\*__)" + EMPHASIS + "(__\\*))"
            + "|((_\\*\\*)" + EMPHASIS + "(\\*\\*_))"
            + "|((__\\*)" + EMPHASIS + "(\\*__))"
            + "|((___)" + EMPHASIS + "(___))";
    String BOLD_PATTERN = "(\\*\\*)" + EMPHASIS + "(\\*\\*)"
            + "|((__)" + EMPHASIS + "(__))";
    String ITALIC_PATTERN = "(\\*" + EMPHASIS + "\\*)"
            + "|(_" + EMPHASIS + "_)";
    String CODE_PATTERN = BLANK_CHAR + "*`[\\s\\S]*?`";
    String CODE_BLOCK_PATTERN = BLANK_CHAR + "*`{3}[\\s\\S]*?`{3}";
    String QUOTE_PATTERN = "(%s%s*> [\\s\\S]*?)(?=%s)".formatted(LINE_START, BLANK_CHAR, LINE_SEPARATOR);
    String URL_PATTERN = "(!?\\[[\\s\\S]*?\\])(\\([\\s\\S]*?\\))?";

    /**
     * Testing.
     *
     * @param args
     */
    static void main(String[] args) {
        Pattern pattern = Pattern.compile(
                "(?<HEADING>" + HEADING_PATTERN + ")"
                        + "|(?<CODEBLOCK>" + CODE_BLOCK_PATTERN + ")"
                        + "|(?<BOLDITALIC>" + BOLD_ITALIC_PATTERN + ")"
                        + "|(?<BOLD>" + BOLD_PATTERN + ")"
                        + "|(?<ITALIC>" + ITALIC_PATTERN + ")"
                        + "|(?<LIST>" + LIST_PATTERN + ")"
                        + "|(?<TABLE>" + TABLE_PATTERN + ")"
                        + "|(?<CODE>" + CODE_PATTERN + ")"
                        + "|(?<QUOTE>" + QUOTE_PATTERN + ")"
                        + "|(?<URL>" + URL_PATTERN + ")"
        );
//        String text = "> hello1\n # heading\n  > hello2\n";
        String text = "|A|B|C|\n|:---|:----:|---:|\n";
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String styleClass =
                    matcher.group("HEADING") != null ? "heading" :
                            matcher.group("LIST") != null ? "list" :
                                    matcher.group("TABLE") != null ? "table" :
                                            matcher.group("BOLD") != null ? "bold" :
                                                    matcher.group("ITALIC") != null ? "italic" :
                                                            matcher.group("BOLDITALIC") != null ? "bold-italic" :
                                                                    matcher.group("CODE") != null ? "code" :
                                                                            matcher.group("CODEBLOCK") != null ? "code-block" :
                                                                                    matcher.group("QUOTE") != null ? "md-quote" :
                                                                                            matcher.group("URL") != null ? "url" :
                                                                                                    "unknown";
            System.out.printf("style: %s(%d-%d) %n", styleClass, matcher.start(), matcher.end());
            System.out.println(StringUtils.substring(text, matcher.start(), matcher.end()));
        }
    }
}
