package com.mindolph.base.util;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * @author mindolph.com@gmail.com
 */
public class RegionUtils {

    /**
     *
     * @param region
     */
    public static void applyDragDropBorder(Region region) {
        region.setBorder(new Border(new BorderStroke(Color.DARKBLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

}
