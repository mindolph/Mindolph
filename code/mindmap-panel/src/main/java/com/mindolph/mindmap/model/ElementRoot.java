package com.mindolph.mindmap.model;

import com.mindolph.base.constant.StrokeType;
import com.mindolph.base.graphic.Graphics;
import com.mindolph.mfx.util.DimensionUtils;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import static com.mindolph.mfx.util.RectangleUtils.centerX;
import static com.mindolph.mfx.util.RectangleUtils.centerY;

public final class ElementRoot extends BaseElement {

    private Dimension2D leftBlockSize = new Dimension2D(0, 0);
    private Dimension2D rightBlockSize = new Dimension2D(0, 0);

    public ElementRoot(TopicNode topic, Graphics g, MindMapConfig cfg, MindMapContext context) {
        super(topic, g, cfg, context);
    }

    protected ElementRoot(ElementRoot element) {
        super(element);
        this.leftBlockSize = DimensionUtils.copy(element.leftBlockSize);
        this.rightBlockSize = DimensionUtils.copy(element.rightBlockSize);
    }

    @Override
    public BaseElement makeCopy() {
        return new ElementRoot(this);
    }

    @Override
    public BaseElement makeCopyWith(Graphics g) {
        ElementRoot e = new ElementRoot(this);
        e.updateGraphics(g);
        return e;
    }

    @Override
    public boolean isMoveable() {
        return false;
    }

    @Override
    public boolean isCollapsed() {
        return false;
    }

    private Shape makeShape(double x, double y) {
        float round = mindMapContext.safeScale(10.0f, 0.1f);
        Rectangle rect = new Rectangle(x, y, this.bounds.getWidth(), this.bounds.getHeight());
        rect.setArcWidth(round);
        rect.setArcHeight(round);
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
    }

    @Override
    public void drawConnector(Rectangle2D source, Rectangle2D destination, boolean leftDirection) {
        g.setStroke(mindMapContext.safeScale(theme.getConnectorWidth(), 0.1f), StrokeType.SOLID);

        double startX;
        double endX;
        if (centerX(destination) < centerX(source)) {
            // left
            startX = centerX(source) - source.getWidth() / 4;
            endX = destination.getMaxX() - 10;
        }
        else {
            // right
            startX = centerX(source) + source.getWidth() / 4;
            endX = destination.getMinX() + 10;
        }

        g.drawCurve(startX, centerY(source), endX, centerY(destination), theme.getConnectorColor());
    }

    private double calcTotalChildrenHeight(double vertInset, boolean left) {
        double result = 0.0d;
        boolean nonfirst = false;
        for (TopicNode t : this.model.getChildren()) {
            BaseCollapsableElement w = assertNotNull((BaseCollapsableElement) t.getPayload());
            boolean lft = w.isLeftDirection();
            if ((left && lft) || (!left && !lft)) {
                if (nonfirst) {
                    result += vertInset;
                }
                else {
                    nonfirst = true;
                }
                result += w.getBlockSize().getHeight();
            }
        }
        return result;
    }

    @Override
    public void alignElementAndChildren(boolean leftSide, double cx, double cy) {
        super.alignElementAndChildren(leftSide, cx, cy);

        double dx = cx;
        double dy = cy;
        this.moveTo(dx, dy);

        double insetVert = theme.getFirstLevelVerticalInset() * mindMapContext.getScale();
        double insetHorz = theme.getFirstLevelHorizontalInset() * mindMapContext.getScale();

        double leftHeight = calcTotalChildrenHeight(insetVert, true);
        double rightHeight = calcTotalChildrenHeight(insetVert, false);

        if (leftHeight > 0.0d) {
            double ddx = dx - insetHorz;
            double ddy = dy - (leftHeight - this.bounds.getHeight()) / 2;
            for (TopicNode t : this.model.getChildren()) {
                BaseCollapsableElement c = assertNotNull((BaseCollapsableElement) t.getPayload());
                if (c.isLeftDirection()) {
                    c.alignElementAndChildren(true, ddx - c.getBlockSize().getWidth(), ddy);
                    ddy += c.getBlockSize().getHeight() + insetVert;
                }
            }
        }

        if (rightHeight > 0.0d) {
            double ddx = dx + this.bounds.getWidth() + insetHorz;
            double ddy = dy - (rightHeight - this.bounds.getHeight()) / 2;
            for (TopicNode t : this.model.getChildren()) {
                BaseCollapsableElement c = assertNotNull((BaseCollapsableElement) t.getPayload());
                if (!c.isLeftDirection()) {
                    c.alignElementAndChildren(false, ddx, ddy);
                    ddy += c.getBlockSize().getHeight() + insetVert;
                }
            }
        }
    }

    @Override
    public void updateElementBounds() {
        super.updateElementBounds();
        double marginOffset = ((theme.getTextMargins() + theme.getElementBorderWidth()) * 2.0d) * mindMapContext.getScale();
        this.bounds = new Rectangle2D(this.bounds.getMinY(), this.bounds.getMinY(), this.bounds.getWidth() + marginOffset, this.bounds.getHeight() + marginOffset);
    }

    public Dimension2D getLeftBlockSize() {
        return this.leftBlockSize;
    }

    public Dimension2D getRightBlockSize() {
        return this.rightBlockSize;
    }

    @Override
    public Dimension2D calcBlockSize(Dimension2D size, boolean childrenOnly) {
        double insetV = mindMapContext.getScale() * theme.getFirstLevelVerticalInset();
        double insetH = mindMapContext.getScale() * theme.getFirstLevelHorizontalInset();

        Dimension2D result = size;

        double leftWidth = 0.0d;
        double leftHeight = 0.0d;
        double rightWidth = 0.0d;
        double rightHeight = 0.0d;

        boolean nonfirstOnLeft = false;
        boolean nonfirstOnRight = false;

        for (TopicNode t : this.model.getChildren()) {
            ElementLevelFirst w = assertNotNull((ElementLevelFirst) t.getPayload());

            result = w.calcBlockSize(result, false);

            if (w.isLeftDirection()) {
                leftWidth = Math.max(leftWidth, result.getWidth());
                leftHeight += result.getHeight();
                if (nonfirstOnLeft) {
                    leftHeight += insetV;
                }
                else {
                    nonfirstOnLeft = true;
                }
            }
            else {
                rightWidth = Math.max(rightWidth, result.getWidth());
                rightHeight += result.getHeight();
                if (nonfirstOnRight) {
                    rightHeight += insetV;
                }
                else {
                    nonfirstOnRight = true;
                }
            }
        }

        if (!childrenOnly) {
            leftWidth += nonfirstOnLeft ? insetH : 0.0d;
            rightWidth += nonfirstOnRight ? insetH : 0.0d;
        }

        this.leftBlockSize = new Dimension2D(leftWidth, leftHeight);
        this.rightBlockSize = new Dimension2D(rightWidth, rightHeight);

        if (childrenOnly) {
            result = new Dimension2D(leftWidth + rightWidth, Math.max(leftHeight, rightHeight));
        }
        else {
            result = new Dimension2D(leftWidth + rightWidth + this.bounds.getWidth(), Math.max(this.bounds.getHeight(), Math.max(leftHeight, rightHeight)));
        }
        return result;
    }

    @Override
    public boolean hasDirection() {
        return true;
    }

    @Override
    public Color getBackgroundColor() {
        return this.fillColor == null ?  theme.getRootBackgroundColor() : this.fillColor;
    }

    @Override
    public Color getTextColor() {
        return this.textColor == null ? theme.getRootTextColor() : this.textColor;
    }

}
