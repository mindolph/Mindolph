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
    String LIST_PATTERN = "%s%s*((\\* )|(\\+ )|(- )|(\\d+\\. ))".formatted(LINE_START, BLANK_CHAR);
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
    String CODE_PATTERN = "(`[\\s\\S]*?`)";
    String CODE_BLOCK_PATTERN = BLANK_CHAR + "*`{3}[\\s\\S]*?`{3}";
    String QUOTE_PATTERN = "(%s%s*> [\\s\\S]*?)(?=%s)".formatted(LINE_START, BLANK_CHAR, LINE_SEPARATOR);
    String URL_PATTERN = "(!?\\[[\\s\\S]*?\\])(\\([\\s\\S]*?\\))?";
    String HORIZONTAL_RULE_PATTERN = "((--[-]+%s*?(?=%s))|(\\*\\*[\\*]+%s*?(?=%s))|(__[_]+%s*?(?=%s)))".formatted(BLANK_CHAR, LINE_SEPARATOR, BLANK_CHAR, LINE_SEPARATOR, BLANK_CHAR, LINE_SEPARATOR);

    /**
     * Testing.
     *
     * @param args
     */
    static void main(String[] args) {
        Pattern pattern = Pattern.compile(
                "(?<HEADING>" + HEADING_PATTERN + ")"
                        + "|(?<LIST>" + LIST_PATTERN + ")"
                        + "|(?<TABLE>" + TABLE_PATTERN + ")"
                        + "|(?<QUOTE>" + QUOTE_PATTERN + ")"
                        + "|(?<URL>" + URL_PATTERN + ")"
        );
        System.out.println(LIST_PATTERN);
//        String text = "> hello1\n # heading\n  > hello `every`w`body` ==\n";
//        String text = "|A|B|C|\n|:---|:----:|---:|\n";
//        String text = " `' *foobar*`  `' **foobar**` **`foobar`**";
//        String text = "hello `abc` foobar";
//        String text = "\n---\n---- \n***\n**** \n___\n____ \nfoo `bar`\n";
//        String text = "```\n [key] \n```";
        String text = "* \uD83D\uDE02 hello\n";
//        String text = "> \uD83D\uDE02 hello\n";
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String styleClass =
                    matcher.group("HEADING") != null ? "heading" :
                            matcher.group("LIST") != null ? "list" :
                                    matcher.group("TABLE") != null ? "table" :
                                            matcher.group("QUOTE") != null ? "md-quote" :
                                                    matcher.group("URL") != null ? "url" :
                                                            "unknown";
            System.out.printf("matched %s: (%d-%d) %n", styleClass, matcher.start(), matcher.end());
            System.out.println(StringUtils.substring(text, matcher.start(), matcher.end()));
        }

        Pattern pattern2 = Pattern.compile(
                "(?<HRULE>" + HORIZONTAL_RULE_PATTERN + ")"
                        + "|(?<BOLDITALIC>" + BOLD_ITALIC_PATTERN + ")"
                        + "|(?<BOLD>" + BOLD_PATTERN + ")"
                        + "|(?<ITALIC>" + ITALIC_PATTERN + ")"
                        + "|(?<CODEBLOCK>" + CODE_BLOCK_PATTERN + ")"
                        + "|(?<CODE>" + CODE_PATTERN + ")"
        );
        Matcher matcher2 = pattern2.matcher(text);
        while (matcher2.find()) {
            System.out.println();
            String styleClass = matcher2.group("HRULE") != null ? "hrule" :
                    matcher2.group("BOLD") != null ? "bold" :
                            matcher2.group("ITALIC") != null ? "italic" :
                                    matcher2.group("BOLDITALIC") != null ? "bold-italic" :
                                            matcher2.group("CODEBLOCK") != null ? "code-block" :
                                                    matcher2.group("CODE") != null ? "code" :
                                                            null;
            System.out.printf("matched %s: (pos: %d-%d, count: %d) %n", styleClass, matcher2.start(), matcher2.end(), matcher2.groupCount());
            System.out.println(StringUtils.substring(text, matcher2.start(), matcher2.end()));
        }
    }
}
