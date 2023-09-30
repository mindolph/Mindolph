package com.mindolph.mindmap.model;

import com.mindolph.base.graphic.Graphics;
import com.mindolph.core.constant.TextConstants;
import com.mindolph.mfx.util.FontUtils;
import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import com.mindolph.mindmap.constant.TextAlign;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public final class TextBlock extends Block {
    private String text;
    private Line[] lines;
    private Font font;
    private double maxLineAscent;
    private TextAlign textAlign;
    private Color textColor;

    public TextBlock(TextBlock orig) {
        super(orig.cfg, orig.mindMapContext, orig.g);
        this.text = orig.text;
        this.lines = orig.lines.clone();
        this.font = orig.font;
        this.maxLineAscent = orig.maxLineAscent;
        super.bounds = RectangleUtils.copy(orig.getBounds());
        this.textAlign = orig.textAlign;
        this.textColor = orig.textColor;
    }

    public TextBlock(String text, TextAlign justify, Graphics g, MindMapConfig cfg, MindMapContext mindMapContext) {
        super(cfg, mindMapContext, g);
        updateText(text);
        this.textAlign = justify;
    }

    public void updateText(String text) {
        this.text = text == null ? "" : text;
        String[] texts = StringUtils.split(this.text, TextConstants.LINE_SEPARATOR);

        this.lines = new Line[texts.length];
        for (int i = 0; i < texts.length; i++) {
            this.lines[i] = new Line(texts[i], RectangleUtils.newZero());
        }
    }

    public Font getFont() {
        return font;
    }

    public TextAlign getTextAlign() {
        return this.textAlign;
    }

    public void setTextAlign(TextAlign textAlign) {
        this.textAlign = textAlign == null ? TextAlign.LEFT : textAlign;
    }

    @Override
    public void updateBounds() {
        // ugly but works
        this.font = FontUtils.newFontWithSize(cfg.getTopicFont(), cfg.getTopicFont().getSize() * mindMapContext.getScale());
//        this.font = cfg.getFont().deriveFont(AffineTransform.getScaleInstance(cfg.getScale(), cfg.getScale()));
        g.setFont(font);

        this.maxLineAscent = g.getFontMaxAscent();

        double maxWidth = 0.0d;
        double maxHeight = 0.0d;
        if (ArrayUtils.isEmpty(lines) || StringUtils.isBlank(lines[0].line)) {
            // force to set bounds as one letter if topic is blank.
            Rectangle2D rect = g.getStringBounds("X");
            maxWidth = rect.getWidth();
            maxHeight = rect.getHeight();
        }
        else {
            for (Line l : lines) {
                Rectangle2D lineBounds = g.getStringBounds(l.line);
                maxWidth = Math.max(lineBounds.getWidth(), maxWidth);
                maxHeight += lineBounds.getHeight();
                l.bounds = lineBounds;
            }
        }
        this.bounds = new Rectangle2D(0.0d, 0.0d, maxWidth, maxHeight);
    }

    @Override
    public void paint() {
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
                g.drawString(l.line, drawX, posy, textColor);
                posy += l.bounds.getHeight();
            }
        }
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    private static class Line {

        private Rectangle2D bounds;
        private String line;

        private Line(String line, Rectangle2D bounds) {
            this.line = line;
            this.bounds = bounds;
        }
    }

}
