package com.mindolph.core.constant;

import org.apache.commons.lang3.RegExUtils;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mindolph.core.constant.SyntaxConstants.EMOJI_CHAR;

/**
 *
 */
public class SyntaxConstantTest {


    @Test
    public void emoji() {
            String text = "<div>foo 👶 bar</div>";
            String pattern = "([%s]+)".formatted(EMOJI_CHAR);
            System.out.println(pattern);
            Pattern p = Pattern.compile(pattern);
            Matcher matcher = p.matcher(text);
            while (matcher.find()) {
                System.out.printf("Found emoji: %s at position %d%n", matcher.group(), matcher.start());
            }
            System.out.println(p.matcher(text).find());

            String s = RegExUtils.replaceAll(text, pattern, "<span>$1</span>");
            System.out.println(s);
    }
}
