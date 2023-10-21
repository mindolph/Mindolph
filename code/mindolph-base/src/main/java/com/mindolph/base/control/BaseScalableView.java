package com.mindolph.base.control;

import com.mindolph.base.ShortcutManager;
import com.mindolph.base.constant.ShortcutConstants;
import com.mindolph.base.control.event.ScrollEventHandler;
import com.mindolph.base.util.ScrollUtils;
import com.mindolph.mfx.util.PointUtils;
import com.mindolph.mfx.util.RectangleUtils;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Control;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mindolph.mfx.util.PointUtils.pointInStr;

/**
 * <pre>
 * This view can be scaled by pinch, shortcuts(CTRL+=, CTRL+-, CTRL+0), or set scale directly.
 * Usage:
 * Contained in a {@link com.mindolph.base.container.ScalableScrollPane} by calling {@code ScalableScrollPane.setScalableView()}.
 * Call {@code setViewportRectangle()} to initialize the viewport rectangle when the {@link com.mindolph.base.container.ScalableScrollPane} is ready.
 * Calculate the dimension of the content of view and call {@code setOriginalDimension()} to initialize.
 * </pre>
 *
 * @author mindolph.com@gmail.com
 * @see BaseScalableViewSkin
 * @see com.mindolph.base.container.ScalableScrollPane
 */
public abstract class BaseScalableView extends Control {
    private static final Logger log = LoggerFactory.getLogger(BaseScalableView.class);

    private final ShortcutManager sm = ShortcutManager.getIns();

    // View dimensions
    protected ObjectProperty<Dimension2D> originalDimension = new SimpleObjectProperty<>(new Dimension2D(0, 0));
    protected ObjectProperty<Dimension2D> dimension = new SimpleObjectProperty<>(new Dimension2D(0, 0)); // TBD cancel？

    // Handler for scroll event.
    protected ScrollEventHandler scrollEventHandler; // handler when request scroll(if possible)
    protected final EventSource<Void> scrollDoneEvents = new EventSource<>(); // use this in derived class to subscribe scroll(animation) done event.

    // Listened by Skin class to force layout and re-draw.
    private final DoubleProperty scale = new SimpleDoubleProperty(1.0f);
    private final DoubleProperty minScale = new SimpleDoubleProperty(0.1f);
    private final DoubleProperty maxScale = new SimpleDoubleProperty(10f);
    // Viewport of container
    private final ObjectProperty<Rectangle2D> viewportRectangle = new SimpleObjectProperty<>();
    /**
     * Positions used to calculate the value of scroll.
     */
    protected Point2D mousePosInViewport = new Point2D(0, 0);
    protected Point2D mousePosInContent = new Point2D(0, 0);
    // This pos should be set after zoomed, viewport size changed or scrolled directly.
    protected Point2D lastScrollPos = new Point2D(0, 0);

    // Position when mouse pressed for dragging whole mind map.
    private Point2D mousePressedPos = new Point2D(0, 0);

    public BaseScalableView() {
        this.registerListeners();
    }

    protected void registerListeners() {
        this.layoutBoundsProperty().addListener((observableValue, oldBounds, newBounds) -> {
            // init the original dimension for calculating zoom in/out.
            if (newBounds.getWidth() > 0 && newBounds.getHeight() > 0) {
                dimension.set(originalDimension.get());
            }
        });
        // update last scroll pos because the change of viewport may change the scroll position.
        this.viewportRectangleProperty().addListener((observable, oldValue, newValue) ->
                lastScrollPos = new Point2D(newValue.getMinX(), newValue.getMinY()));

        // Scroll changes the point in content, or zoom in/out when CTRL is down.
        this.setOnScroll(event -> {
            // if (log.isTraceEnabled())log.trace(String.format("onScroll: %s, %s", event.getX(), event.getY()));
            mousePosInContent = new Point2D(event.getX(), event.getY());
            lastScrollPos = new Point2D(mousePosInContent.getX() - mousePosInViewport.getX(), mousePosInContent.getY() - mousePosInViewport.getY());
            if (event.isControlDown()) {
                if (event.isInertia()) {
                    event.consume();
                }
                else {
                    event.consume();
                    double newScale = ScrollUtils.scrollToScaleGradually(event.getDeltaY(), this.getScale());
                    final double limitedScale = Math.max(this.getMinScale(), Math.min(newScale, this.getMaxScale()));
                    Dimension2D oldDim = this.getDimension();
                    if (this.setScale(limitedScale)) {
                        scrollInPoint(oldDim, this.getDimension());
                    }
                }
            }
        });

        this.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                mousePressedPos = new Point2D(mouseEvent.getX(), mouseEvent.getY());
                this.requestFocus();
            }
            this.onMousePressed(mouseEvent);
        });

        // Mouse movement changes the points.
        this.setOnMouseMoved(event -> {
            mousePosInContent = new Point2D(event.getX(), event.getY());
            mousePosInViewport = new Point2D(mousePosInContent.getX() - lastScrollPos.getX(), mousePosInContent.getY() - lastScrollPos.getY());
            this.onMouseMoved(event);
        });

        this.setOnMouseDragged(mouseEvent -> {
            log.trace("%s".formatted(PointUtils.pointInStr(mousePressedPos)));
            if (sm.isMouseWithModifier(mouseEvent, ShortcutConstants.KEY_MODIFIER_DRAGGING)) {
                if (this.mousePressedPos != null) {
                    mouseEvent.consume();
                    this.setCursor(Cursor.HAND);
                    log.trace("Scroll by mouse: %s, %s".formatted(mouseEvent.getX(), mouseEvent.getY()));
                    scrollTo(mouseEvent);
                }
            }
            if (!mouseEvent.isConsumed()) {
                this.onMouseDragged(mouseEvent);
            }
        });
        this.setOnMouseDragReleased(event -> {
            this.setCursor(Cursor.DEFAULT);
        });
        this.setOnMouseReleased(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                mousePressedPos = null;
            }
            this.setCursor(Cursor.DEFAULT);
            this.onMouseReleased(mouseEvent);
        });

        // Zoom by pinch.
        this.setOnZoomStarted(event -> log.debug("Start zoom"));
        this.setOnZoom(event -> {
            double zoomFactor = event.getZoomFactor();
            Dimension2D oldDim = getDimension();
            if (this.setScale(this.getScale() * zoomFactor)) {
                Dimension2D newDim = getDimension();
                scrollInPoint(oldDim, newDim);
            }
        });
        this.setOnZoomFinished(event -> log.debug("Zoom finished"));
        // Zoom by shortcut(20% each press)
        this.setOnKeyPressed(event -> {
            double newScale = 1.0f;
            if (sm.isKeyEventMatch(event, ShortcutConstants.KEY_ZOOM_IN)) {
                event.consume();
                newScale = this.getScale() + 0.2;
            }
            else if (sm.isKeyEventMatch(event, ShortcutConstants.KEY_ZOOM_OUT)) {
                event.consume();
                newScale = this.getScale() - 0.2;
            }
            else if (sm.isKeyEventMatch(event, ShortcutConstants.KEY_ZOOM_RESET)) {
                event.consume();
                newScale = 1.0f;
            }
            else {
                onKeyPressed(event);
                return;
            }
            Dimension2D oldDim = getDimension();
            if (this.setScale(newScale)) {
                Dimension2D newDim = getDimension();
                scrollInCenter(oldDim, newDim);
            }
        });
    }

    protected void onMouseMoved(MouseEvent event) {
        // DO NOTHING
    }

    protected void onMousePressed(MouseEvent event) {
        // DO NOTHING
    }

    protected void onMouseDragged(MouseEvent event) {
        // DO NOTHING
    }

    protected void onMouseReleased(MouseEvent event) {
        // DO NOTHING
    }

    protected void onKeyPressed(KeyEvent event) {
        // DO NOTHING
    }

    /**
     * force to refresh the view.
     */
    public void forceRefresh() {
        BaseScalableViewSkin<?> skin = (BaseScalableViewSkin<?>) getSkin();
        skin.reLayout(getScale());
        skin.redraw();
    }

    /**
     * Fit the viewport when it's width/height is larger than content.
     */
    public void fitViewport() {
        if (this.isWidthOverViewport() && this.isHeightOverViewport()) {
            // both width and height are over the viewport, do nothing.
        }
        else {
            // reset x of viewport when it's width is over the content.
            if (!this.isWidthOverViewport()) {
                double padding = (this.getViewportRectangle().getWidth() - this.getLayoutBounds().getWidth()) / 2;
                this.setViewportRectangle(RectangleUtils.newWithX(this.getViewportRectangle(), -padding));
            }
            // reset y of viewport when it's height is over the content.
            if (!this.isHeightOverViewport()) {
                double padding = (this.getViewportRectangle().getHeight() - this.getLayoutBounds().getHeight()) / 2;
                this.setViewportRectangle(RectangleUtils.newWithY(this.getViewportRectangle(), -padding));
            }
        }
    }

    public void repaint() {
        BaseScalableViewSkin<?> skin = (BaseScalableViewSkin<?>) getSkin();
        if (skin != null) skin.redraw();
    }

    /**
     * Scroll to a position by translated mouse position.
     *
     * @param mouseEvent
     */
    public void scrollTo(MouseEvent mouseEvent) {
        double newx = lastScrollPos.getX() - (mouseEvent.getX() - mousePressedPos.getX());
        double newy = lastScrollPos.getY() - (mouseEvent.getY() - mousePressedPos.getY());
        log.debug("new scroll pos: %s, %s".formatted(newx, newy));
        scrollEventHandler.onScroll(new Point2D(newx, newy), false);
    }

    /**
     * Scroll by center position of viewport.
     *
     * @param oldDim
     * @param newDim
     * @return
     */
    public Point2D scrollInCenter(Dimension2D oldDim, Dimension2D newDim) {
        Rectangle2D viewportRectangle = this.getViewportRectangle();
        double newx = 0f;
        double newy = 0f;
        if (isWidthOverViewport()) {
            double centerX = viewportRectangle.getWidth() / 2;
            newx = ((centerX + lastScrollPos.getX()) * newDim.getWidth() - centerX * oldDim.getWidth()) / oldDim.getWidth();
        }
        if (isHeightOverViewport()) {
            double centerY = viewportRectangle.getHeight() / 2;
            newy = ((centerY + lastScrollPos.getY()) * newDim.getHeight() - centerY * oldDim.getHeight()) / oldDim.getHeight();
        }
        if (log.isTraceEnabled()) log.trace(String.format("Request scroll to position: %s", pointInStr(newx, newy)));
        if (isWidthOverViewport() || isHeightOverViewport()) {
            Point2D newPos = new Point2D(newx, newy);
            scrollEventHandler.onScroll(newPos, false);
            lastScrollPos = newPos;
        }
        return lastScrollPos;
    }

    /**
     * Scroll by mouse position in viewport and content for scale.
     *
     * @param oldDim
     * @param newDim
     * @return
     */
    public Point2D scrollInPoint(Dimension2D oldDim, Dimension2D newDim) {
        if (mousePosInViewport != null && mousePosInContent != null) {
            if (log.isTraceEnabled()) log.trace(String.format("Latest scroll pos: %s", pointInStr(lastScrollPos)));
            mousePosInContent = new Point2D(mousePosInViewport.getX() + lastScrollPos.getX(), mousePosInViewport.getY() + lastScrollPos.getY());
            if (log.isTraceEnabled())
                log.trace(String.format("Point in viewport: %s, point in content: %s", pointInStr(mousePosInViewport), pointInStr(mousePosInContent)));
            double newx = 0;
            double newy = 0;
            if (isWidthOverViewport()) {
                newx = ((mousePosInContent.getX() * newDim.getWidth() - mousePosInViewport.getX() * oldDim.getWidth()) / oldDim.getWidth());
            }
            if (isHeightOverViewport()) {
                newy = ((mousePosInContent.getY() * newDim.getHeight() - mousePosInViewport.getY() * oldDim.getHeight()) / oldDim.getHeight());
            }
            if (log.isTraceEnabled()) log.trace(String.format("Request scroll to position: (%.2f, %.2f)", newx, newy));
            // reset the mouse position in content
            mousePosInContent = new Point2D(mousePosInContent.getX() + newx, mousePosInContent.getY() + newy);
            lastScrollPos = new Point2D(newx, newy);
            scrollEventHandler.onScroll(lastScrollPos, false);
            return lastScrollPos;
        }
        else {
            log.warn("No mouse position to calculate!");
        }
        return null;
    }

    /**
     * Translate the mouse position to pos in content by viewport padding only if the content's
     * width/height is smaller than viewport's width/height.
     *
     * @param mouseEvent
     * @return
     */
    protected Point2D translateMousePos(MouseEvent mouseEvent) {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        return withoutViewportPadding(x, y);
    }

    /**
     * Create new point without viewport padding to the pos in content.
     *
     * @param x
     * @param y
     * @return
     */
    protected Point2D withoutViewportPadding(double x, double y) {
        double newx = isWidthOverViewport() ? x : x + getViewportRectangle().getMinX();
        double newy = isHeightOverViewport() ? y : y + getViewportRectangle().getMinY();
        return new Point2D(newx, newy);
    }


    /**
     * Create new point with viewport padding
     *
     * @param x
     * @param y
     * @return
     */
    protected Point2D withViewportPadding(double x, double y) {
        double newx = isWidthOverViewport() ? x : x - getViewportRectangle().getMinX();
        double newy = isHeightOverViewport() ? y : y - getViewportRectangle().getMinY();
        return new Point2D(newx, newy);
    }

    /**
     * Whether the width of this view is larger than viewport width.
     *
     * @return
     */
    public boolean isWidthOverViewport() {
        if (this.getViewportRectangle() == null) {
            return true;
        }
        double vrWidth = this.getViewportRectangle().getWidth();
        return vrWidth < this.getLayoutBounds().getWidth();
    }

    /**
     * Whether the height of this view is larger than viewport height.
     *
     * @return
     */
    public boolean isHeightOverViewport() {
        if (this.getViewportRectangle() == null) {
            return true;
        }
        double vrHeight = this.getViewportRectangle().getHeight();
        return vrHeight < this.getLayoutBounds().getHeight();
    }

    /**
     * Scale the view to fit the viewport width.
     */
    public void fitToViewportWidth() {
        double scale = this.getViewportRectangle().getWidth() / this.getOriginalDimension().getWidth();
        log.info("Scale to fit the viewport width: " + scale);
//        Dimension2D oldDim = getDimension();
        if (this.setScale(scale)) {
//            scrollInCenter(oldDim, getDimension());
        }
    }

    /**
     * Scale the view to fit the viewport height.
     */
    public void fitToViewportHeight() {
        double scale = this.getViewportRectangle().getHeight() / this.getOriginalDimension().getHeight();
        log.info("Scale to fit the viewport height: %.4f".formatted(scale));
//        Dimension2D oldDim = getDimension();
        if (this.setScale(scale)) {
//            scrollInCenter(oldDim, getDimension());
        }
    }

    public WritableImage takeViewportSnapshot() {
        BaseScalableViewSkin skin = (BaseScalableViewSkin) getSkin();
        WritableImage result = new WritableImage((int) skin.getCanvas().getWidth(), (int) skin.getCanvas().getHeight());
        skin.getCanvas().snapshot(null, result);
        return result;
    }

    public abstract WritableImage takeSnapshot();

    public Rectangle2D getViewportRectangle() {
        return viewportRectangle.get();
    }

    public ObjectProperty<Rectangle2D> viewportRectangleProperty() {
        return viewportRectangle;
    }

    public void setViewportRectangle(Rectangle2D viewportRectangle) {
        this.viewportRectangle.set(viewportRectangle);
    }

    public double getScale() {
        return scale.get();
    }

    public boolean setScale(double scale) {
        if (scale > minScale.get() && scale < maxScale.get()) {
            if (log.isTraceEnabled()) log.trace("Set scale to: %.4f".formatted(scale));
            this.scale.set(scale);
            return true;
        }
        else {
            if (log.isTraceEnabled())
                log.trace("Scale must between %.4f and %.4f".formatted(minScale.get(), maxScale.get()));
            return false;
        }
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public double getMinScale() {
        return minScale.get();
    }

    public DoubleProperty minScaleProperty() {
        return minScale;
    }

    public void setMinScale(double minScale) {
        this.minScale.set(minScale);
    }

    public double getMaxScale() {
        return maxScale.get();
    }

    public DoubleProperty maxScaleProperty() {
        return maxScale;
    }

    public void setMaxScale(double maxScale) {
        this.maxScale.set(maxScale);
    }

    public Dimension2D getOriginalDimension() {
        return originalDimension.get();
    }

    public ObjectProperty<Dimension2D> originalDimensionProperty() {
        return originalDimension;
    }

    public void setOriginalDimension(Dimension2D originalDimension) {
        this.originalDimension.set(originalDimension);
    }

    public Dimension2D getDimension() {
        return dimension.get();
    }

    public ObjectProperty<Dimension2D> dimensionProperty() {
        return dimension;
    }

    public void setDimension(Dimension2D dimension) {
        this.dimension.set(dimension);
    }

    public ScrollEventHandler getScrollEventHandler() {
        return scrollEventHandler;
    }

    public void setScrollEventHandler(ScrollEventHandler scrollEventHandler) {
        this.scrollEventHandler = scrollEventHandler;
    }

    public Point2D getMousePosInContent() {
        return mousePosInContent;
    }

    public Point2D getMousePosInViewport() {
        return mousePosInViewport;
    }

    public EventSource<Void> getScrollDoneEvents() {
        return scrollDoneEvents;
    }
}
