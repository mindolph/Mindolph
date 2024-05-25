package com.mindolph.base.util;

import com.mindolph.mfx.util.PointUtils;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @since 1.7.6
 */
public class LayoutUtilsTest {

    static Bounds baseParentBounds;
    static Bounds baseHoverBounds;

    @BeforeAll
    public static void setup() {
        baseParentBounds = new BoundingBox(0, 0, 1000, 1000);
        baseHoverBounds =  new BoundingBox(0, 0, 1000, 1000);
    }

    @Test
    public void bestLocation() {

        Dimension2D dim = new Dimension2D(100, 100);

        Bounds hb = new BoundingBox(0, 0, 100, 100);
        Point2D point2D = LayoutUtils.bestLocation(baseParentBounds, hb, dim, null);
        System.out.println(PointUtils.pointInStr(point2D));

        hb = new BoundingBox(950, 950, 100, 100);
        point2D = LayoutUtils.bestLocation(baseParentBounds, hb, dim, null);
        System.out.println(PointUtils.pointInStr(point2D));


    }
}
