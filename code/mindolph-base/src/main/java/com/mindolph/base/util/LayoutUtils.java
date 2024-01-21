package com.mindolph.base.util;

import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;

/**
 * @author mindolph
 * @since 1.6.4
 */
public class LayoutUtils {

    /**
     * Limit one component in the area of another component.
     * If the component is out of the area, it will be moved to the best location.
     *
     * @param parentBounds bounds of target component.
     * @param hoverBounds  bounds of hover component which will be limited to the area of target component.
     * @param targetDim dimension of target component or region.
     * @param extraPadding extra padding to the border of target bounds, optional.
     * @return
     */
    public static Point2D bestLocation(Bounds parentBounds, Bounds hoverBounds,
                                       Dimension2D targetDim, Dimension2D extraPadding) {
        double padWidth = extraPadding == null ? 1f : extraPadding.getWidth(); // use 1 to avoid calculation bias
        double padHeight = extraPadding == null ? 1f : extraPadding.getHeight(); // use 1 to avoid calculation bias
        double offsetX = (parentBounds.getMaxX() - padWidth) - hoverBounds.getMaxX();
        double offsetY = (parentBounds.getMaxY() - padHeight) - hoverBounds.getMaxY();
        // different strategy from x and y
        double newX = offsetX > 0 ? hoverBounds.getMinX() : hoverBounds.getMinX() + offsetX;
        double newY = offsetY > 0 ? hoverBounds.getMinY() : hoverBounds.getMinY() - hoverBounds.getHeight() - targetDim.getHeight() - padHeight;
        newX = Math.max(0, newX);
        newY = Math.max(0, newY); // adjust again if it's head still out of target bounds.
        return new Point2D(newX, newY);
    }
}
