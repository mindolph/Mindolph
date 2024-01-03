package com.mindolph.base.util;

import javafx.scene.control.IndexedCell;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.VirtualFlow;

/**
 * @author mindolph.com@gmail.com
 */
public class SkinUtils {

    public static IndexedCell<?> getWidestVisibleCell(SkinBase<?> skin) {
        VirtualFlow<?> flow = getVirtualFlow(skin);
        IndexedCell<?> longestCell = null;
        for (int i = 0; i < flow.getCellCount(); i++) {
            IndexedCell<?> cell = flow.getCell(i);
            if (longestCell == null || cell.getWidth() > longestCell.getWidth()) {
                longestCell = cell;
            }
        }
        return longestCell;
    }


    public static IndexedCell<?> getFirstVisibleCell(SkinBase<?> skin) {
        return getVirtualFlow(skin).getFirstVisibleCell();
    }

    public static VirtualFlow<?> getVirtualFlow(SkinBase<?> skin) {
        return (VirtualFlow<?>) skin.getChildren().get(0);
    }
}
