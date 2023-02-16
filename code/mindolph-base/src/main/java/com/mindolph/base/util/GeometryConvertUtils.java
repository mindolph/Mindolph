package com.mindolph.base.util;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.Rectangle;

/**
 * @author mindolph.com@gmail.com
 */
public class GeometryConvertUtils {

    public static Rectangle rectangle2D2Rectangle(Rectangle2D rectangle2D) {
        return new Rectangle(rectangle2D.getMinX(), rectangle2D.getMinY(),rectangle2D.getWidth(), rectangle2D.getHeight());
    }

    public static Bounds rectangle2Bounds(Rectangle2D rect) {
        if (rect == null) {
            return null;
        }
        return new BoundingBox(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    public static Bounds dimension2DToBounds(Dimension2D dim) {
        return new BoundingBox(0, 0, dim.getWidth(), dim.getHeight());
    }

    public static Rectangle2D boundsToRectangle2D(Bounds bounds) {
        return new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(),  bounds.getHeight());
    }
}
