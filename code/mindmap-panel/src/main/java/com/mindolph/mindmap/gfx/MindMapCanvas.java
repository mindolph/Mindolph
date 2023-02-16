package com.mindolph.mindmap.gfx;

import com.igormaznitsa.mindmap.model.*;
import com.mindolph.base.constant.StrokeType;
import com.mindolph.base.graphic.CanvasGraphicsWrapper;
import com.mindolph.base.graphic.Graphics;
import com.mindolph.base.util.GeometryConvertUtils;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.MindMapCalculateHelper;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import com.mindolph.mindmap.constant.MindMapConstants;
import com.mindolph.mindmap.model.*;
import com.mindolph.mindmap.util.DiagramUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.mindolph.mfx.util.RectangleUtils.*;

/**
 * @author mindolph.com@gmail.com
 */
public class MindMapCanvas {

    private static final Logger log = LoggerFactory.getLogger(MindMapCanvas.class);

    private final Graphics g;
    private final MindMapConfig config;
    private final MindMapContext mindMapContext;

    public MindMapCanvas(Graphics g, MindMapConfig config, MindMapContext context) {
        this.g = g;
        this.config = config;
        this.mindMapContext = context;
    }

    private void setElementSizesForElementAndChildren(TopicNode topic, int level) {
        BaseElement element = (BaseElement) topic.getPayload();
        if (element == null) {
            switch (level) {
                case 0:
                    element = new ElementRoot(topic, g, config, mindMapContext);
                    break;
                case 1:
                    element = new ElementLevelFirst(topic, g, config, mindMapContext);
                    break;
                default:
                    element = new ElementLevelOther(topic, g, config, mindMapContext);
                    break;
            }
            topic.setPayload(element);
        }

        element.updateElementBounds();
        for (TopicNode t : topic.getChildren()) {
            setElementSizesForElementAndChildren(t, level + 1);
        }
        element.updateBlockSize();
    }

    public boolean calculateElementSizes(MindMap<TopicNode> model) {
        boolean result = false;
        TopicNode root = model == null ? null : model.getRoot();
        if (root != null && model != null) {
            model.resetPayload();
            setElementSizesForElementAndChildren(root, 0);
            result = true;
        }
        return result;
    }

    public Dimension2D layoutModelElements(MindMap<TopicNode> model) {
        Dimension2D result = null;
        if (model != null) {
            TopicNode rootTopic = model.getRoot();
            if (rootTopic != null) {
                BaseElement root = (BaseElement) rootTopic.getPayload();
                if (root != null) {
                    root.alignElementAndChildren(true, 0, 0);
                    result = root.getBlockSize();
                }
            }
        }
        return result;
    }

    public void drawOnGraphicsForConfiguration(MindMap<TopicNode> map, boolean drawSelection, List<TopicNode> selectedTopics) {
        drawBackground();
        drawTopics(map, null);
        if (drawSelection && CollectionUtils.isNotEmpty(selectedTopics)) {
            drawSelection(selectedTopics);
        }
    }

    public void drawBackground() {
        Rectangle2D clipBounds = g.getClipBounds();
        if (config.isDrawBackground()) {
            if (clipBounds == null) {
                log.warn("Can't draw background because clip bounds is not provided!");
            }
            else {
                if (log.isTraceEnabled())
                    log.trace(String.format("Draw background within %s", rectangleInStr(clipBounds)));
                double beginX = clipBounds.getMinX() < 0 ? clipBounds.getMinX() : 0;
                double beginY = clipBounds.getMinY() < 0 ? clipBounds.getMinY() : 0;
                double totalWidth = (clipBounds.getWidth() + (clipBounds.getMinX() < 0 ? 0 : clipBounds.getMinX()));
                double totalHeight = (clipBounds.getHeight() + (clipBounds.getMinY() < 0 ? 0 : clipBounds.getMinY()));
                g.drawRect(beginX, beginY, totalWidth, totalHeight, null, config.getPaperColor());
                //g.drawRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height, null, cfg.getPaperColor());

                g.setStroke(1, StrokeType.SOLID);
                if (config.isShowGrid()) {
                    double scaledGridStep = config.getGridStep() * mindMapContext.getScale();

                    double minX = clipBounds.getMinX();
                    double minY = clipBounds.getMinY();
                    double maxX = clipBounds.getMinX() + clipBounds.getWidth();
                    double maxY = clipBounds.getMinY() + clipBounds.getHeight();

                    Color gridColor = config.getGridColor();

                    for (double x = beginX; x < maxX; x += scaledGridStep) {
                        if (x < minX) {
                            continue;
                        }
                        g.drawLine(x, minY, x, maxY, gridColor);
                    }

                    for (double y = beginY; y < maxY; y += scaledGridStep) {
                        if (y < minY) {
                            continue;
                        }
                        g.drawLine(minX, y, maxX, y, gridColor);
                    }
                }
            }
        }
    }

    /**
     * @param map
     * @param drawSelection
     * @param selectedTopics
     * @param collapsingTopic to determine whether to draw collapsator.
     */
    public void drawGraphics(MindMap<TopicNode> map,
                             boolean drawSelection, List<TopicNode> selectedTopics, TopicNode collapsingTopic) {
        drawTopics(map, collapsingTopic);
        if (drawSelection && CollectionUtils.isNotEmpty(selectedTopics)) {
            drawSelection(selectedTopics);
        }
    }

    private void drawTopics(MindMap<TopicNode> map, TopicNode collapsingTopic) {
        if (map != null) {
            if (log.isTraceEnabled()) log.trace("Draw topics");
            if (Boolean.parseBoolean(map.getAttribute(MindMapConstants.MODEL_ATTR_SHOW_JUMPS))) {
                drawJumps(map);
            }

            TopicNode root = map.getRoot();
            if (root != null) {
                drawTopicTree(root, collapsingTopic);
            }
        }
    }

    private void drawTopicTree(TopicNode topic, TopicNode collapsingTopic) {
        paintTopic(topic, collapsingTopic);
        BaseElement w = (BaseElement) topic.getPayload();
        if (w != null) {
            if (w.isCollapsed()) {
                return;
            }
            for (TopicNode t : topic.getChildren()) {
                drawTopicTree(t, collapsingTopic);
            }
        }
    }

    private void paintTopic(TopicNode topic, TopicNode collapsingTopic) {
        BaseElement element = (BaseElement) topic.getPayload();
        if (element != null) {
            element.doPaint(!config.isShowCollapsatorOnMouseHover() || topic == collapsingTopic);
        }
    }

    private void drawSelection(List<TopicNode> selectedTopics) {
        if (selectedTopics != null && !selectedTopics.isEmpty()) {
            if (log.isTraceEnabled()) log.trace("Draw selection");
            Color selectLineColor = config.getSelectLineColor();
            g.setStroke(mindMapContext.safeScale(config.getSelectLineWidth(), 0.1f), StrokeType.DASHES);
            double selectLineGap = mindMapContext.safeScale(config.getSelectLineGap(), 0.05f);
            double selectLineGapX2 = selectLineGap + selectLineGap;

            for (TopicNode s : selectedTopics) {
                BaseElement e = (BaseElement) s.getPayload();
                if (e != null) {
                    double x = Math.round(e.getBounds().getMinX() - selectLineGap);
                    double y = Math.round(e.getBounds().getMinY() - selectLineGap);
                    double w = Math.round(e.getBounds().getWidth() + selectLineGapX2);
                    double h = Math.round(e.getBounds().getHeight() + selectLineGapX2);
                    g.drawRect(x, y, w, h, selectLineColor, null);
                }
            }
            // force resetting stroke or the background drawing will be affected.
            g.setStroke(mindMapContext.safeScale(config.getElementBorderWidth(), 0.1f), StrokeType.SOLID);
        }
    }


    private double findLineAngle(double sx, double sy, double ex, double ey) {
        double deltax = ex - sx;
        if (deltax == 0.0d) {
            return Math.PI / 2;
        }
        return Math.atan((ey - sy) / deltax) + (ex < sx ? Math.PI : 0);
    }

    private void drawJumps(MindMap<TopicNode> map) {
        List<TopicNode> allTopicsWithJumps = map.findAllTopicsForExtraType(Extra.ExtraType.TOPIC);
        float lineWidth = mindMapContext.safeScale(config.getJumpLinkWidth(), 0.1f);
        float arrowWidth = mindMapContext.safeScale(config.getJumpLinkWidth(), 0.3f);
        Color jumpLinkColor = config.getJumpLinkColor();
        float arrowSize = mindMapContext.safeScale(10.0f * config.getJumpLinkWidth(), 0.2f);
        for (TopicNode src : allTopicsWithJumps) {
            ExtraTopic extra = (ExtraTopic) src.getExtras().get(Extra.ExtraType.TOPIC);
            src = src.isHidden() ? src.findFirstVisibleAncestor() : src;
            if (extra != null) {
                TopicNode dst = map.findTopicForLink(extra);
                if (dst != null) {
                    if (dst.isHidden()) {
                        dst = dst.findFirstVisibleAncestor();
                        if (dst == src) {
                            dst = null;
                        }
                    }

                    if (dst != null) {
                        BaseElement dstElement = (BaseElement) dst.getPayload();
                        if (!dst.isHidden() && dstElement != null) {
                            BaseElement srcElement = (BaseElement) src.getPayload();
                            Rectangle2D srcRect = srcElement.getBounds();
                            Rectangle2D dstRect = dstElement.getBounds();
                            drawArrowToDestination(srcRect, dstRect, lineWidth, arrowSize, jumpLinkColor);
                        }
                    }
                }
            }
        }
    }

    private void drawArrowToDestination(Rectangle2D start, Rectangle2D destination,
                                        float lineWidth, float arrowSize, Color color) {

        double startx = centerX(start);
        double starty = centerY(start);

        Point2D arrowPoint = DiagramUtils.findRectEdgeIntersection(destination, startx, starty);

        if (arrowPoint != null) {
            g.setStroke(lineWidth, StrokeType.SOLID);

            double angle = findLineAngle(arrowPoint.getX(), arrowPoint.getY(), startx, starty);

            double arrowAngle = Math.PI / 12.0d; // 30 degrees

            double x1 = arrowSize * Math.cos(angle - arrowAngle);
            double y1 = arrowSize * Math.sin(angle - arrowAngle);
            double x2 = arrowSize * Math.cos(angle + arrowAngle);
            double y2 = arrowSize * Math.sin(angle + arrowAngle);

            double cx = (arrowSize / 2.0f) * Math.cos(angle);
            double cy = (arrowSize / 2.0f) * Math.sin(angle);

            Path polygon = new Path();
            polygon.getElements().add(new MoveTo(arrowPoint.getX(), arrowPoint.getY()));
            polygon.getElements().add(new LineTo(arrowPoint.getX() + x1, arrowPoint.getY() + y1));
            polygon.getElements().add(new LineTo(arrowPoint.getX() + x2, arrowPoint.getY() + y2));
            g.draw(polygon, null, color);

            g.setStroke(lineWidth, StrokeType.DOTS);
            g.drawLine(startx, starty, (arrowPoint.getX() + cx), (arrowPoint.getY() + cy), color);
        }
    }


    static void drawErrorText(GraphicsContext gc, Bounds fullSize, String error) {
        Font font = new Font("Serif", 24);
        Text text = new Text(error);
        text.setFont(font);
        gc.setFont(font);
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, 0, fullSize.getWidth(), fullSize.getHeight());
        double x = (fullSize.getWidth() - text.getLayoutBounds().getWidth()) / 2;
        double y = (fullSize.getHeight() - text.getLayoutBounds().getHeight()) / 2;
        gc.setStroke(Color.BLACK);
        gc.strokeText(error, x + 5, y + 5);
        gc.setStroke(Color.RED);
        gc.strokeText(error, x, y);
    }

    protected void moveDiagram(MindMap<TopicNode> model, double deltaX, double deltaY) {
        if (model != null) {
            TopicNode root = model.getRoot();
            if (root != null) {
                BaseElement element = (BaseElement) root.getPayload();
                if (element != null) {
                    element.moveWholeTreeBranchCoordinates(deltaX, deltaY);
                }
            }
        }
    }

    /**
     * @param model
     * @param paperSize
     * @return
     */
    public Dimension2D layoutFullDiagramWithCenteringToPaper(MindMap<TopicNode> model, Bounds paperSize) {
        Dimension2D resultSize = null;
        if (calculateElementSizes(model)) {
            Dimension2D rootBlockSize = layoutModelElements(model);
            double paperMargin = config.getPaperMargins() * mindMapContext.getScale();

            if (rootBlockSize != null) {
                ElementRoot rootElement = (ElementRoot) model.getRoot().getPayload();

                double rootOffsetXInBlock = rootElement.getLeftBlockSize().getWidth();
                double rootOffsetYInBlock = (rootBlockSize.getHeight() - rootElement.getBounds().getHeight()) / 2;

                rootOffsetXInBlock += paperMargin;
                rootOffsetYInBlock += paperMargin;

                // extend the map in model?
                moveDiagram(model, rootOffsetXInBlock, rootOffsetYInBlock);
                resultSize = new Dimension2D(Math.round(rootBlockSize.getWidth() + paperMargin * 2), Math.round(rootBlockSize.getHeight() + paperMargin * 2));
            }
        }
        return resultSize;
    }

    public static WritableImage renderMindMapAsImage(MindMap<TopicNode> model, MindMapConfig config, boolean expandAll) {
        MindMap<TopicNode> workMap = new MindMap<>(model);
        workMap.resetPayload();

        if (expandAll) {
            workMap.getRoot().removeCollapseAttr();
        }
        MindMapContext context = new MindMapContext();
        Dimension2D blockSize = MindMapCalculateHelper.calculateSizeOfMapInPixels(workMap, config, context, expandAll);
        if (blockSize == null) {
            return null;
        }

        Graphics gfx = CanvasGraphicsWrapper.create(blockSize.getWidth(), blockSize.getHeight());
        MindMapCanvas mmCanvas = new MindMapCanvas(gfx, config, new MindMapContext());
        try {
            gfx.setClip(0, 0, blockSize.getWidth(), blockSize.getHeight());
            mmCanvas.layoutFullDiagramWithCenteringToPaper(workMap, GeometryConvertUtils.dimension2DToBounds(blockSize));
            mmCanvas.drawOnGraphicsForConfiguration(workMap, false, null);
        } finally {
            gfx.dispose();
        }
        return ((CanvasGraphicsWrapper) gfx).getCanvas().snapshot(null, null);
    }

    static String makeHtmlTooltipForExtra(MindMap<TopicNode> model, Extra<?> extra) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html>");
        switch (extra.getType()) {
            case FILE: {
                ExtraFile efile = (ExtraFile) extra;
                String line = efile.getAsURI().getParameters().getProperty("line", null);
                if (line != null && !line.equals("0")) {
                    builder.append(String.format(I18n.getIns().getString("MindMapPanel.tooltipOpenFileWithLine"), StringEscapeUtils.escapeHtml3(efile.getAsString()), StringEscapeUtils.escapeHtml3(line)));
                }
                else {
                    builder.append(I18n.getIns().getString("MindMapPanel.tooltipOpenFile")).append(StringEscapeUtils.escapeHtml3(efile.getAsString()));
                }
            }
            break;
            case TOPIC: {
                TopicNode topic = model.findTopicForLink((ExtraTopic) extra);
                builder.append(I18n.getIns().getString("MindMapPanel.tooltipJumpToTopic")).append(StringEscapeUtils.escapeHtml3(ModelUtils.makeShortTextVersion(topic == null ? "----" : topic.getText(), 32)));
            }
            break;
            case LINK: {
                builder.append(I18n.getIns().getString("MindMapPanel.tooltipOpenLink")).append(StringEscapeUtils.escapeHtml3(ModelUtils.makeShortTextVersion(extra.getAsString(), 48)));
            }
            break;
            case NOTE: {
                ExtraNote extraNote = (ExtraNote) extra;
                if (extraNote.isEncrypted()) {
                    builder.append(I18n.getIns().getString("MindMapPanel.tooltipOpenText")).append("#######");
                }
                else {
                    builder.append(I18n.getIns().getString("MindMapPanel.tooltipOpenText")).append(StringEscapeUtils
                            .escapeHtml3(ModelUtils.makeShortTextVersion(extra.getAsString(), 64)));
                }
            }
            break;
            default: {
                builder.append("<b>Unknown</b>");
            }
            break;
        }
        builder.append("</html>");
        return builder.toString();
    }
}
