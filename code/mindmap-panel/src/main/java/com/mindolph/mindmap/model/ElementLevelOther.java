package com.mindolph.mindmap.model;


import com.mindolph.base.constant.StrokeType;
import com.mindolph.base.graphic.Graphics;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

public class ElementLevelOther extends ElementLevelFirst {

    public ElementLevelOther(TopicNode model, Graphics g, MindMapConfig cfg, MindMapContext context) {
        super(model, g, cfg, context);
    }

    protected ElementLevelOther(ElementLevelOther element) {
        super(element);
    }

    @Override
    public BaseElement makeCopy() {
        return new ElementLevelOther(this);
    }

    @Override
    public BaseElement makeCopyWith(Graphics g) {
        ElementLevelOther e = new ElementLevelOther(this);
        e.updateGraphics(g);
        return e;
    }

    @Override
    public void drawComponent(boolean drawCollapsator) {
        g.setStroke(mindMapContext.safeScale(theme.getElementBorderWidth(), 0.1f), StrokeType.SOLID);

        if (theme.isDropShadow()) {
            float offset = mindMapContext.safeScale(theme.getShadowOffset(), 0.0f);
            g.draw(makeShape(offset, offset), null, theme.getShadowColor());
        }

        Shape shape = makeShape(0f, 0f);
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
    public void doPaintConnectors(boolean isLeftDirection) {
        Rectangle2D source = new Rectangle2D(this.bounds.getMinX() + this.collapsatorZone.getMinX(),
                this.bounds.getMinY() + this.collapsatorZone.getMinY(),
                this.collapsatorZone.getWidth(), this.collapsatorZone.getHeight());
        for (TopicNode t : this.model.getChildren()) {
            this.drawConnector(source, ((BaseElement) t.getPayload()).getBounds(), isLeftDirection);
        }
    }

    @Override
    public boolean isLeftDirection() {
        TopicNode topic = this.model.getParent();

        boolean result = false;

        while (topic != null) {
            BaseElement w = (BaseElement) topic.getPayload();
            if (w.getClass() == ElementLevelFirst.class) {
                result = w.isLeftDirection();
                break;
            }
            else {
                topic = topic.getParent();
            }
        }

        return result;
    }

    @Override
    public void setLeftDirection(boolean leftSide) {
    }

    @Override
    public Color getBackgroundColor() {
        return this.fillColor == null ? theme.getOtherLevelBackgroundColor() : this.fillColor;
    }

    @Override
    public Color getTextColor() {
        return this.textColor == null ? theme.getOtherLevelTextColor() : this.textColor;
    }

}
