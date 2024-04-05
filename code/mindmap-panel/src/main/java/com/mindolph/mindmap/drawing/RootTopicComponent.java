package com.mindolph.mindmap.drawing;

import com.mindolph.mindmap.theme.BorderType;
import com.mindolph.mindmap.theme.MindMapTheme;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

public class RootTopicComponent extends BaseTopicComponent {
    public RootTopicComponent(Rectangle2D bounds) {
        super(bounds);
    }

    public RootTopicComponent(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    protected BorderType getBorderType(MindMapTheme theme) {
        return BorderType.BOX;
    }

    @Override
    protected Color getBackgroundColor(MindMapTheme theme) {
        return theme.getRootBackgroundColor();
    }

    @Override
    protected float getRoundRadius(MindMapTheme theme) {
        return 10.0f;
    }

}
