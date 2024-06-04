package com.mindolph.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TemplateUtilsTest {

    @Test
    public void format() {
        String formatted = TemplateUtils.format("hello ${name}, is ${name} your name?", "name", "world");
        System.out.println(formatted);
        Assertions.assertEquals("hello world, is world your name?", formatted);
    }

    @Test
    public void formatMulti() {
        String formatted = TemplateUtils.format("hello ${name}, is ${name} your name?, what's your ${attr}?",
                new String[]{"name", "attr"},
                new String[]{"world", "age"});
        System.out.println(formatted);
        Assertions.assertEquals("hello world, is world your name?, what's your age?", formatted);
    }
}