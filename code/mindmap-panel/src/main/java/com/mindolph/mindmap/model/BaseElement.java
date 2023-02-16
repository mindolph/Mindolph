package com.mindolph.mindmap.model;

import com.mindolph.base.graphic.Graphics;
import com.mindolph.base.util.ColorUtils;
import com.mindolph.mfx.util.DimensionUtils;
import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import com.mindolph.mindmap.constant.ElementPart;
import com.mindolph.mindmap.constant.TextAlign;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

import static com.mindolph.mindmap.constant.StandardTopicAttribute.*;

public abstract class BaseElement {

    protected Graphics g;

    protected final MindMapConfig cfg;

    protected final MindMapContext mindMapContext;

    protected final TopicNode model;

    protected final TextBlock textBlock;

    protected final IconBlock extrasIconBlock;

    protected final VisualAttributeImageBlock visualAttributeImageBlock;

    protected Rectangle2D bounds = RectangleUtils.newZero();
    protected Dimension2D blockSize = DimensionUtils.newZero();

    protected Color fillColor;
    protected Color textColor;
    protected Color borderColor;

    protected BaseElement(BaseElement orig) {
        this.model = orig.model;
        this.cfg = orig.cfg;
        this.g = orig.g;
        this.mindMapContext = orig.mindMapContext;
        this.textBlock = new TextBlock(orig.textBlock);
        this.extrasIconBlock = new IconBlock(orig.extrasIconBlock);
        this.visualAttributeImageBlock = new VisualAttributeImageBlock(orig.visualAttributeImageBlock);
        this.bounds = RectangleUtils.copy(orig.bounds);
        this.blockSize = DimensionUtils.copy(orig.blockSize);
        this.fillColor = orig.fillColor;
        this.textColor = orig.textColor;
        this.borderColor = orig.borderColor;
    }

    public BaseElement(TopicNode model, Graphics g, MindMapConfig cfg, MindMapContext context) {
        this.model = model;
        this.cfg = cfg;
        this.g = g;
        this.mindMapContext = context;
        this.textBlock = new TextBlock(this.model.getText(), TextAlign.findForName(model.getAttribute("align")), g, cfg, context);
        this.textBlock.setTextAlign(TextAlign.findForName(model.getAttribute("align")));
        this.extrasIconBlock = new IconBlock(model, g, cfg, context);
        this.visualAttributeImageBlock = new VisualAttributeImageBlock(model, g, cfg, context);
        updateColorAttributeFromModel();
    }


    public String getText() {
        return this.model.getText();
    }

    public void setText(String text) {
        this.model.setText(text);
        this.textBlock.updateText(text);
    }

    public final void updateColorAttributeFromModel() {
        this.borderColor = ColorUtils.html2color(this.model.getAttribute(ATTR_BORDER_COLOR.getText()), false);
        this.textColor = ColorUtils.html2color(this.model.getAttribute(ATTR_TEXT_COLOR.getText()), false);
        this.fillColor = ColorUtils.html2color(this.model.getAttribute(ATTR_FILL_COLOR.getText()), false);
    }

    public BaseElement getParent() {
        TopicNode parent = this.model.getParent();
        return parent == null ? null : (BaseElement) parent.getPayload();
    }


    public TopicNode getModel() {
        return this.model;
    }


    public TextAlign getTextAlign() {
        return this.textBlock.getTextAlign();
    }

    public void setTextAlign(TextAlign textAlign) {
        this.textBlock.setTextAlign(textAlign);
        this.model.setAttribute("align", this.textBlock.getTextAlign().name());
    }

//    public void setBounds(Rectangle2D rectangle2D) {
//        this.bounds = rectangle2D;
//    }
//
//    public void updateWidth(double width) {
//        this.bounds = RectangleUtils.newWithWidth(this.bounds, width);
//    }
//
//    public void updateHeight(double height) {
//        this.bounds = RectangleUtils.newWithHeight(this.bounds, height);
//    }

    public void updateElementBounds() {
        this.visualAttributeImageBlock.updateSize();
        this.textBlock.updateSize();
        this.extrasIconBlock.updateSize();

        double scaledHorzBlockGap = mindMapContext.getScale() * cfg.getHorizontalBlockGap();

        double width = 0.0d;
        if (this.visualAttributeImageBlock.mayHaveContent()) {
            width += this.visualAttributeImageBlock.getBounds().getWidth() + scaledHorzBlockGap;
        }

        width += this.textBlock.getBounds().getWidth();

        if (this.extrasIconBlock.hasContent()) {
            width += this.extrasIconBlock.getBounds().getWidth() + scaledHorzBlockGap;
        }

        this.bounds = new Rectangle2D(0d, 0d,
                width,
                Math.max(this.visualAttributeImageBlock.getBounds().getHeight(),
                        Math.max(this.textBlock.getBounds().getHeight(), this.extrasIconBlock.getBounds().getHeight()))
        );
    }

    public void updateBlockSize() {
        this.blockSize = this.calcBlockSize(this.blockSize, false);
    }


    public Dimension2D getBlockSize() {
        return this.blockSize;
    }

    public void moveTo(double x, double y) {
        this.bounds = new Rectangle2D(x, y, this.bounds.getWidth(), this.bounds.getHeight());
    }

    public void moveWholeTreeBranchCoordinates(double deltaX, double deltaY) {
        moveTo(this.bounds.getMinX() + deltaX, this.bounds.getMinY() + deltaY);
        for (TopicNode t : this.model.getChildren()) {
            BaseElement el = (BaseElement) t.getPayload();
            if (el != null) {
                el.moveWholeTreeBranchCoordinates(deltaX, deltaY);
            }
        }
    }


    public Rectangle2D getBounds() {
        return this.bounds;
    }

    public final void doPaint(boolean drawCollapsator) {
        if (this.hasChildren() && !isCollapsed()) {
            doPaintConnectors(isLeftDirection());
        }
        Rectangle2D canvasClip = g.getClipBounds();
        if (canvasClip.intersects(this.bounds)) {
            g.translate(this.bounds.getMinX(), this.bounds.getMinY()); // translate to draw correct position in canvas.
            drawComponent(isCollapsed() || drawCollapsator);
            g.translate(-this.bounds.getMinX(), -this.bounds.getMinY());
        }
    }

    public final void doPaint(double x, double y, double opacity) {
        g.translate(x, y);
        g.setOpacity(opacity);
        drawComponent(isCollapsed());
        g.setOpacity(1);
        g.translate(-x, -y);
    }

    public void doPaintConnectors(boolean isLeftDirection) {
        Rectangle2D source = this.bounds;
        for (TopicNode t : this.model.getChildren()) {
            drawConnector(source, ((BaseElement) t.getPayload()).getBounds(), isLeftDirection);
        }
    }

    public boolean hasChildren() {
        return this.model.hasChildren();
    }

    public abstract void drawComponent(boolean drawCollapsator);

    public abstract void drawConnector(Rectangle2D source, Rectangle2D destination,
                                       boolean leftDirection);

    public abstract boolean isMoveable();

    public abstract boolean isCollapsed();

    public void alignElementAndChildren(boolean leftSide, double centerX, double centerY) {
        double textMargin = mindMapContext.getScale() * cfg.getTextMargins();
        double centralBlockLineY = textMargin +
                Math.max(this.visualAttributeImageBlock.getBounds().getHeight(),
                        Math.max(this.textBlock.getBounds().getHeight(), this.extrasIconBlock.getBounds().getHeight())) / 2;

        double scaledHorzBlockGap = mindMapContext.getScale() * cfg.getHorizontalBlockGap();

        double offset = textMargin;

        if (this.visualAttributeImageBlock.mayHaveContent()) {
            this.visualAttributeImageBlock.setCoordOffset(offset, centralBlockLineY - this.visualAttributeImageBlock.getBounds().getHeight() / 2);
            offset += this.visualAttributeImageBlock.getBounds().getWidth() + scaledHorzBlockGap;
        }

        this.textBlock.setCoordOffset(offset, centralBlockLineY - this.textBlock.getBounds().getHeight() / 2);
        offset += this.textBlock.getBounds().getWidth() + scaledHorzBlockGap;

        if (this.extrasIconBlock.hasContent()) {
            this.extrasIconBlock.setCoordOffset(offset, centralBlockLineY - this.extrasIconBlock.getBounds().getHeight() / 2);
        }
    }


    public abstract Dimension2D calcBlockSize(Dimension2D size, boolean childrenOnly);

    public abstract boolean hasDirection();


    public ElementPart findPartForPoint(Point2D point) {
        ElementPart result = ElementPart.NONE;
        if (this.bounds.contains(point)) {
            double offX = point.getX() - this.bounds.getMinX();
            double offY = point.getY() - this.bounds.getMinY();

            result = ElementPart.AREA;
            if (this.visualAttributeImageBlock.getBounds().contains(offX, offY)) {
                result = ElementPart.VISUAL_ATTRIBUTES;
            }
            else {
                if (this.textBlock.getBounds().contains(offX, offY)) {
                    result = ElementPart.TEXT;
                }
                else if (this.extrasIconBlock.getBounds().contains(offX, offY)) {
                    result = ElementPart.ICONS;
                }
            }
        }
        return result;
    }

    public BaseElement findForPoint(Point2D point) {
        BaseElement result = null;
        if (point != null) {
            if (this.bounds.contains(point)) {
                result = this;
            }
            else {
                for (TopicNode t : this.model.getChildren()) {
                    BaseElement w = (BaseElement) t.getPayload();
                    result = w == null ? null : w.findForPoint(point);
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    public boolean isLeftDirection() {
        return false;
    }


    public TextBlock getTextBlock() {
        return this.textBlock;
    }


    public IconBlock getIconBlock() {
        return this.extrasIconBlock;
    }


    public VisualAttributeImageBlock getVisualAttributeImageBlock() {
        return this.visualAttributeImageBlock;
    }

    public boolean collapseOrExpandAllChildren(boolean collapse) {
        boolean result = false;

        if (this instanceof BaseCollapsableElement el) {
            if (collapse) {
                if (!el.isCollapsed()) {
                    el.setCollapse(true);
                    result = true;
                }
            }
            else if (el.isCollapsed()) {
                el.setCollapse(false);
                result = true;
            }
        }

        if (this.hasChildren()) {
            for (TopicNode t : this.model.getChildren()) {
                BaseElement e = (BaseElement) t.getPayload();
                if (e != null) {
                    result |= e.collapseOrExpandAllChildren(collapse);
                }
            }
        }

        return result;
    }


    public abstract Color getBackgroundColor();


    public abstract Color getTextColor();


    public Color getBorderColor() {
        return this.borderColor == null ? cfg.getElementBorderColor() : this.borderColor;
    }


    public abstract BaseElement makeCopy();

    /**
     * Make copy of this element but with another Graphics instance.
     *
     * @param g
     * @return
     * @deprecated
     */
    public abstract BaseElement makeCopyWith(Graphics g);

    // to update Graphics for this element and all it's children, this is used for drawing dragging elements.
    // this probably useless when everything drawing on one canvas.
    void updateGraphics(Graphics g) {
        this.g = g;
        textBlock.updateGraphics(g);
        visualAttributeImageBlock.updateGraphics(g);
        extrasIconBlock.updateGraphics(g);
    }

    public Graphics getGraphics() {
        return g;
    }

    public MindMapConfig getConfig() {
        return cfg;
    }

    public MindMapContext getMindMapContext() {
        return mindMapContext;
    }
}
