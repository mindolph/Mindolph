package com.mindolph.mindmap.model;

import com.mindolph.base.graphic.Graphics;
import com.mindolph.mfx.util.FontUtils;
import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import com.mindolph.mindmap.constant.TextAlign;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static com.igormaznitsa.mindmap.model.ModelUtils.breakToLines;

public final class TextBlock implements Cloneable {
    private Graphics g;
    private final MindMapConfig cfg;
    private final MindMapContext mindMapContext;
    private static final Rectangle2D ZERO = new Rectangle2D(0, 0, 0, 0);
    private Rectangle2D bounds = new Rectangle2D(0, 0, 0, 0);
    private String text;
    private Line[] lines;
    private Font font;
    private double maxLineAscent;
    private TextAlign textAlign;

    public TextBlock(TextBlock orig) {
        this.cfg = orig.cfg;
        this.g = orig.g;
        this.mindMapContext = orig.mindMapContext;
        this.text = orig.text;
        this.lines = orig.lines.clone();
        this.font = orig.font;
        this.maxLineAscent = orig.maxLineAscent;
        this.bounds = RectangleUtils.copy(orig.getBounds());
        this.textAlign = orig.textAlign;
    }

    public TextBlock(String text, TextAlign justify, Graphics g, MindMapConfig cfg, MindMapContext mindMapContext) {
        updateText(text);
        this.textAlign = justify;
        this.cfg = cfg;
        this.g = g;
        this.mindMapContext = mindMapContext;
    }

    public void updateText(String text) {
        this.text = text == null ? "" : text;
        invalidate();
    }

    public Font getFont() {
        return font;
    }

    public Rectangle2D getBounds() {
        return this.bounds == null ? ZERO : this.bounds;
    }

    public TextAlign getTextAlign() {
        return this.textAlign;
    }

    public void setTextAlign(TextAlign textAlign) {
        this.textAlign = textAlign == null ? TextAlign.LEFT : textAlign;
        invalidate();
    }

    public void invalidate() {
        this.lines = null;
    }

    public void setCoordOffset(double x, double y) {
        this.bounds = new Rectangle2D(x, y, this.bounds.getWidth(), this.bounds.getHeight());
    }

    public void updateSize() {
        // ugly but works
        this.font = FontUtils.newFontWithSize(cfg.getTheme().getTopicFont(), cfg.getTheme().getTopicFont().getSize() * mindMapContext.getScale());
//        this.font = cfg.getFont().deriveFont(AffineTransform.getScaleInstance(cfg.getScale(), cfg.getScale()));
        g.setFont(font);

        this.maxLineAscent = g.getFontMaxAscent();

        double maxWidth = 0.0d;
        double maxHeight = 0.0d;

        String[] brokenText = breakToLines(this.text);

        this.lines = new Line[brokenText.length];

        int index = 0;
        for (String s : brokenText) {
            Rectangle2D lineBounds = g.getStringBounds(s);
            maxWidth = Math.max(lineBounds.getWidth(), maxWidth);
            maxHeight += lineBounds.getHeight();
            this.lines[index++] = new Line(s, lineBounds);
        }
        this.bounds = new Rectangle2D(0.0d, 0.0d, maxWidth, maxHeight);
    }

    public void paint(Color color) {
        if (this.font != null && this.lines != null) {
            double posy = this.bounds.getMinY() + this.maxLineAscent;
            g.setFont(this.font);
            for (Line l : this.lines) {
                double drawX;
                switch (this.textAlign) {
                    case LEFT -> {
                        drawX = this.bounds.getMinX();
                    }
                    case CENTER -> {
                        drawX = this.bounds.getMinX() + (this.bounds.getWidth() - l.bounds.getWidth()) / 2;
                    }
                    case RIGHT -> {
                        drawX = this.bounds.getMinX() + (this.bounds.getWidth() - l.bounds.getWidth());
                    }
                    default -> throw new Error("unsupported text alignment");
                }
                g.drawString(l.line, drawX, posy, color);
                posy += l.bounds.getHeight();
            }
        }
    }

    public void updateGraphics(Graphics g) {
        this.g = g;
    }

    private static class Line {

        private Rectangle2D bounds;
        private String line;

        private Line(String line, Rectangle2D bounds) {
            this.bounds = bounds;
            this.line = line;
        }
    }

}
