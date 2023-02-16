package com.mindolph.core.search;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 * @deprecated
 */
class TextLocatorTest {

    final String simpleText = "01234567890123456789";

    final String multilineText = """
            Mindolph is a open source knowledge base application.
            It is powerful and easy to use, you can use it freely.
            When everybody is somebody, then nobody is anybody.
                        
            """;

    @Test
    void stringIndexOf() {
        int idx = "abc".indexOf("ab", 0);
        Assertions.assertEquals(0, idx);

        idx = "a a a".lastIndexOf("a", 4);
        Assertions.assertEquals(4, idx);
        idx = "a a a".lastIndexOf("a", 2);
        Assertions.assertEquals(2, idx);
        idx = "a a a".lastIndexOf("a", 1);
        Assertions.assertEquals(0, idx);

        idx = "When everybody is somebody, then nobody is anybody.".lastIndexOf("body", 45);
        Assertions.assertEquals(35, idx);
    }

    @Test
    void locateNext() {
        TextLocator textLocator = new TextLocator();
        textLocator.setText(multilineText, false);
        TextLocation one = textLocator.locateNext("is", false, 0, 0);
        TextLocation two = textLocator.locateNext("is", false, 0, 0);
        Assertions.assertEquals("0 9 0 11", one.toString());
        Assertions.assertEquals("1 3 1 5", two.toString());
        System.out.println(one);
        System.out.println(two);
        TextLocation three = textLocator.locateNext("Mindolph", false, 0, 0);
        Assertions.assertEquals("0 0 0 8", three.toString());
        System.out.println(three);
    }

    @Test
    void locateNextContinuously() {
        TextLocator textLocator = new TextLocator();
        textLocator.setText(multilineText, false);
        TextLocation one = textLocator.locateNext("is", false, 0, 0);
        TextLocation two = textLocator.locateNext("is", false, one.getEndRow(), one.getEndCol());
        Assertions.assertEquals("0 9 0 11", one.toString());
        Assertions.assertEquals("1 3 1 5", two.toString());
    }

    @Test
    void locatePrev() {
        TextLocator textLocator = new TextLocator();
        textLocator.setText(simpleText, false);
        TextLocation tloc0 = textLocator.locatePrev("89", false, -1, -1);
        Assertions.assertEquals("0 18 0 20", tloc0.toString());

        textLocator.setText(multilineText, true);
        System.out.println(multilineText);
//        textLocator.locateNext("anybody", false, 0, 0);
        System.out.println("locate last one");
        TextLocation zero = textLocator.locatePrev("body", false, -1, -1);
        System.out.println("locate last - 1");
        TextLocation one = textLocator.locatePrev("body", false, -1, -1);
        System.out.println("locate last - 2");
        TextLocation two = textLocator.locatePrev("body", false, -1, -1);
        System.out.println("locate last - 3");
        TextLocation three = textLocator.locatePrev("body", false, -1, -1);
        System.out.println(zero);
        System.out.println(one);
        System.out.println(two);
        System.out.println(three);
        Assertions.assertEquals("2 46 2 50", zero.toString());
        Assertions.assertEquals("2 35 2 39", one.toString());
        Assertions.assertEquals("2 22 2 26", two.toString());
        Assertions.assertEquals("2 10 2 14", three.toString());

        TextLocation five = textLocator.locatePrev("Mindolph", false, -1, -1);
        System.out.println(five);
        Assertions.assertEquals("0 0 0 8", five.toString());
    }

    @Test
    public void locateNextOverlapping() {
        TextLocator textLocator = new TextLocator();
        textLocator.setText("###__", false);
        TextLocation fist = textLocator.locateNext("##", true, 0, 0);
        TextLocation second = textLocator.locateNext("##", true, 0, 0);
        Assertions.assertEquals("0 0 0 2", fist.toString());
        Assertions.assertNull(second);
//        Assertions.assertEquals("", first.toString());
    }

    @Test
    public void locatePrevOverlapping() {
        TextLocator textLocator = new TextLocator();
        textLocator.setText("#####", false);
        TextLocation tloc0 = textLocator.locatePrev("##", true, -1, -1);
        Assertions.assertEquals("0 3 0 5", tloc0.toString());
        textLocator.moveStartCol(-1);
        TextLocation tloc1 = textLocator.locatePrev("##", true, -1, -1);
        Assertions.assertEquals("0 1 0 3", tloc1.toString());
        textLocator.moveStartCol(-1);
        TextLocation tloc2 = textLocator.locatePrev("##", true, -1, -1);
        Assertions.assertNull(tloc2);
//        Assertions.assertEquals("", first.toString());

        // multiline
        textLocator.setText("####\n###", true);
        TextLocation tloc00 = textLocator.locatePrev("##", true, -1, -1);
        Assertions.assertEquals("1 3 1 5", tloc00.toString());

    }
}