package com.mindolph.fx.print;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.util.GeometryConvertUtils;
import com.mindolph.mindmap.MindMapContext;
import com.mindolph.mindmap.gfx.MindMapCanvas;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.base.graphic.CanvasGraphicsWrapper;
import com.mindolph.mfx.util.DimensionUtils;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.theme.MindMapTheme;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.print.PageLayout;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author mindolph.com@gmail.com
 */
public class MindMapPrintable extends BasePrintable {

    private static final Logger log = LoggerFactory.getLogger(MindMapPrintable.class);

    private final MindMap<TopicNode> model;
    private final MindMapConfig cfg;
    private Dimension2D diagramDimension;
    private final MindMapCanvas mmCanvas ;
    private final MindMapContext mindMapContext;

    public MindMapPrintable(MindMap<TopicNode> model, PageLayout pageLayout) {
        super.pageLayout = pageLayout;
        this.model = model;
        this.mindMapContext = new MindMapContext();

        cfg = new MindMapConfig();
        MindMapTheme theme = cfg.getTheme();
        theme.setDrawBackground(false);
        theme.setDropShadow(false);

        theme.setConnectorColor(Color.BLACK);
        theme.setRootBackgroundColor(Color.BLACK);
        theme.setRootTextColor(Color.WHITE);
        theme.setFirstLevelBackgroundColor(Color.LIGHTGRAY);
        theme.setFirstLevelTextColor(Color.BLACK);
        theme.setOtherLevelBackgroundColor(Color.WHITE);
        theme.setOtherLevelTextColor(Color.BLACK);
        theme.setCollapsatorBorderColor(Color.BLACK);
        theme.setCollapsatorBackgroundColor(Color.WHITE);
        theme.setJumpLinkColor(Color.DARKGRAY);

        theme.setElementBorderWidth(1.5f);
        theme.setCollapsatorBorderWidth(1.0f);
        theme.setConnectorWidth(2.0f);
        theme.setPaperMargins(2);

        mmCanvas = new MindMapCanvas(new CanvasGraphicsWrapper(new Canvas()), cfg, mindMapContext);

        this.update(pageLayout, new PrintOptions());
    }

    @Override
    public void update(PageLayout pageLayout, PrintOptions printOptions) {
        super.update(pageLayout, printOptions);
        mindMapContext.setScale(super.actualScale);
        diagramDimension = mmCanvas.layoutFullDiagramWithCenteringToPaper(model, null);
        log.debug("mind map dimension: %s".formatted(DimensionUtils.dimensionInStr(diagramDimension)));
        pagesH = (int) (this.diagramDimension.getWidth() / printableWidth + 1);
        pagesV = (int) (this.diagramDimension.getHeight() / printableHeight + 1);
        initPages();
    }

    protected void initPages() {
        pages = new PrintPage[pagesH][pagesV];
        for (int i = 0; i < pagesH; i++) {
            for (int j = 0; j < pagesV; j++) {
                int h = i;
                int v = j;
                pages[i][j] = new PrintPage() {
                    private Canvas pageCanvas;

                    @Override
                    public void print(GraphicsContext gc) {
                        double drawX = h * printableWidth;
                        double drawY = v * printableHeight;
                        double drawWidth = (drawX + printableWidth) < diagramDimension.getWidth() ? printableWidth : diagramDimension.getWidth() - drawX;
                        double drawHeight = (drawY + printableHeight) < diagramDimension.getHeight() ? printableHeight : diagramDimension.getHeight() - drawY;
                        // Draw page area in new canvas(dimension limited) TODO should be optimized for performance.
                        pageCanvas = new Canvas(drawWidth, drawHeight);
                        CanvasGraphicsWrapper pageGc = new CanvasGraphicsWrapper(pageCanvas);
                        pageGc.translate(-drawX, -drawY); // translate to current page
                        pageGc.setClipBounds(new Rectangle2D(drawX, drawY, drawWidth, drawHeight)); // set drawing area in mind map
                        MindMapCanvas mmc = new MindMapCanvas(pageGc, cfg, mindMapContext);
                        mmc.layoutFullDiagramWithCenteringToPaper(model, GeometryConvertUtils.rectangle2Bounds(pageGc.getClipBounds()));
                        mmc.drawOnGraphicsForConfiguration(model,false, null);
                        SnapshotParameters params = new SnapshotParameters();
                        WritableImage snapshot = pageGc.getCanvas().snapshot(params, null);
                        // Draw the content from the page canvas to the main canvas.
                        gc.drawImage(snapshot, 0, 0, drawWidth, drawHeight,
                                0, 0, drawWidth, drawHeight);
                    }

                    @Override
                    public Node getPageCanvas() {
                        return pageCanvas;
                    }
                };
            }
        }
    }

    @Override
    protected double getWidth() {
        return diagramDimension.getWidth();
    }

    @Override
    protected double getHeight() {
        return diagramDimension.getHeight();
    }

    @Override
    public Dimension2D getDimension() {
        return diagramDimension;
    }

}
