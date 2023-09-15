package com.mindolph.mindmap.model;

import com.mindolph.base.constant.StrokeType;
import com.mindolph.base.graphic.Graphics;
import com.mindolph.mfx.util.DimensionUtils;
import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import com.mindolph.mindmap.constant.ElementPart;
import com.mindolph.mindmap.theme.BorderType;
import com.mindolph.mindmap.theme.ConnectorStyle;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

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

        double DELTA = Math.round(theme.getCollapsatorSize() * 0.3d * mindMapContext.getScale());

        g.setStroke(mindMapContext.safeScale(theme.getCollapsatorBorderWidth(), 0.1f), StrokeType.SOLID);
        Color lineColor = theme.getCollapsatorBorderColor();
        g.drawOval(x, y, w, h, lineColor, theme.getCollapsatorBackgroundColor());
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


    public void foldAllChildren() {

    }

    public void collapseAllFirstLevelChildren() {
        for (TopicNode t : this.model.getChildren()) {
            t.setCollapsed(true);
        }
    }

    @Override
    public Dimension2D calcBlockSize(Dimension2D size, boolean childrenOnly) {
        Dimension2D result = size == null ? DimensionUtils.newZero() : size;

        double scaledVInset = mindMapContext.getScale() * theme.getOtherLevelVerticalInset();
        double scaledHInset = mindMapContext.getScale() * theme.getOtherLevelHorizontalInset();

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
                width += theme.getCollapsatorSize() * mindMapContext.getScale();
            }
        }
        result = new Dimension2D(width, height);
        return result;
    }

    @Override
    public void alignElementAndChildren(boolean leftSide, double leftX, double topY) {
        super.alignElementAndChildren(leftSide, leftX, topY);

        double horzInset = theme.getOtherLevelHorizontalInset() * mindMapContext.getScale();

        double childrenX;

        double COLLAPSATOR_SIZE = theme.getCollapsatorSize() * mindMapContext.getScale();
        double COLLAPSATOR_DISTANCE = theme.getCollapsatorSize() * 0.1d * mindMapContext.getScale();

        double collapsatorX;

        if (leftSide) {
            childrenX = leftX + this.blockSize.getWidth() - this.bounds.getWidth();
            this.moveTo(childrenX, topY + (this.blockSize.getHeight() - this.bounds.getHeight()) / 2);
            childrenX -= horzInset;
            collapsatorX = -COLLAPSATOR_SIZE - COLLAPSATOR_DISTANCE;
        }
        else {
            childrenX = leftX;
            this.moveTo(childrenX, topY + (this.blockSize.getHeight() - this.bounds.getHeight()) / 2);
            childrenX += this.bounds.getWidth() + horzInset;
            collapsatorX = this.bounds.getWidth() + COLLAPSATOR_DISTANCE;
        }

        // the collapsator Y depends on the border type
        double collapsatorY = super.collapsatorY(theme.getBorderType(), this.bounds, COLLAPSATOR_SIZE);
        this.collapsatorZone = new Rectangle2D(collapsatorX, collapsatorY,
                COLLAPSATOR_SIZE, COLLAPSATOR_SIZE);

        if (!this.isCollapsed()) {
            double vertInset = theme.getOtherLevelVerticalInset() * mindMapContext.getScale();

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
        for (TopicNode t : this.model.getChildren()) {
            this.drawConnector(super.bounds, ((BaseElement) t.getPayload()).getBounds(), isLeftDirection());
        }
    }

    @Override
    public void drawConnector(Rectangle2D source, Rectangle2D destination, boolean isLeftDirection) {
        g.setStroke(mindMapContext.safeScale(theme.getConnectorWidth(), 0.1f), StrokeType.SOLID);

        // different level topics might have different style.
        BorderType borderType = getBorderType();

        Point2D sourcePoint = sourcePoint(borderType, source, isLeftDirection);
        Point2D destPoint = destinationPoint(theme.getBorderType(), destination, isLeftDirection);
        double endX = destPoint.getX() + (isLeftDirection ? -destination.getWidth() : destination.getWidth());

        if (theme.getConnectorStyle() == ConnectorStyle.POLYLINE) {
            double dy = Math.abs(destPoint.getY() - centerY(source));
            if (dy < (16.0d * mindMapContext.getScale())) {
                g.drawLine(sourcePoint.getX(),
                        sourcePoint.getY(),
                        destPoint.getX(),
                        sourcePoint.getY(), // force to align with source
                        theme.getConnectorColor());
            }
            else {
                Path path = new Path();
                if (isLeftDirection) {
                    path.getElements().add(new MoveTo(sourcePoint.getX(), sourcePoint.getY()));
                    double dx = source.getMinX() - destination.getMaxX();
                    path.getElements().add(new LineTo((source.getMinX() - dx / 2), sourcePoint.getY()));
                    path.getElements().add(new LineTo((source.getMinX() - dx / 2), destPoint.getY()));
                }
                else {
                    path.getElements().add(new MoveTo(sourcePoint.getX(), sourcePoint.getY()));
                    double dx = destination.getMinX() - source.getMaxX();
                    path.getElements().add(new LineTo((source.getMaxX() + dx / 2), sourcePoint.getY()));
                    path.getElements().add(new LineTo((source.getMaxX() + dx / 2), destPoint.getY()));
                }
                path.getElements().add(new LineTo(destPoint.getX(), destPoint.getY()));
                g.draw(path, theme.getConnectorColor(), null);
            }
        }
        else if (theme.getConnectorStyle() == ConnectorStyle.BEZIER) {
            // draw bezier style connector
            g.drawBezier(sourcePoint.getX(), sourcePoint.getY(), destPoint.getX(), destPoint.getY(), theme.getConnectorColor());
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
        double marginOffset = ((theme.getTextMargins() + theme.getElementBorderWidth()) * 2.0d) * mindMapContext.getScale();
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

    @Override
    public boolean isLeftDirection() {
        return this.model.isLeftSidedTopic();
    }

    public void setLeftDirection(boolean leftSide) {
        this.model.makeTopicLeftSided(leftSide);
    }

    @Override
    public boolean isCollapsed() {
        return this.model.isCollapsed();
    }

    public void setCollapse(boolean collapseElementFlag) {
        this.model.setCollapsed(collapseElementFlag);
    }
}
