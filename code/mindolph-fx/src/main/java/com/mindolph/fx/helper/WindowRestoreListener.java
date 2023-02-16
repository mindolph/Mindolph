package com.mindolph.fx.helper;


import javafx.geometry.Rectangle2D;

/**
 * @author mindolph.com@gmail.com
 */
@FunctionalInterface
public interface WindowRestoreListener {

    void onWindowRestore(Rectangle2D rectangle);
}
