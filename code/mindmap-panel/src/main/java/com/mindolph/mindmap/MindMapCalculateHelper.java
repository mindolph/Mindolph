package com.mindolph.mindmap;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.graphic.CanvasGraphicsWrapper;
import com.mindolph.mindmap.gfx.MindMapCanvas;
import com.mindolph.mindmap.model.BaseElement;
import com.mindolph.mindmap.model.TopicNode;
import javafx.geometry.Dimension2D;
import javafx.scene.canvas.Canvas;

/**
 * @author mindolph.com@gmail.com
 */
public class MindMapCalculateHelper {


    /**
     * TODO to be refactored with SVGImageExporter refactoring.
     *
     * @param model
     * @param cfg
     * @param context
     * @param expandAll
     * @return
     */
    public static Dimension2D calculateSizeOfMapInPixels(MindMap<TopicNode> model, MindMapConfig cfg, MindMapContext context, boolean expandAll) {
        MindMap<TopicNode> workMap = new MindMap<>(model);
        workMap.resetPayload();
        Canvas newCanvas = new Canvas();
        newCanvas.setWidth(32);
        newCanvas.setHeight(32);
        CanvasGraphicsWrapper gfx = new CanvasGraphicsWrapper(newCanvas);
        MindMapCanvas mmCanvas = new MindMapCanvas(gfx,cfg, context);
        Dimension2D blockSize = null;
        try {
            if (mmCanvas.calculateElementSizes(workMap)) {
                if (expandAll) {
                    BaseElement root = (BaseElement) workMap.getRoot().getPayload();
                    root.collapseOrExpandAllChildren(false);
                    mmCanvas.calculateElementSizes(workMap);
                }
                blockSize = mmCanvas.layoutModelElements(workMap);
                double paperMargin = cfg.getTheme().getPaperMargins() * context.getScale();
                blockSize = new Dimension2D(blockSize.getWidth() + paperMargin * 2, blockSize.getHeight() + paperMargin * 2);
            }
        } finally {
            gfx.dispose();
        }
        return blockSize;
    }
}
