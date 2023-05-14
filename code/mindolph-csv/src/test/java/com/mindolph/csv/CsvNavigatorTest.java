package com.mindolph.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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
        navigator.moveCursor(1, 2);
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

    @Test
    public void testSpecific() throws IOException {
        String csv = """
                a1, a2,a3, a4, a5,
                b1, b2,b3, b4, b5,
                ,,,,aaa,
                """;
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();
        StringReader stringReader = new StringReader(csv);
        CSVParser parsed = csvFormat.parse(stringReader);
        List<String> cells = parsed.stream().flatMap((Function<CSVRecord, Stream<String>>) CSVRecord::stream).toList();
        System.out.println(StringUtils.join(cells, ","));
        CsvNavigator navigator = new CsvNavigator(cells, 6);
        navigator.locateNext("a", false);
        navigator.moveCursorNext();
        navigator.locateNext("a", false);
        navigator.moveCursorNext();
        navigator.locateNext("a", false);
        navigator.moveCursorNext();
        navigator.locateNext("a", false);
        navigator.moveCursorNext();
        navigator.locateNext("a", false);
        navigator.moveCursorNext();
        navigator.locateNext("a", false);
        Assertions.assertEquals(new CellPos(2, 4), navigator.locateNext("a", false));
    }
}
