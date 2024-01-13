package com.mindolph.base.control;

import javafx.scene.layout.Pane;

/**
 * Any control that has other control anchored on it should implement this interface.
 *
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public interface Anchorable {

    /**
     * Set the parent pane that this control is anchored on.
     * The bounds of parent pane must be the same with the bounds of this control.
     * Otherwise, the position calculation will be wrong.
     *
     * @param pane
     */
    void setParentPane(Pane pane);

    Pane getParentPane();
}
