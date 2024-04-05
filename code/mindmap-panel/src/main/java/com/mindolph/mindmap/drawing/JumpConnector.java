package com.mindolph.mindmap.drawing;

import com.mindolph.mfx.drawing.*;
import com.mindolph.mfx.drawing.component.Component;
import com.mindolph.mfx.drawing.connector.Connector;
import javafx.geometry.Point2D;

public class JumpConnector extends Connector {
    public JumpConnector(Component from, Component to) {
        super(from, to);
    }

    public JumpConnector(Component from, Component to, Point2D pointFrom, Point2D pointTo) {
        super(from, to, pointFrom, pointTo);
    }

    public JumpConnector(Component from, Component to, double x1, double y1, double x2, double y2) {
        super(from, to, x1, y1, x2, y2);
    }

    @Override
    public void draw(Graphics g, Context context) {
        super.draw(g, context);
    }
}
