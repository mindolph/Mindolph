package com.mindolph.mindmap;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.control.BaseScalableViewSkin;
import com.mindolph.base.graphic.CanvasGraphicsWrapper;
import com.mindolph.mfx.util.ColorUtils;
import com.mindolph.mfx.util.DimensionUtils;
import com.mindolph.mfx.util.KeyEventUtils;
import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.event.TopicEditEventHandler;
import com.mindolph.mindmap.gfx.MindMapCanvas;
import com.mindolph.mindmap.model.BaseElement;
import com.mindolph.mindmap.model.DraggedElement;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.ElementUtils;
import com.mindolph.mindmap.util.TextUtils;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mindolph.mindmap.constant.MindMapConstants.*;
import static javafx.scene.input.KeyCode.*;

/**
 * @author mindolph.com@gmail.com
 * @see MindMapView
 */
public class MindMapViewSkin<T extends MindMapView> extends BaseScalableViewSkin<T> implements TopicEditEventHandler {

    private static final Logger log = LoggerFactory.getLogger(MindMapViewSkin.class);

    private final StackPane stackPane; // container of text area.
    private final TextArea textArea;
    // The modifier key state for controlling the '\n' input.
    private boolean isModifierKeyDown = false;

    // used to calculate the position of topic editor and limit the topic editor's min bounds after editing stated.
    private Rectangle2D originalEditingBounds;

    private MindMapCanvas mindMapCanvas;

    private CanvasGraphicsWrapper graphicsWrapper;

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    protected MindMapViewSkin(T control) {
        super(control);
        this.graphicsWrapper = new CanvasGraphicsWrapper(canvas);
        this.mindMapCanvas = new MindMapCanvas(graphicsWrapper, control.getConfig(), getSkinnable().mindMapContext);
        this.stackPane = new StackPane();
        this.stackPane.setManaged(false);
        this.textArea = new TextArea();
        this.textArea.setVisible(false);
        this.stackPane.getChildren().addAll(textArea);
        this.getChildren().add(stackPane);
        this.textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!StringUtils.equals(oldValue, newValue)) {
                log.trace(String.format("Text changed from %d to %d (size)", oldValue.length(), newValue.length()));
                Platform.runLater(() -> { // if not run in this, the display will different, why?
                    Dimension2D textDim = calTextDimensionWithPaddingAndLimitation(textArea);
                    double width = textDim.getWidth();
                    double height = textDim.getHeight();
                    log.trace(String.format("Set topic editor dimension to: %s", DimensionUtils.dimensionInStr(textDim)));
                    textArea.setPrefSize(width, height);
                    textArea.setMinSize(width, height);
                    log.debug("Text area dimension: %s".formatted(DimensionUtils.dimensionInStr(textDim)));
                    this.relocateTextArea(textDim);
                });
            }
        });
        // Handle keyboard events.
        textArea.setWrapText(false);
        textArea.setOnKeyPressed(e -> {
            e.consume();
            if (KeyEventUtils.isModifierKeyDown(e)) {
                isModifierKeyDown = true;
                if (e.getCode() == ENTER && e.isShiftDown()) {
                    log.debug("Insert new line to topic");
                    textArea.insertText(textArea.getCaretPosition(), "\n");
                }
            }
            else {
                isModifierKeyDown = false;
                if (e.getCode() == ENTER) {
                    originalEditingBounds = null;
                    control.endEdit(textArea.getText(), true);
                    Platform.runLater(() -> {
                        textArea.setVisible(false);
                        //requestFocus();
                    });
                }
                else if (e.getCode() == TAB) {
                    originalEditingBounds = null; // reset for calculating the new text editor size.
                    Dimension2D dim = calTextDimensionWithPaddingAndLimitation(textArea);
                    control.onStartNewTopicEdit(textArea.getText(), new Dimension2D(dim.getWidth(), dim.getHeight()));
                }
            }
        });
        textArea.setOnKeyReleased(e -> {
            e.consume();
            if (e.getCode() == ESCAPE) {
                originalEditingBounds = null;
                control.onEditCanceled();
                Platform.runLater(() -> {
                    textArea.setVisible(false);
                    //requestFocus();
                });
            }
            isModifierKeyDown = false;
        });
        // disable ENTER and TAB key inputs.
        textArea.setTextFormatter(new TextFormatter<>(change -> {
            // replace TAB and ENTER key press.
            if (!isModifierKeyDown) {
                if ("\t".equals(change.getText()) || "\n".equals(change.getText())) {
                    change.setText(textArea.getSelectedText());//empty string if no selection.
                }
            }
            return change;
        }));

        // bind input selection text to control.
        this.control.selectedInputTextProperty().bind(textArea.selectedTextProperty());

        // listen the topic edit event
        this.control.setTopicEditEventHandler(this);
        log.info("MindMapViewSkin constructed.");
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        if (log.isTraceEnabled())
            log.trace("layoutChildren() for bounds: (%.2f, %.2f) (%.2f x %.2f)".formatted(contentX, contentY, contentWidth, contentHeight));
    }

    public void calculateAndSetOriginalDimension() {
        Dimension2D dimension2D = this.updateElementsAndSizeForGraphics(true);
        log.debug("Original dimension set to %s".formatted(DimensionUtils.dimensionInStr(dimension2D)));
        control.setOriginalDimension(dimension2D);
    }

    @Override
    public void reLayout(double newScale) {
        if (log.isTraceEnabled()) log.trace("reLayout()");
        updateElementsAndSizeForGraphics(true);
        control.fitViewport();
    }

    @Override
    public void redraw() {
        super.redraw();
    }

    public Dimension2D updateElementsAndSizeForGraphics(boolean forceLayout) {
        MindMap<TopicNode> model = this.control.getModel();
        if (forceLayout || !this.control.isModelValid()) {
            if (canvas != null) {
                if (log.isTraceEnabled()) log.trace("updateElementsAndSizeForGraphics()");
                //if (log.isTraceEnabled())log.trace("viewportBounds: " + RectangleUtils.rectangleInStr(vr));
                Dimension2D diagramDimension = mindMapCanvas.layoutFullDiagramWithCenteringToPaper(model, null);
                if (diagramDimension != null) {
                    if (log.isTraceEnabled())
                        log.trace(String.format("Calculated dimension for diagram : %s", DimensionUtils.dimensionInStr(diagramDimension)));
                    this.control.setPrefSize(diagramDimension.getWidth(), diagramDimension.getHeight());
                    // if without this line, the calculation of auto scroll will repeat for original size and new size.
                    this.control.getParent().layout();
                    this.control.setDimension(diagramDimension);
                }
                return diagramDimension;
            }
        }
        else {
            log.debug("Ignore calc size of mind map");
        }
        return null;
    }

    @Override
    protected void drawBackground() {
        super.drawBackground();
        graphicsWrapper.setClipBounds(this.control.getViewportRectangle());
        super.translateGraphicsContext(false);
        mindMapCanvas.drawBackground();
        super.translateGraphicsContext(true);
    }

    @Override
    protected void drawContent() {
        super.translateGraphicsContext(false);
        if (log.isTraceEnabled()) log.trace("Draw content");
        graphicsWrapper.setClipBounds(this.control.getViewportRectangle());
        mindMapCanvas.drawGraphics(this.control.getModel(),
                true, this.control.getSelectedTopics(), this.control.getCollapsingTopic());
        drawDestinationElement(canvas.getGraphicsContext2D(), this.control.getConfig());

        // TODO  refactor to be more elegant.
        if (this.control.getDraggedElement() != null) {
            if (log.isTraceEnabled()) log.trace("Draw dragged element");
            this.control.getDraggedElement().draw();
        }
        else if (this.control.getMouseDragSelection() != null) {
            if (log.isTraceEnabled()) log.trace("Draw mouse drag selection");
            Rectangle2D r = this.control.getMouseDragSelection().asRectangle();
            graphicsWrapper.drawRect(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), null, COLOR_MOUSE_DRAG_SELECTION);
        }
        super.translateGraphicsContext(true);
    }

    private void drawDestinationElement(GraphicsContext gc, MindMapConfig cfg) {
        BaseElement destinationElement = this.control.getDestinationElement();
        DraggedElement draggedElement = this.control.getDraggedElement();
        if (destinationElement != null && draggedElement != null) {
            if (log.isTraceEnabled()) log.trace("Draw destination element: " + destinationElement.getText());
            Color color = ColorUtils.colorWithOpacity(Color.ORANGE, 0.5);
            gc.setFill(color);
            gc.setStroke(color);
            gc.setLineWidth(getSkinnable().mindMapContext.safeScale(3.0f, 0.1f));

//            rectToDraw.setRect(destinationElement.getBounds());

            Rectangle2D eBounds = destinationElement.getBounds();
            double selectLineGap = cfg.getTheme().getSelectLineGap() * 3.0d * getSkinnable().mindMapContext.getScale();
            // calculate extended rectangle
            Rectangle2D r = new Rectangle2D(eBounds.getMinX() - selectLineGap, eBounds.getMinY() - selectLineGap,
                    eBounds.getWidth() + selectLineGap * 2, eBounds.getHeight() + selectLineGap * 2);

            int position = ElementUtils.calcDropPosition(destinationElement, draggedElement.getPosition());

            boolean draw = !draggedElement.isPositionInside() && !destinationElement.getModel().isAncestor(draggedElement.getElement().getModel());

            if (log.isTraceEnabled())
                log.trace(String.format("dragged modifier and position: %s %d", draggedElement.getModifier(), position));
            switch (draggedElement.getModifier()) {
                case NONE: {
                    switch (position) {
                        case DRAG_POSITION_TOP -> {
                            r = new Rectangle2D(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight() / 2);
                        }
                        case DRAG_POSITION_BOTTOM -> {
                            r = new Rectangle2D(r.getMinX(), r.getMinY() + r.getHeight() / 2, r.getWidth(), r.getHeight() / 2);
                        }
                        case DRAG_POSITION_LEFT -> {
                            r = new Rectangle2D(r.getMinX(), r.getMinY(), r.getWidth() / 2, r.getHeight());
                        }
                        case DRAG_POSITION_RIGHT -> {
                            r = new Rectangle2D(r.getMinX() + r.getWidth() / 2, r.getMinY(), r.getWidth() / 2, r.getHeight());
                        }
                        default -> draw = false;
                    }
                }
                break;
                case MAKE_JUMP: {
                }
                break;
                default:
                    throw new Error("Unexpected state " + draggedElement.getModifier());
            }

            if (draw) {
                gc.fillRect(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
            }
            gc.setLineWidth(getSkinnable().mindMapContext.safeScale(cfg.getTheme().getElementBorderWidth(), 0.1f));
        }
    }

    /**
     * @param text
     * @param font
     * @param topicBounds this bounds come with viewport padding.
     */
    @Override
    public void startEdit(String text, Font font, Rectangle2D topicBounds) {
        log.info("Start edit");
        originalEditingBounds = topicBounds;
        Platform.runLater(() -> {
            log.debug("Start edit topic at bounds: %s".formatted(RectangleUtils.rectangleInStr(topicBounds)));
            textArea.setText(text);
            textArea.setFont(font);
            textArea.setVisible(true); // set visible before request focus.
            textArea.positionCaret(text.length());
            // refresh bounds by new text area, not topic
            Dimension2D textDim = this.calTextDimensionWithPaddingAndLimitation(textArea);
            textArea.setMinSize(textDim.getWidth(), textDim.getHeight());
            textArea.setPrefSize(textDim.getWidth(), textDim.getHeight());
            log.debug("Text area dimension: %s".formatted(DimensionUtils.dimensionInStr(textDim)));
            this.relocateTextArea(textDim);
            textArea.requestFocus(); // to obtain input focus after creating new topic by clicking key TAB at an editing topic.
            log.info("text editor focused: (%s, %s), (%.1f x %.1f)".formatted(topicBounds.getMinX(), topicBounds.getMinY(), textArea.getPrefWidth(), textArea.getPrefHeight()));
        });
    }

    /**
     * Relocate text area by new text dimension which is calculated.
     *
     * @param textDim
     */
    private void relocateTextArea(Dimension2D textDim) {
        Rectangle2D vr = this.control.getViewportRectangle();
        if (originalEditingBounds == null) {
            log.debug("No original bounds for topic editor, ignore locating");
            return;
        }
        log.trace("originalEditingBounds: %s".formatted(RectangleUtils.rectangleInStr(originalEditingBounds)));
        // subtract the offset only when width/height is exceeds the viewport
        double originLeftInViewport = originalEditingBounds.getMinX() - (getSkinnable().isWidthOverViewport() ? vr.getMinX() : 0);
        double originTopInViewport = originalEditingBounds.getMinY() - (getSkinnable().isHeightOverViewport() ? vr.getMinY() : 0);
        // right/bottom edge of text input area
        double rightEdgeInViewport = originLeftInViewport + textDim.getWidth();
        double bottomEdgeInViewport = originTopInViewport + textDim.getHeight();
        log.trace("right edge: %s %s".formatted(rightEdgeInViewport, bottomEdgeInViewport));
        // see the right/bottom edge exceeds how much.
        log.trace("viewport %s %s".formatted(vr.getWidth(), vr.getHeight()));
        double offsetX = Math.max(rightEdgeInViewport - vr.getWidth(), 0);
        double offsetY = Math.max(bottomEdgeInViewport - vr.getHeight(), 0);
        log.trace("offset %s x %s".formatted(offsetX, offsetY));
        // calculate the new center of text area.
        double newX = originalEditingBounds.getMinX() + textDim.getWidth() / 2 - offsetX;
        double newY = originalEditingBounds.getMinY() + textDim.getHeight() / 2 - offsetY;

        // relocate center of the StackPane.
        stackPane.relocate(newX, newY);
    }

    /**
     *
     * @param element
     * @return
     * @since 1.7
     */
    public Bounds getBoundsInCanvas(BaseElement element) {
        if (element == null) {
            return null;
        }
        Rectangle2D vr = this.control.getViewportRectangle();
        Rectangle2D bounds = element.getBounds();
        log.trace("node bounds: %s".formatted(RectangleUtils.rectangleInStr(bounds)));
        log.trace("viewport bounds: %s".formatted(RectangleUtils.rectangleInStr(vr)));
        // subtract the offset only when width/height is exceeds the viewport
        double x = bounds.getMinX() - (getSkinnable().isWidthOverViewport() ? 0: vr.getMinX());
        double y = bounds.getMinY() - (getSkinnable().isHeightOverViewport() ? 0: vr.getMinY());
        log.trace("x,y = %s,%s".formatted(x, y));
        return new BoundingBox(x, y, bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void endEdit() {
        log.info("end edit");
        originalEditingBounds = null;
        String text = textArea.getText();
        if (StringUtils.isBlank(text)) {
            control.onEditCanceled();
        }
        else {
            Dimension2D dim = calTextDimensionWithPaddingAndLimitation(textArea);
            control.endEdit(text, true);
        }
        Platform.runLater(() -> {
            textArea.setVisible(false);
            //requestFocus();
        });
    }

    /**
     * Calculate text bounds in TextArea with padding and limitation.
     *
     * @param textArea
     * @return
     */
    private Dimension2D calTextDimensionWithPaddingAndLimitation(TextArea textArea) {
        Dimension2D textDim = TextUtils.calculateTextBounds(textArea);
        double width = textDim.getWidth();
        double height = textDim.getHeight();
        // add extra padding from the topic node.

        BaseElement editingElement = control.elementUnderEdit;
        if (editingElement != null) {
            width = width + (editingElement.getBounds().getWidth() - editingElement.getTextBlock().getBounds().getWidth());
            height = height + (editingElement.getBounds().getHeight() - editingElement.getTextBlock().getBounds().getHeight());
        }
        width += 16; // extra padding for more fitting edit area!
        // limit text bounds to its original bounds.
        if (originalEditingBounds != null) {
            if (width < originalEditingBounds.getWidth()) width = originalEditingBounds.getWidth();
            if (height < originalEditingBounds.getHeight()) height = originalEditingBounds.getHeight();
        }
        return new Dimension2D(width, height);
    }

    public WritableImage takeSnapshot() {
        return MindMapCanvas.renderMindMapAsImage(this.control.getModel(), control.getConfig(), true);
    }

}
