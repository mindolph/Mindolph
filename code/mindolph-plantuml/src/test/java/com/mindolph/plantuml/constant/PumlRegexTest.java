package com.mindolph.plantuml.constant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Pattern p = Pattern.compile(QUOTE_BLOCK);
        Assertions.assertTrue(p.matcher("\"\"").matches());
        Assertions.assertTrue(p.matcher("\"hello\"").matches());
        Assertions.assertTrue(p.matcher("\" hello world!\"").matches());
        Assertions.assertTrue(p.matcher("[ hello world!]").matches());

        Assertions.assertFalse(p.matcher("\" hello world!").matches());
        Assertions.assertFalse(p.matcher("[ hello world!").matches());
    }
}
