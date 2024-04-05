package com.mindolph.mindmap.drawing;

import com.mindolph.mfx.drawing.Context;
import com.mindolph.mfx.drawing.Graphics;
import com.mindolph.mfx.drawing.component.Component;
import com.mindolph.mfx.drawing.connector.BezierConnector;
import com.mindolph.mfx.drawing.constant.StrokeType;
import com.mindolph.mindmap.theme.MindMapTheme;
import javafx.geometry.Point2D;

public class TopicConnector extends BezierConnector {
    public TopicConnector(Component from, Component to) {
        super(from, to);
    }

    public TopicConnector(Component from, Component to, Point2D pointFrom, Point2D pointTo) {
        super(from, to, pointFrom, pointTo);
    }

    public TopicConnector(Component from, Component to, double x1, double y1, double x2, double y2) {
        super(from, to, x1, y1, x2, y2);
    }

    @Override
    public void draw(Graphics g, Context context) {
        super.draw(g, context);
        MindMapDrawingContext c = (MindMapDrawingContext) context;
        MindMapTheme theme = c.getTheme();
        g.setStroke(theme.getConnectorWidth(), StrokeType.SOLID);
        g.drawBezier(super.absolutePointFrom.getX(), super.absolutePointFrom.getY(), super.absolutePointTo.getX(), super.absolutePointTo.getY(), theme.getConnectorColor());
    }
}
