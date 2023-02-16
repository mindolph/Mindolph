package com.mindolph.mindmap.model;

import com.mindolph.base.constant.StrokeType;
import com.mindolph.base.graphic.Graphics;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;


public class ElementLevelFirst extends BaseCollapsableElement {

    public ElementLevelFirst(TopicNode model, Graphics g, MindMapConfig cfg, MindMapContext context) {
        super(model, g, cfg, context);
    }

    protected ElementLevelFirst(ElementLevelFirst element) {
        super(element);
    }

    @Override
    public BaseElement makeCopy() {
        return new ElementLevelFirst(this);
    }

    @Override
    public BaseElement makeCopyWith(Graphics g) {
        ElementLevelFirst e = new ElementLevelFirst(this);
        e.updateGraphics(g);
        return e;
    }

    protected Shape makeShape(double x, double y) {
        return new Rectangle(x, y, this.bounds.getWidth(), this.bounds.getHeight());
    }

    @Override
    public void drawComponent(boolean drawCollapsator) {
        g.setStroke(mindMapContext.safeScale(cfg.getElementBorderWidth(), 0.1f), StrokeType.SOLID);

        Shape shape = makeShape(0f, 0f);

        if (cfg.isDropShadow()) {
            float offset = mindMapContext.safeScale(cfg.getShadowOffset(), 0.0f);
            g.draw(makeShape(offset, offset), null, cfg.getShadowColor());
        }

        g.draw(shape, getBorderColor(), getBackgroundColor());

        if (this.visualAttributeImageBlock.mayHaveContent()) {
            this.visualAttributeImageBlock.paint();
        }

        this.textBlock.paint(getTextColor());

        if (this.extrasIconBlock.hasContent()) {
            this.extrasIconBlock.paint();
        }

        if (drawCollapsator && this.hasChildren()) {
            drawCollapsator(this.isCollapsed());
        }
    }

    @Override
    public boolean isMoveable() {
        return true;
    }

    @Override
    public Color getBackgroundColor() {
        return this.fillColor == null ? cfg.getFirstLevelBackgroundColor() : this.fillColor;
    }

    @Override
    public Color getTextColor() {
        return this.textColor == null ? cfg.getFirstLevelTextColor() : this.textColor;
    }
}
