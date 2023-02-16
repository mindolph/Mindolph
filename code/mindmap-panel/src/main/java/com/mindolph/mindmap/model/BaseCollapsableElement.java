package com.mindolph.mindmap.model;

import com.mindolph.base.constant.StrokeType;
import com.mindolph.base.graphic.Graphics;
import com.mindolph.mfx.util.DimensionUtils;
import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import com.mindolph.mindmap.constant.ElementPart;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import static com.mindolph.mfx.util.RectangleUtils.centerX;
import static com.mindolph.mfx.util.RectangleUtils.centerY;

public abstract class BaseCollapsableElement extends BaseElement {

    protected Rectangle2D collapsatorZone = RectangleUtils.newZero();

    protected BaseCollapsableElement(BaseCollapsableElement element) {
        super(element);
        this.collapsatorZone = RectangleUtils.copy(element.collapsatorZone);
    }

    public BaseCollapsableElement(TopicNode model, Graphics g, MindMapConfig cfg, MindMapContext context) {
        super(model, g, cfg, context);
    }

    protected void drawCollapsator(boolean collapsed) {
        double x = collapsatorZone.getMinX();
        double y = collapsatorZone.getMinY();
        double w = collapsatorZone.getWidth();
        double h = collapsatorZone.getHeight();

        double DELTA = Math.round(cfg.getCollapsatorSize() * 0.3d * mindMapContext.getScale());

        g.setStroke(mindMapContext.safeScale(cfg.getCollapsatorBorderWidth(), 0.1f), StrokeType.SOLID);
        Color lineColor = cfg.getCollapsatorBorderColor();
        g.drawOval(x, y, w, h, lineColor, cfg.getCollapsatorBackgroundColor());
        g.drawLine(x + DELTA, y + h / 2, x + w - DELTA, y + h / 2, lineColor);
        if (collapsed) {
            g.drawLine(x + w / 2, y + DELTA, x + w / 2, y + h - DELTA, lineColor);
        }
    }

    @Override
    public ElementPart findPartForPoint(Point2D point) {
        ElementPart result = super.findPartForPoint(point);
        if (result == ElementPart.NONE) {
            if (this.hasChildren()) {
                if (this.collapsatorZone.contains(point.getX() - this.bounds.getMinX(), point.getY() - this.bounds.getMinY())) {
                    result = ElementPart.COLLAPSATOR;
                }
            }
        }
        return result;
    }

    @Override
    public boolean isCollapsed() {
        return this.model.isCollapsed();
    }

    public void setCollapse(boolean collapseElementFlag) {
        this.model.setCollapsed(collapseElementFlag);
    }

    public void foldAllChildren() {

    }

    public void collapseAllFirstLevelChildren() {
        for (TopicNode t : this.model.getChildren()) {
            t.setCollapsed(true);
        }
    }

    @Override
    public boolean isLeftDirection() {
        return this.model.isLeftSidedTopic();
    }

    public void setLeftDirection(boolean leftSide) {
        this.model.makeTopicLeftSided(leftSide);
    }

    @Override
    public Dimension2D calcBlockSize(Dimension2D size, boolean childrenOnly) {
        Dimension2D result = size == null ? DimensionUtils.newZero() : size;

        double scaledVInset = mindMapContext.getScale() * cfg.getOtherLevelVerticalInset();
        double scaledHInset = mindMapContext.getScale() * cfg.getOtherLevelHorizontalInset();

        double width = childrenOnly ? 0.0d : this.bounds.getWidth();
        double height = childrenOnly ? 0.0d : this.bounds.getHeight();

        if (this.hasChildren()) {
            if (!this.isCollapsed()) {
                width += childrenOnly ? 0.0d : scaledHInset;

                double baseWidth = childrenOnly ? 0.0d : width;
                double childrenHeight = 0.0d;

                boolean notFirstChiild = false;

                for (TopicNode t : this.model.getChildren()) {
                    if (notFirstChiild) {
                        childrenHeight += scaledVInset;
                    }
                    else {
                        notFirstChiild = true;
                    }
                    result = ((BaseElement) t.getPayload()).calcBlockSize(result, false);
                    width = Math.max(baseWidth + result.getWidth(), width);
                    childrenHeight += result.getHeight();
                }

                height = Math.max(height, childrenHeight);
            }
            else if (!childrenOnly) {
                width += cfg.getCollapsatorSize() * mindMapContext.getScale();
            }
        }
        result = new Dimension2D(width, height);
        return result;
    }

    @Override
    public void alignElementAndChildren(boolean leftSide, double leftX, double topY) {
        super.alignElementAndChildren(leftSide, leftX, topY);

        double horzInset = cfg.getOtherLevelHorizontalInset() * mindMapContext.getScale();

        double childrenX;

        double COLLAPSATORSIZE = cfg.getCollapsatorSize() * mindMapContext.getScale();
        double COLLAPSATORDISTANCE = cfg.getCollapsatorSize() * 0.1d * mindMapContext.getScale();

        double collapsatorX;

        if (leftSide) {
            childrenX = leftX + this.blockSize.getWidth() - this.bounds.getWidth();
            this.moveTo(childrenX, topY + (this.blockSize.getHeight() - this.bounds.getHeight()) / 2);
            childrenX -= horzInset;
            collapsatorX = -COLLAPSATORSIZE - COLLAPSATORDISTANCE;
        }
        else {
            childrenX = leftX;
            this.moveTo(childrenX, topY + (this.blockSize.getHeight() - this.bounds.getHeight()) / 2);
            childrenX += this.bounds.getWidth() + horzInset;
            collapsatorX = this.bounds.getWidth() + COLLAPSATORDISTANCE;
        }

        this.collapsatorZone = new Rectangle2D(collapsatorX, (this.bounds.getHeight() - COLLAPSATORSIZE) / 2, COLLAPSATORSIZE, COLLAPSATORSIZE);
        //this.collapsatorZone.setRect(collapsatorX, (this.bounds.getHeight() - COLLAPSATORSIZE) / 2, COLLAPSATORSIZE, COLLAPSATORSIZE);

        if (!this.isCollapsed()) {
            double vertInset = cfg.getOtherLevelVerticalInset() * mindMapContext.getScale();

            Dimension2D childBlockSize = calcBlockSize(null, true);
            double currentY = topY + (this.blockSize.getHeight() - childBlockSize.getHeight()) / 2.0d;

            boolean notFirstChild = false;

            for (TopicNode t : this.model.getChildren()) {
                if (notFirstChild) {
                    currentY += vertInset;
                }
                else {
                    notFirstChild = true;
                }
                BaseElement w = (BaseElement) t.getPayload();
                w.alignElementAndChildren(leftSide, leftSide ? childrenX - w.getBlockSize().getWidth() : childrenX, currentY);
                currentY += w.getBlockSize().getHeight();
            }
        }
    }

    @Override
    public void doPaintConnectors(boolean isLeftDirection) {
        // source is the collapsator
        Rectangle2D source = new Rectangle2D(
                this.bounds.getMinX() + this.collapsatorZone.getMinX(),
                this.bounds.getMinY() + this.collapsatorZone.getMinY(),
                this.collapsatorZone.getWidth(),
                this.collapsatorZone.getHeight());
        for (TopicNode t : this.model.getChildren()) {
            this.drawConnector(source, ((BaseElement) t.getPayload()).getBounds(), isLeftDirection());
        }
    }

    @Override
    public void drawConnector(Rectangle2D source, Rectangle2D destination, boolean isLeftDirection) {
        g.setStroke(mindMapContext.safeScale(cfg.getConnectorWidth(), 0.1f), StrokeType.SOLID);

        double dy = Math.abs(centerY(destination) - centerY(source));
        if (dy < (16.0d * mindMapContext.getScale())) {
            g.drawLine(isLeftDirection ? source.getMaxX() : source.getMinX(),
                    centerY(source), centerX(destination), centerY(source),
                    cfg.getConnectorColor());
        }
        else {
            Path path = new Path();

            if (isLeftDirection) {
                path.getElements().add(new MoveTo(source.getMaxX(), centerY(source)));
                double dx = source.getMaxX() - destination.getMaxX() + 8;
                path.getElements().add(new LineTo((source.getMaxX() - dx / 2), centerY(source)));
                path.getElements().add(new LineTo((source.getMaxX() - dx / 2), centerY(destination)));
            }
            else {
                path.getElements().add(new MoveTo(source.getMinX(), centerY(source)));
                double dx = destination.getMinX() - source.getMinX() + 8;
                path.getElements().add(new LineTo((source.getMinX() + dx / 2), centerY(source)));
                path.getElements().add(new LineTo((source.getMinX() + dx / 2), centerY(destination)));
            }
            path.getElements().add(new LineTo(centerX(destination), centerY(destination)));

            g.draw(path, cfg.getConnectorColor(), null);
        }
    }

    @Override
    public BaseElement findForPoint(Point2D point) {
        BaseElement result = null;
        if (point != null) {
            if (this.bounds.contains(point.getX(), point.getY()) || this.collapsatorZone.contains(point.getX() - this.bounds.getMinX(), point.getY() - this.bounds.getMinY())) {
                result = this;
            }
            else if (!isCollapsed()) {
                double topZoneY = this.bounds.getMinY() - (this.blockSize.getHeight() - this.bounds.getHeight()) / 2;
                double topZoneX = isLeftDirection() ? this.bounds.getMaxX() - this.blockSize.getWidth() : this.bounds.getMinX();

                if (point.getX() >= topZoneX && point.getY() >= topZoneY && point.getX() < (this.blockSize.getWidth() + topZoneX) && point.getY() < (this.blockSize.getHeight() + topZoneY)) {
                    for (TopicNode t : this.model.getChildren()) {
                        BaseElement w = (BaseElement) t.getPayload();
                        result = w == null ? null : w.findForPoint(point);
                        if (result != null) {
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void updateElementBounds() {
        super.updateElementBounds();
        double marginOffset = ((cfg.getTextMargins() + cfg.getElementBorderWidth()) * 2.0d) * mindMapContext.getScale();
        this.bounds = new Rectangle2D(this.bounds.getMinX(), this.bounds.getMinY(),
                this.bounds.getWidth() + marginOffset, this.bounds.getHeight() + marginOffset);
    }

    public Rectangle2D getCollapsatorArea() {
        return this.collapsatorZone;
    }

    @Override
    public boolean hasDirection() {
        return true;
    }

}
