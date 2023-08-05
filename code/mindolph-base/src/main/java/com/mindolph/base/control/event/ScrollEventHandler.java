package com.mindolph.base.control.event;


import javafx.geometry.Point2D;

/**
 * Event handler for containers.
 *
 * @author mindolph.com@gmail.com
 * @see com.mindolph.base.control.BaseScalableView
 */
public interface ScrollEventHandler {

    /**
     * Invoked when the inner control needs the container to scroll.
     *
     * @param scrollPos The position that the control want scroll to.
     * @param animate Whether scroll with animation.
     */
    void onScroll(Point2D scrollPos, boolean animate);
}
