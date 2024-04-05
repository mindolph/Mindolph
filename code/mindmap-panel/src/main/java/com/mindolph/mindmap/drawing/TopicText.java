package com.mindolph.mindmap.drawing;

import com.mindolph.mfx.drawing.Context;
import com.mindolph.mfx.drawing.Graphics;
import com.mindolph.mindmap.theme.MindMapTheme;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public class TopicText extends TopicBlock {

    protected String text;

    public TopicText() {
    }

    public TopicText(Rectangle2D bounds) {
        super(bounds);
    }

    public TopicText(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    public void draw(Graphics g, Context context) {
        super.draw(g, context);
        MindMapDrawingContext c = (MindMapDrawingContext) context;
        MindMapTheme theme = c.getTheme();
        Point2D p = super.getAbsoluteMinPoint();
        double y = p.getY() + g.getFontMaxAscent();
        g.drawString(this.text, p.getX(), y, theme.getRootTextColor());
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
