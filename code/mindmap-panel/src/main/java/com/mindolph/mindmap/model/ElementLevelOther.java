package com.mindolph.mindmap.model;


import com.mindolph.base.graphic.Graphics;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import com.mindolph.mindmap.theme.BorderType;
import javafx.scene.paint.Color;

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

    @Override
    public BorderType getBorderType() {
        return theme.getBorderType();
    }
}
