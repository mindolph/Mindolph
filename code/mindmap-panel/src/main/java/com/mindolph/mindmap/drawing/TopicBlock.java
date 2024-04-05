package com.mindolph.mindmap.drawing;

import com.mindolph.mfx.drawing.Context;
import com.mindolph.mfx.drawing.Graphics;
import com.mindolph.mfx.drawing.component.Component;
import javafx.geometry.Rectangle2D;

public class TopicBlock extends Component {

    public TopicBlock() {
    }

    public TopicBlock(Rectangle2D bounds) {
        super(bounds);
    }

    public TopicBlock(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    public void draw(Graphics g, Context context) {
        super.draw(g, context);
    }
}
