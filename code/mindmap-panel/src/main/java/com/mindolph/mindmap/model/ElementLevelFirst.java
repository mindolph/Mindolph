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
        Rectangle rect = new Rectangle(x, y, this.bounds.getWidth(), this.bounds.getHeight());
        if (theme.getRoundRadius() > 0) {
            float round = mindMapContext.safeScale(theme.getRoundRadius(), 0f);
            rect.setArcWidth(round);
            rect.setArcHeight(round);
        }
        return rect;
    }

    @Override
    public void drawComponent(boolean drawCollapsator) {
        g.setStroke(mindMapContext.safeScale(theme.getElementBorderWidth(), 0.1f), StrokeType.SOLID);

        // Draw element area with shadow(if required)
        Shape shape = makeShape(0f, 0f);
        if (theme.isDropShadow()) {
            float offset = mindMapContext.safeScale(theme.getShadowOffset(), 0.0f);
            g.draw(makeShape(offset, offset), null, theme.getShadowColor());
        }
        g.draw(shape, this.getBorderColor(), this.getBackgroundColor());

        // Draw image
        if (this.visualAttributeImageBlock.mayHaveContent()) {
            this.visualAttributeImageBlock.paint();
        }

        // Draw text
        this.textBlock.paint(this.getTextColor());

        // Draw icon
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
        return this.fillColor == null ? theme.getFirstLevelBackgroundColor() : this.fillColor;
    }

    @Override
    public Color getTextColor() {
        return this.textColor == null ? theme.getFirstLevelTextColor() : this.textColor;
    }

    @Override
    public float getRoundRadius() {
        return config.getTheme().getRoundRadius();
    }
}
