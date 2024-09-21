package com.mindolph.base.constant;

import com.mindolph.core.constant.SyntaxConstants;

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

    String HEADING_PATTERN = "(^|" + LINE_SEPARATOR + ")(#+\\s+[\\s\\S]*?(?=" + LINE_SEPARATOR + "))";
    String LIST_PATTERN = "(^|" + LINE_SEPARATOR + ")[\\t ]*((\\* )|(\\+ )|(- )|(\\d. ))";
    // "(\\|[\\s\\S]*?)+\\|" +
    String TABLE_SEPARATOR = LINE_SEPARATOR + "(\\|-+)+\\|" + LINE_SEPARATOR;
    String TABLE_PATTERN =  "(\\|)|(" + TABLE_SEPARATOR + ")";
    
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
    String CODE_PATTERN = "`[\\s\\S]*?`";
    String CODE_BLOCK_PATTERN = "`{3}[\\s\\S]*?`{3}";
    String QUOTE_PATTERN = "> [\\s\\S]*?(?=" + LINE_SEPARATOR + ")";
    String URL_PATTERN = "(!?\\[[\\s\\S]*?\\])(\\([\\s\\S]*?\\))?";

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
        Matcher matcher = pattern.matcher("_not italic");
        // System.out.println(matcher.groupCount());
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
                                                                                    matcher.group("QUOTE") != null ? "quote" :
                                                                                            matcher.group("URL") != null ? "url" :
                                                                                                    "unknown";
            System.out.println(styleClass);
        }
    }
}
