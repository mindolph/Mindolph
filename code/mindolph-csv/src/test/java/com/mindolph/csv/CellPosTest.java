package com.mindolph.csv;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 */
public class CellPosTest {
    static final CellPos l0c0 = CellPos.zero();
    static final CellPos l1c0 = new CellPos(1, 0);
    static final int COLUMNS = 10;

    @Test
    public void getIndexOfAll() {
        Assertions.assertEquals(0, CellPos.getIndexOfAll(l0c0, COLUMNS));
        Assertions.assertEquals(10, CellPos.getIndexOfAll(l1c0, COLUMNS));
    }

    @Test
    public void fromIndexOfAll() {
        Assertions.assertEquals(l0c0, CellPos.fromIndexOfAll(0, COLUMNS));
        Assertions.assertEquals(l1c0, CellPos.fromIndexOfAll(10, COLUMNS));
        System.out.println(CellPos.fromIndexOfAll(8, 5));
        Assertions.assertEquals(new CellPos(1, 3), CellPos.fromIndexOfAll(8, 5));
    }
}
