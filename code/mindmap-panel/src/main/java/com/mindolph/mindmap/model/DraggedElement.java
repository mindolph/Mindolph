package com.mindolph.mindmap.model;

import javafx.geometry.Point2D;

/**
 * The DraggedElement will be created once, and re-paint multiple times during dragging.
 *
 * @author mindolph.com@gmail.com
 */
public class DraggedElement {

    private final BaseElement element;
    private final Point2D mousePointerOffset;
    private Point2D currentPosition;
    private final Modifier modifier;

    public DraggedElement(BaseElement element, Point2D mousePointerOffset, Modifier modifier) {
        this.element = element;
        this.mousePointerOffset = mousePointerOffset;
        this.currentPosition = new Point2D(0, 0);
        this.modifier = modifier;
    }

    public Modifier getModifier() {
        return this.modifier;
    }

    public boolean isPositionInside() {
        return this.element.getBounds().contains(this.currentPosition);
    }

    public BaseElement getElement() {
        return this.element;
    }

    public void updatePosition(Point2D point) {
        this.currentPosition = new Point2D(point.getX(), point.getY());
    }

    public Point2D getPosition() {
        return this.currentPosition;
    }

    public Point2D getMousePointerOffset() {
        return this.mousePointerOffset;
    }

    public double getDrawPositionX() {
        return this.currentPosition.getX() - this.mousePointerOffset.getX();
    }

    public double getDrawPositionY() {
        return this.currentPosition.getY() - this.mousePointerOffset.getY();
    }

    public void draw() {
        double x = getDrawPositionX();
        double y = getDrawPositionY();
        element.doPaint(x, y, 0.55f);
    }

    public enum Modifier {
        NONE,
        MAKE_JUMP
    }
}
