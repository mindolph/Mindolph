package com.mindolph.csv;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
public class CsvNavigatorTest {

    private final List<String> CELLS = Arrays.asList("A0", "A1", "A2", "A3", "A4", "B0", "B1", "B2", "B3", "B4");

    @Test
    public void locateNext() {
        CsvNavigator navigator = new CsvNavigator(CELLS, 5);// init position
        Assertions.assertEquals(new CellPos(0, 0), navigator.locateNext("A", false));
        navigator.moveCursorNext();
        Assertions.assertEquals(new CellPos(0, 1), navigator.locateNext("A", false));
        navigator.moveCursorNext();
        Assertions.assertEquals(new CellPos(1, 0), navigator.locateNext("B", false));
        navigator.moveCursor(1,2);
        Assertions.assertEquals(new CellPos(1, 2), navigator.locateNext("B", false));
        navigator.moveCursorNext();
        Assertions.assertNull(navigator.locateNext("a", false));
    }

    @Test
    public void locatePrev() {
        CsvNavigator navigator = new CsvNavigator(CELLS, 5);
        navigator.moveCursor(9);
        Assertions.assertEquals(new CellPos(1, 4), navigator.locatePrev("B", false));
        navigator.moveCursorPrev();
        Assertions.assertEquals(new CellPos(1, 3), navigator.locatePrev("B", false));
        navigator.moveCursor(0, 4);
        Assertions.assertEquals(new CellPos(0, 4), navigator.locatePrev("A", false));
        navigator.moveCursorPrev();
        Assertions.assertNull(navigator.locatePrev("B", false));
        navigator.moveCursor(0, 0);
        Assertions.assertEquals(new CellPos(0, 0), navigator.locatePrev("A", false));
        navigator.moveCursorPrev();
        Assertions.assertEquals(new CellPos(0, 4), navigator.locatePrev("A", false));
    }
}
