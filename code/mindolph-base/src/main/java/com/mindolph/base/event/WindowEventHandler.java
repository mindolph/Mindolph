package com.mindolph.base.event;


import javafx.geometry.Rectangle2D;

/**
 * @author mindolph.com@gmail.com
 */
@FunctionalInterface
public interface WindowEventHandler {

    void onWindowResized(Rectangle2D rectangle);
}
