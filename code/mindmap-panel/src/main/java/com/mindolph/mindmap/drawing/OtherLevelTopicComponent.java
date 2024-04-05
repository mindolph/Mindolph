package com.mindolph.mindmap.drawing;

import com.mindolph.mindmap.theme.BorderType;
import com.mindolph.mindmap.theme.MindMapTheme;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

public class OtherLevelTopicComponent extends BaseTopicComponent {
    public OtherLevelTopicComponent() {
    }

    public OtherLevelTopicComponent(Rectangle2D bounds) {
        super(bounds);
    }

    public OtherLevelTopicComponent(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    protected BorderType getBorderType(MindMapTheme theme) {
        return BorderType.LINE;
    }

    @Override
    protected Color getBackgroundColor(MindMapTheme theme) {
        return theme.getOtherLevelBackgroundColor();
    }

    @Override
    protected float getRoundRadius(MindMapTheme theme) {
        return theme.getRoundRadius();
    }
}
