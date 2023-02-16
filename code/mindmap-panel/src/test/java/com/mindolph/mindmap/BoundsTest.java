package com.mindolph.mindmap;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author mindolph.com@gmail.com
 */
public class BoundsTest {

    /**
     * TODO
     * Test contains() for {@code Bounds}
     * Note：min and max indicates the border of Bounds，width and height means the size of it.
     */
    @Test
    public void testContains() {
        Bounds big = new BoundingBox(0, 0, 1000, 800);
        Bounds small = new BoundingBox(100, 100, 200, 200);
        Assert.assertTrue(big.contains(small));

        big = new BoundingBox(0, 0, 300, 300);
        Assert.assertTrue(big.contains(small));

        big = new BoundingBox(-100, -100, 1000, 800);
        Assert.assertTrue(big.contains(small));

        big = new BoundingBox(-100, -100, 300, 300);
        Assert.assertFalse(big.contains(small));

        big = new BoundingBox(-100, -100, 400, 400);
        Assert.assertTrue(big.contains(small));

        big = new BoundingBox(-100, -100, 300, 300);
        small = new BoundingBox(-50, -50, 200, 200);
        Assert.assertTrue(big.contains(small));
    }
}
