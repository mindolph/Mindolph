package com.mindolph.mindmap.util;

import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.model.BaseElement;
import com.mindolph.mindmap.model.ElementRoot;
import com.mindolph.mindmap.model.TopicNode;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import static com.mindolph.mindmap.constant.MindMapConstants.*;

/**
 * @author mindolph.com@gmail.com
 */
public class ElementUtils {

    /**
     * find the nearest element from given point under root element.
     *
     * @param rootElement
     * @param elementToIgnore
     * @param point
     * @return
     */
    public static BaseElement findNearestOpenedTopicToPoint(BaseElement rootElement, BaseElement elementToIgnore, Point2D point) {
        return findNearestTopic(rootElement, elementToIgnore, Double.MAX_VALUE, point);
    }

    private static BaseElement findNearestTopic(BaseElement rootElement, BaseElement elementToIgnore, double maxDistance, Point2D point) {
        BaseElement result = null;
        if (elementToIgnore != rootElement) {
            double dist = calcAverageDistanceToPoint(rootElement, point);
            if (dist < maxDistance) {
                maxDistance = dist;
                result = rootElement;
            }
        }

        if (!rootElement.isCollapsed()) {
            for (TopicNode t : rootElement.getModel().getChildren()) {
                BaseElement element = t.getPayload() == null ? null : (BaseElement) t.getPayload();
                if (element != null) {
                    BaseElement nearestChild = findNearestTopic(element, elementToIgnore, maxDistance, point);
                    if (nearestChild != null) {
                        maxDistance = calcAverageDistanceToPoint(nearestChild, point);
                        result = nearestChild;
                    }
                }
            }
        }
        return result;
    }

    public static double calcAverageDistanceToPoint(BaseElement element, Point2D point) {
        Rectangle2D bounds = element.getBounds();
        double d1 = point.distance(bounds.getMinX(), bounds.getMinY());
        double d2 = point.distance(bounds.getMaxX(), bounds.getMinY());
        double d3 = point.distance(bounds.getMinX(), bounds.getMaxY());
        double d4 = point.distance(bounds.getMaxX(), bounds.getMaxY());
        return (d1 + d2 + d3 + d4) / (bounds.contains(point) ? 8.0d : 4.0d);
    }

    public static int calcDropPosition(BaseElement destination, Point2D dropPoint) {
        int result;
        if (destination.getClass() == ElementRoot.class) {
            result = dropPoint.getX() < RectangleUtils.centerX(destination.getBounds()) ? DRAG_POSITION_LEFT : DRAG_POSITION_RIGHT;
        }
        else {
            boolean isLeft = destination.isLeftDirection();
            Rectangle2D bounds = destination.getBounds();

            double edgeOffset = bounds.getWidth() * 0.25d; // to distinguish left/right and top/bottom
            if (dropPoint.getX() >= (bounds.getMinX() + edgeOffset) && dropPoint.getX() <= (bounds.getMaxX() - edgeOffset)) {
                result = dropPoint.getY() < RectangleUtils.centerY(bounds) ? DRAG_POSITION_TOP : DRAG_POSITION_BOTTOM;
            }
            else if (isLeft) {
                result = dropPoint.getX() < RectangleUtils.centerX(bounds) ? DRAG_POSITION_LEFT : DRAG_POSITION_UNKNOWN;
            }
            else {
                result = dropPoint.getX() > RectangleUtils.centerX(bounds) ? DRAG_POSITION_RIGHT : DRAG_POSITION_UNKNOWN;
            }
        }
        return result;
    }
}
