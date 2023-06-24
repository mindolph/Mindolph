package com.mindolph.core.search;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 */
class TextNavigatorTest {

    private final String text1 = "0123456789\nabcdefghij\n0123456789";
    private final String text2 = "#####";
    private final String text3 = """
            0123456789
            0123456789
            0123456789
            """;
    private final String text4 = """
            0123456789  0123456789  0123456789
            0123456789  0123456789  0123456789
            """;// with 2 blank chars
    @Test
    void locateNext() {
        TextNavigator textNavigator = new TextNavigator();
        textNavigator.setText(text1, true);

        TextLocation loc89 = textNavigator.locateNext("89", false);
        TextLocation locfgh = textNavigator.locateNext("fgh", false);
        TextLocation locij = textNavigator.locateNext("ij", false);
        Assertions.assertEquals("0 8 0 9", loc89.toString());
        Assertions.assertEquals("1 5 1 7", locfgh.toString());
        Assertions.assertEquals("1 8 1 9", locij.toString());
        // fail
        TextLocation locNone = textNavigator.locateNext("ij", false);
        Assertions.assertNull(locNone);


        // with line break
        textNavigator.moveCursor(0);
        TextLocation locBreak = textNavigator.locateNext("\n", false);
        Assertions.assertEquals("0 10 0 10", locBreak.toString());
        TextLocation locWithBreak = textNavigator.locateNext("j\n0", false);
        Assertions.assertEquals("1 9 2 0", locWithBreak.toString());
    }

    @Test
    void locateNextOverlapping() {
        TextNavigator textNavigator = new TextNavigator();
        textNavigator.setText(text2, true);
        TextLocation loc0 = textNavigator.locateNext("##", false);
        TextLocation loc1 = textNavigator.locateNext("##", false);
        TextLocation loc2 = textNavigator.locateNext("##", false);
        Assertions.assertEquals("0 0 0 1", loc0.toString());
        Assertions.assertEquals("0 2 0 3", loc1.toString());
        Assertions.assertNull(loc2);
    }

    @Test
    void locatePrev() {
        TextNavigator textNavigator = new TextNavigator();
        textNavigator.setText(text1, true);

        TextLocation loc0 = textNavigator.locatePrev("ij", false);
        TextLocation loc1 = textNavigator.locatePrev("fgh", false);
        TextLocation loc2 = textNavigator.locatePrev("89", false);
        Assertions.assertEquals("1 8 1 9", loc0.toString());
        Assertions.assertEquals("1 5 1 7", loc1.toString());
        Assertions.assertEquals("0 8 0 9", loc2.toString());
    }

    @Test
    void locatePrevOverlapping() {
        TextNavigator textNavigator = new TextNavigator();
        textNavigator.setText(text2, true);
        TextLocation loc0 = textNavigator.locatePrev("##", false);
        TextLocation loc1 = textNavigator.locatePrev("##", false);
        TextLocation loc2 = textNavigator.locatePrev("##", false);
        Assertions.assertEquals("0 3 0 4", loc0.toString());
        Assertions.assertEquals("0 1 0 2", loc1.toString());
        Assertions.assertNull(loc2);
    }

    @Test
    void locateNextPrev() {
        TextNavigator textNavigator = new TextNavigator();
        textNavigator.setText(text1, true);

        TextLocation loc89 = textNavigator.locateNext("89", false);
        TextLocation locfgh = textNavigator.locateNext("fgh", false);
        TextLocation loc89Back = textNavigator.locatePrev("89", false);
        TextLocation loc89Back2 = textNavigator.locatePrev("01", false);
        Assertions.assertEquals("0 8 0 9", loc89.toString());
        Assertions.assertEquals("1 5 1 7", locfgh.toString());
        Assertions.assertEquals("0 8 0 9", loc89Back.toString());
        Assertions.assertEquals("0 0 0 1", loc89Back2.toString());
    }

    @Test
    void changeText() {
        TextNavigator textNavigator = new TextNavigator();
        textNavigator.setText(text1, true);

        TextLocation loc89 = textNavigator.locateNext("89", false);
        Assertions.assertEquals("0 8 0 9", loc89.toString());

        textNavigator.setText(text2, true);
        TextLocation loc = textNavigator.locateNext("##", false);
        Assertions.assertEquals("0 0 0 1", loc.toString());
    }


    @Test
    public void convert() {
        TextNavigator navigator = new TextNavigator();
        navigator.setText(text3, false);
        Assertions.assertEquals(new TextLocation(0, 5, 0, 8), navigator.convert(5, 8));
        Assertions.assertEquals(new TextLocation(1, 5, 1, 8), navigator.convert(16, 19));
        Assertions.assertEquals(new TextLocation(2, 5, 2, 8), navigator.convert(27, 30));
    }

    @Test
    public void convertWithBlank() {
        TextNavigator navigator = new TextNavigator();
        navigator.setText(text4, false);
        Assertions.assertEquals(new TextLocation(0, 5, 0, 8), navigator.convert(5, 8));
        Assertions.assertEquals(new TextLocation(0, 17, 0, 20), navigator.convert(17, 20));
        Assertions.assertEquals(new TextLocation(0, 29, 0, 32), navigator.convert(29, 32));
        Assertions.assertEquals(new TextLocation(1, 5, 1, 8), navigator.convert(40, 43));
        Assertions.assertEquals(new TextLocation(1, 17, 1, 20), navigator.convert(52, 55));
        Assertions.assertEquals(new TextLocation(1, 29, 1, 32), navigator.convert(64, 67));
    }
}