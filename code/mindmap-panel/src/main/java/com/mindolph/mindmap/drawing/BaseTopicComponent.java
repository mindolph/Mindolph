package com.mindolph.mindmap.drawing;

import com.mindolph.mfx.drawing.Context;
import com.mindolph.mfx.drawing.Graphics;
import com.mindolph.mfx.drawing.component.Component;
import com.mindolph.mfx.drawing.constant.StrokeType;
import com.mindolph.mfx.util.GeometryConvertUtils;
import com.mindolph.mindmap.theme.BorderType;
import com.mindolph.mindmap.theme.MindMapTheme;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public abstract class BaseTopicComponent extends Component {
    public BaseTopicComponent() {
    }

    public BaseTopicComponent(Rectangle2D bounds) {
        super(bounds);
    }

    public BaseTopicComponent(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    public void draw(Graphics g, Context context) {
        super.draw(g, context);
        MindMapDrawingContext c = (MindMapDrawingContext) context;
        MindMapTheme theme = c.getTheme();
        BorderType borderType = getBorderType(theme);
        if (BorderType.BOX == borderType) {
            if (theme.isDropShadow()) {
                double offset = c.safeScale(theme.getShadowOffset(), 0.0f);
                Rectangle rect = new Rectangle(super.getMinX() + offset, super.getMinY() + offset, super.getWidth(), super.getHeight());
                if (theme.getRoundRadius() > 0) {
                    double round = c.safeScale(getRoundRadius(theme), 0f);
                    rect.setArcWidth(round);
                    rect.setArcHeight(round);
                }
                g.draw(rect, null, theme.getShadowColor());
            }
            Rectangle rect = GeometryConvertUtils.rectangle2D2Rectangle(super.getBounds());
            if (theme.getRoundRadius() > 0) {
                double round = c.safeScale(getRoundRadius(theme), 0f);
                rect.setArcWidth(round);
                rect.setArcHeight(round);
            }
            g.draw(rect, theme.getElementBorderColor(), getBackgroundColor(theme));
        }
        else if (BorderType.LINE == borderType) {
            // draw line under text
            g.setStroke((float) c.safeScale(theme.getConnectorWidth(), 0.1f), StrokeType.SOLID);
            Rectangle2D b = super.getAbsoluteBounds();
            g.drawLine(b.getMinX(), b.getMinY() + b.getHeight(), b.getMinX() + b.getWidth(), b.getMinY() + b.getHeight(), theme.getConnectorColor());
        }
    }

    protected abstract BorderType getBorderType(MindMapTheme theme);

    protected abstract Color getBackgroundColor(MindMapTheme theme);

    protected abstract float getRoundRadius(MindMapTheme theme);
}
