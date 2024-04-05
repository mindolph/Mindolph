package com.mindolph.mindmap.drawing;

import com.mindolph.mindmap.theme.BorderType;
import com.mindolph.mindmap.theme.MindMapTheme;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

public class FirstLevelTopicComponent extends BaseTopicComponent {

    public FirstLevelTopicComponent() {
    }

    public FirstLevelTopicComponent(Rectangle2D bounds) {
        super(bounds);
    }

    public FirstLevelTopicComponent(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    protected BorderType getBorderType(MindMapTheme theme){
        return theme.getFirstLevelBorderType();
    }

    protected Color getBackgroundColor(MindMapTheme theme) {
        return theme.getFirstLevelBackgroundColor();
    }

    protected float getRoundRadius(MindMapTheme theme) {
        return theme.getRoundRadius();
    }
}
