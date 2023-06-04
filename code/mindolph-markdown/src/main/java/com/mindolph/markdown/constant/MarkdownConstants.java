package com.mindolph.markdown.constant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;

/**
 * @author allen
 * @since 1.4
 */
public interface MarkdownConstants {

    String EMPHASIS_KW = "[^*_\\r\\n\\t\\f\\v ]"; // visible letters but without '*' and '_
    String EMPHASIS = "(()|(" + EMPHASIS_KW + "[\\s\\S]*?" + EMPHASIS_KW + "))"; // this can't be used to match directly

    String HEADING_PATTERN = "#+[\\s\\S]*?" + LINE_SEPARATOR;
    String LIST_PATTERN = "[\\t ]*((\\* )|(\\+ )|(- )|(\\d. ))";
    String BOLD_ITALIC_PATTERN = "((\\*\\*\\*)" + EMPHASIS + "(\\*\\*\\*))"
            + "|((\\*\\*_)" + EMPHASIS + "(_\\*\\*))"
            + "|((\\*__)" + EMPHASIS + "(__\\*))"
            + "|((_\\*\\*)" + EMPHASIS + "(\\*\\*_))"
            + "|((__\\*)" + EMPHASIS + "(\\*__))"
            + "|((___)" + EMPHASIS + "(___))";
    String BOLD_PATTERN_1 = "(\\*\\*)" + EMPHASIS + "(\\*\\*)";
    String BOLD_PATTERN_2 = "(__)" + EMPHASIS + "(__)";
    String ITALIC_PATTERN_1 = "\\*" + EMPHASIS + "\\*";
    String ITALIC_PATTERN_2 = "_" + EMPHASIS + "_";
    String CODE_PATTERN = "`[\\s\\S]*?`";
    String CODE_BLOCK_PATTERN = "`{3}[\\s\\S]*?`{3}";
    String QUOTE_PATTERN = "> [\\s\\S]*?" + LINE_SEPARATOR;
    String URL_PATTERN = "(!?\\[[\\s\\S]*?\\])(\\([\\s\\S]*?\\))?";

    static void main(String[] args) {
        Pattern pattern = Pattern.compile(
                "(?<HEADING>" + HEADING_PATTERN + ")"
                        + "|(?<BOLDITALIC>" + BOLD_ITALIC_PATTERN + ")"
                        + "|(?<BOLD1>" + BOLD_PATTERN_1 + ")"
                        + "|(?<BOLD2>" + BOLD_PATTERN_2 + ")"
                        + "|(?<ITALIC1>" + ITALIC_PATTERN_1 + ")"
                        + "|(?<ITALIC2>" + ITALIC_PATTERN_2 + ")"
                        + "|(?<LIST>" + LIST_PATTERN + ")"
                        + "|(?<CODE>" + CODE_PATTERN + ")"
                        + "|(?<CODEBLOCK>" + CODE_BLOCK_PATTERN + ")"
                        + "|(?<QUOTE>" + QUOTE_PATTERN + ")"
                        + "|(?<URL>" + URL_PATTERN + ")"
        );
        Matcher matcher = pattern.matcher("[Google](http://www.google.com) [Facebook](http://www.facebook.com)");
        System.out.println(matcher.groupCount());
        while (matcher.find()) {
            String styleClass =
                    matcher.group("HEADING") != null ? "heading" :
                            matcher.group("LIST") != null ? "list" :
                                    matcher.group("BOLD1") != null ? "bold" :
                                            matcher.group("BOLD2") != null ? "bold" :
                                                    matcher.group("ITALIC1") != null ? "italic" :
                                                            matcher.group("ITALIC2") != null ? "italic" :
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
