package com.mindolph.core.search;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 * @since 1.3.4
 */
public class TextNavigator2Test {

    private final String text1 = """
            0123456789
            0123456789
            0123456789
            """;
    private final String text2 = """
            0123456789  0123456789  0123456789
            0123456789  0123456789  0123456789
            """;// with 2 blank chars

    @Test
    public void convert() {
        TextNavigator2 navigator = new TextNavigator2();
        navigator.setText(text1, false);
        Assertions.assertEquals(new TextLocation(0, 5, 0, 8), navigator.convert(5, 8));
        Assertions.assertEquals(new TextLocation(1, 5, 1, 8), navigator.convert(16, 19));
        Assertions.assertEquals(new TextLocation(2, 5, 2, 8), navigator.convert(27, 30));
    }

    @Test
    public void convertWithBlank() {
        TextNavigator2 navigator = new TextNavigator2();
        navigator.setText(text2, false);
        Assertions.assertEquals(new TextLocation(0, 5, 0, 8), navigator.convert(5, 8));
        Assertions.assertEquals(new TextLocation(0, 17, 0, 20), navigator.convert(17, 20));
        Assertions.assertEquals(new TextLocation(0, 29, 0, 32), navigator.convert(29, 32));
        Assertions.assertEquals(new TextLocation(1, 5, 1, 8), navigator.convert(40, 43));
        Assertions.assertEquals(new TextLocation(1, 17, 1, 20), navigator.convert(52, 55));
        Assertions.assertEquals(new TextLocation(1, 29, 1, 32), navigator.convert(64, 67));
    }
}
