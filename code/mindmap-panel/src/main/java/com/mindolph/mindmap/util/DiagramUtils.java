package com.mindolph.mindmap.util;

import com.mindolph.base.util.GeometryConvertUtils;
import com.mindolph.mfx.util.RectangleUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.*;

/**
 *
 *
 * @author mindolph.com@gmail.com
 */
public class DiagramUtils {

    /**
     * Get the edge point from the start point to the center of rectangle.
     *
     * @param rect
     * @param startx x of point in the line outside the rectangle
     * @param starty y of point in the line outside the rectangle
     * @return
     */
    public static Point2D findRectEdgeIntersection(Rectangle2D rect, double startx, double starty) {
        Line line = new Line(startx, starty, RectangleUtils.centerX(rect), RectangleUtils.centerY(rect));
        return intersectLineAndShape(line, GeometryConvertUtils.rectangle2D2Rectangle(rect), startx, starty);
    }

    /**
     * get the edge point from intersection of line and shape
     *
     * @param line
     * @param shape
     * @param startx
     * @param starty
     * @return
     */
    private static Point2D intersectLineAndShape(Line line, Shape shape, double startx, double starty) {
        Shape intersect = Line.intersect(line, shape);
        if (intersect instanceof Path p) {
            if (p.getElements().isEmpty()) {
                return null;
            }
            Point2D farthest = new Point2D(line.getEndX(), line.getEndY());
            // find the nearest point in the interest line
            Point2D nearest = p.getElements().stream()
                    .map(pe -> {
                        if (pe instanceof MoveTo) {
                            return new Point2D(((MoveTo) pe).getX(), ((MoveTo) pe).getY());
                        }
                        else if (pe instanceof LineTo) {
                            return new Point2D(((LineTo) pe).getX(), ((LineTo) pe).getY());
                        }
                        else {
                            return farthest;
                        }
                    })
                    .reduce(farthest, (p1, p2) -> {
                        double d1 = p1.distance(startx, starty);
                        double d2 = p2.distance(startx, starty);
                        return d1 < d2 ? p1 : p2;
                    });
            return new Point2D(nearest.getX(), nearest.getY());
        }
        return null;
    }

}
