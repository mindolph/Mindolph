package com.mindolph.mindmap.utils;

import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.util.DiagramUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mindolph.com@gmail.com
 */
public class DiagramUtilsTest {

    @Test
    public void testFindRectEdgeIntersection() {
        final Rectangle2D rect = new Rectangle2D(50, 50, 100, 50);

        // doesn't work because the accuracy TODO
        assertEquals(new Point2D(100d, 50d), DiagramUtils.findRectEdgeIntersection(rect, 100d, 25d));
        assertEquals(new Point2D(100d, 100d), DiagramUtils.findRectEdgeIntersection(rect, 100d, 125d));
        assertEquals(new Point2D(50d, 75d), DiagramUtils.findRectEdgeIntersection(rect, 10d, 75d));
        assertEquals(new Point2D(150d, 75d), DiagramUtils.findRectEdgeIntersection(rect, 200d, 75d));

        final Rectangle2D rect2 = new Rectangle2D(550, 650, 100, 50);
        assertEquals(550d, DiagramUtils.findRectEdgeIntersection(rect2, 10d, 640d).getX(), 0.0d);
        assertTrue(RectangleUtils.centerY(rect2) > DiagramUtils.findRectEdgeIntersection(rect2, 10d, 640d).getY());
        assertTrue(RectangleUtils.centerY(rect2) < DiagramUtils.findRectEdgeIntersection(rect2, 10d, 710d).getY());

        assertEquals(650d, DiagramUtils.findRectEdgeIntersection(rect2, 1500d, 640d).getX(), 0.0d);
        assertTrue(RectangleUtils.centerY(rect2) > DiagramUtils.findRectEdgeIntersection(rect2, 1500d, 640d).getY());
        assertTrue(RectangleUtils.centerY(rect2) < DiagramUtils.findRectEdgeIntersection(rect2, 1500d, 710d).getY());

        assertEquals(650d, DiagramUtils.findRectEdgeIntersection(rect2, 590d, 10d).getY(), 0.0d);
        assertEquals(700d, DiagramUtils.findRectEdgeIntersection(rect2, 590d, 10000d).getY(), 0.0d);
        assertTrue(RectangleUtils.centerX(rect2) > DiagramUtils.findRectEdgeIntersection(rect2, 520d, 10d).getX());
        assertTrue(RectangleUtils.centerX(rect2) < DiagramUtils.findRectEdgeIntersection(rect2, 660d, 10d).getX());
    }

}
