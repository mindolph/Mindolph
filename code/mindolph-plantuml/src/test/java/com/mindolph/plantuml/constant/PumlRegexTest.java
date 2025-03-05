package com.mindolph.plantuml.constant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mindolph.plantuml.constant.PlantUmlConstants.*;

/**
 * @author mindolph.com@gmail.com
 */
public class PumlRegexTest {

    @Test
    public void arrow() {
        Pattern p = Pattern.compile(ARROW1);
        Assertions.assertTrue(p.matcher("<").matches());
        Assertions.assertTrue(p.matcher(">").matches());
        Assertions.assertTrue(p.matcher("*").matches());
        Assertions.assertTrue(p.matcher("o").matches());
        Assertions.assertTrue(p.matcher("#").matches());
        Assertions.assertTrue(p.matcher("x").matches());
        Assertions.assertTrue(p.matcher("{").matches());
        Assertions.assertTrue(p.matcher("}").matches());
        Assertions.assertTrue(p.matcher("+").matches());
        Assertions.assertTrue(p.matcher("^").matches());

        p = Pattern.compile(ARROW2);
        //left
        Assertions.assertTrue(p.matcher("<|").matches());
        Assertions.assertTrue(p.matcher("}|").matches()); // entity
        Assertions.assertTrue(p.matcher("}o").matches()); // entity
        Assertions.assertTrue(p.matcher("||").matches()); // entity
        Assertions.assertTrue(p.matcher("|o").matches()); // entity
        Assertions.assertTrue(p.matcher("|>").matches());
        Assertions.assertTrue(p.matcher("|{").matches());// entity
        Assertions.assertTrue(p.matcher("o{").matches());// entity
        Assertions.assertTrue(p.matcher("||").matches());// entity
        Assertions.assertTrue(p.matcher("|>").matches());// entity
    }

    @Test
    public void connect() {
        System.out.println(BAR);
        Pattern p = Pattern.compile(BAR);
        Assertions.assertTrue(p.matcher("-").matches());
        Assertions.assertTrue(p.matcher(".").matches());
        Assertions.assertTrue(p.matcher("--").matches());
        Assertions.assertTrue(p.matcher("..").matches());
        System.out.println(CONNECTOR);
        p = Pattern.compile(CONNECTOR);
        Assertions.assertTrue(p.matcher("<-").matches());
        Assertions.assertTrue(p.matcher("<--").matches());
        Assertions.assertTrue(p.matcher("->").matches());
        Assertions.assertTrue(p.matcher("-->").matches());

        Assertions.assertTrue(p.matcher("--|>").matches());
        Assertions.assertTrue(p.matcher("-|>").matches());
        Assertions.assertTrue(p.matcher("..|>").matches());
        Assertions.assertTrue(p.matcher(".|>").matches());
        Assertions.assertTrue(p.matcher("<|--").matches());
        Assertions.assertTrue(p.matcher("<|-").matches());
        Assertions.assertTrue(p.matcher("<|..").matches());
        Assertions.assertTrue(p.matcher("<|.").matches());
    }

    @Test
    public void quote() {
        System.out.println(QUOTE_BLOCK);
        Pattern p = Pattern.compile(QUOTE_BLOCK);
        Assertions.assertTrue(p.matcher("\"\"").matches());
        Assertions.assertTrue(p.matcher("\"hello\"").matches());
        Assertions.assertTrue(p.matcher("\" hello world!\"").matches());
        Assertions.assertTrue(p.matcher("[ hello world!]").matches());

        Assertions.assertFalse(p.matcher("\" hello world!").matches());
        Assertions.assertFalse(p.matcher("\" hello world!\" everybody \"").matches());
        Assertions.assertFalse(p.matcher("[ hello world!").matches());
    }

    @Test
    public void activity() {
        System.out.println(ACTIVITY);
        Pattern p = Pattern.compile(ACTIVITY);
        Assertions.assertTrue(p.matcher(":;").matches());
        Assertions.assertTrue(p.matcher(":hello;").matches());
        Assertions.assertFalse(p.matcher(":hello; world;").matches());
    }

    @Test
    public void outline() {
        String OUTLINE = "(@|(' ))(startsalt|startgantt|startlatex|startmath|startdot|startuml|startmindmap|startwbs|startyaml|startjson|startregex|startebnf|[\\*]+.+[\\*]+?)";
        Pattern p = Pattern.compile(OUTLINE);
        Matcher matcher1 = p.matcher("@startuml");
        Assertions.assertTrue(matcher1.find());
        System.out.println(matcher1.group(3));
        Assertions.assertEquals("startuml", matcher1.group(3));

        Matcher matcher2 = p.matcher("' * level 1 *");
        Assertions.assertTrue(matcher2.find());
        System.out.println(matcher2.group(3));
        Assertions.assertEquals("* level 1 *", matcher2.group(3));


        Matcher matcher3 = p.matcher("' ** level 2 **");
        Assertions.assertTrue(matcher3.find());
        System.out.println(matcher3.group(3));
        Assertions.assertEquals("** level 2 **", matcher3.group(3));

        Matcher matcher = p.matcher("@startuml\n\n\n' **  title  ** \n");
        Assertions.assertTrue(matcher.find());
        System.out.println(matcher.group(3));
        Assertions.assertEquals("startuml", matcher.group(3));
        Assertions.assertTrue(matcher.find());
        System.out.println(matcher.group(3));
        Assertions.assertEquals("**  title  **", matcher.group(3));
    }
}
