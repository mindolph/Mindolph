package com.mindolph.markdown.constant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mindolph.base.constant.MarkdownConstants.*;

/**
 * @author mindolph
 * @since 1.4
 */
public class MdRegexTest {

    @Test
    public void heading() {
        Pattern p = Pattern.compile(HEADING_PATTERN);
        Assertions.assertTrue(p.matcher("\n#Header 1\n").find());
        Assertions.assertTrue(p.matcher("\n## Header 2\n").find());
        Assertions.assertTrue(p.matcher("\n# \n").find());
        Assertions.assertTrue(p.matcher("\n## \n").find());
        Assertions.assertTrue(p.matcher("\n### \n").find());
        Assertions.assertFalse(p.matcher("\n#\n").find());
        Assertions.assertFalse(p.matcher("\n##\n").find());
        Assertions.assertFalse(p.matcher("\n###\n").find());
    }

    @Test
    public void list() {
        Pattern p = Pattern.compile(LIST_PATTERN);
        Assertions.assertTrue(p.matcher("* ").matches());
        Assertions.assertTrue(p.matcher(" * ").matches());
        Assertions.assertTrue(p.matcher("    * ").matches());
        Assertions.assertTrue(p.matcher("\t* ").matches());
        Assertions.assertTrue(p.matcher("\t * ").matches());
        Assertions.assertTrue(p.matcher(" \t* ").matches());
    }

    @Test
    public void bold() {
        Pattern p = Pattern.compile(BOLD_PATTERN);
        Assertions.assertTrue(p.matcher("**bold**").matches());
        Assertions.assertTrue(p.matcher("**is bold**").matches());
        Assertions.assertFalse(p.matcher("***is bold**").matches());
        Matcher matcher = p.matcher("""
                ***
                not valid bold
                **x**
                """);
        if (matcher.find()) {
            System.out.printf("%d %d%n", matcher.start(), matcher.end());
            Assertions.fail();
        }
    }

    @Test
    public void italic() {
        Pattern p = Pattern.compile(ITALIC_PATTERN);
        Assertions.assertTrue(p.matcher("*bold*").matches());
        Assertions.assertTrue(p.matcher("_bold_").matches());
        Assertions.assertFalse(p.matcher("_bold").matches());
        Assertions.assertFalse(p.matcher("bold_").matches());
        Matcher matcher = p.matcher("""
                _not


                italic_
                """);
        Assertions.assertFalse(matcher.find());
    }

    @Test
    public void boldItalic() {
        Pattern p = Pattern.compile(BOLD_ITALIC_PATTERN);
        Assertions.assertTrue(p.matcher("******").matches());
        Assertions.assertTrue(p.matcher("***emphasised***").matches());
        Assertions.assertTrue(p.matcher("___emphasised___").matches());
        Assertions.assertTrue(p.matcher("**_emphasised_**").matches());
        Assertions.assertTrue(p.matcher("_**emphasised**_").matches());
        Assertions.assertTrue(p.matcher("***to be emphasised***").matches());
        Assertions.assertFalse(p.matcher(" emphasised ").matches());
        Assertions.assertFalse(p.matcher(" emphasised").matches());
        Assertions.assertFalse(p.matcher("emphasised ").matches());

        Matcher multi = p.matcher("***emphasised1*** __emphasised2__");
        Assertions.assertTrue(multi.find());
        System.out.println(multi.group());
        for (int i = 0; i < multi.groupCount(); i++) {
            System.out.println(multi.group(i));
        }
    }

    @Test
    public void code() {
        Pattern p = Pattern.compile("(%s)|(%s)".formatted(CODE_PATTERN, CODE_BLOCK_PATTERN));
        Assertions.assertTrue(p.matcher("`code`").matches());
        Assertions.assertTrue(p.matcher("`code` and `code`").matches());
        Assertions.assertTrue(p.matcher("```code```").matches());
        Assertions.assertTrue(p.matcher("""
                ```
                my code
                ```
                """).find());// call find because the line break
    }

    @Test
    public void url() {
        Pattern p = Pattern.compile("%s".formatted(URL_PATTERN));
        Assertions.assertTrue(p.matcher("[Google](http://www.google.com)").matches());
        Assertions.assertTrue(p.matcher("![Google](http://www.google.com/img)").matches());
    }
}
