package com.mindolph.mindmap.model;

import com.mindolph.base.graphic.Graphics;
import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import javafx.geometry.Rectangle2D;

/**
 * @author mindolph
 */
public abstract class Block {

    protected final MindMapConfig cfg;
    protected final MindMapContext mindMapContext;
    protected Graphics g;
    protected Rectangle2D bounds = RectangleUtils.newZero();

    public Block(MindMapConfig cfg, MindMapContext mindMapContext, Graphics g) {
        this.cfg = cfg;
        this.mindMapContext = mindMapContext;
        this.g = g;
    }

    public void translate(double x, double y) {
        this.bounds = new Rectangle2D(x, y, this.bounds.getWidth(), this.bounds.getHeight());
    }

    /**
     * Update bounds if necessary, like content changes.
     */
    abstract void updateBounds();


    abstract void paint();


    public void setGraphics(Graphics g) {
        this.g = g;
    }

    public Rectangle2D getBounds() {
        return bounds;
    }
}
