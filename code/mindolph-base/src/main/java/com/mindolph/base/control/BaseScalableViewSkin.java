package com.mindolph.base.control;

import com.mindolph.mfx.util.RectangleUtils;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.swiftboot.util.CalcUtils.limitIn;
import static com.mindolph.mfx.util.RectangleUtils.rectangleInStr;
import static com.mindolph.mfx.util.RectangleUtils.sizeInStr;

/**
 * Only content in viewport of container be drawn, if the (scaled) content width/height is less than viewport,
 * the background fill in the whole viewport. The viewport changes or scale changes may cause re-layout and redraw.
 * Sub-classes should implement {@code drawContent()} with calling {@code translateGraphicsContext()}.
 *
 * @author mindolph.com@gmail.com
 * @see BaseScalableView
 */
public abstract class BaseScalableViewSkin<T extends BaseScalableView> extends SkinBase<T> {
    private static final Logger log = LoggerFactory.getLogger(BaseScalableViewSkin.class);

    protected T control;
    protected Canvas canvas;
    protected GraphicsContext gc;
    // Listeners
    protected ChangeListener<Number> scaleListener;

    int GRID_SIZE = 100;

    public BaseScalableViewSkin(T control) {
        super(control);
        this.control = control;
        this.canvas = new Canvas(100, 100);
        this.canvas.setManaged(false); // let canvas layout managed by me.
        this.getChildren().add(this.canvas);
        this.gc = canvas.getGraphicsContext2D();
        this.registerListeners();
    }

    protected void registerListeners() {
        // Viewport change translate or resize the canvas and re-layout.
        this.control.viewportRectangleProperty().addListener((observable, oldRect, newRect) -> {
            if (newRect.equals(oldRect)) {
                log.debug("Ignore viewport change");
                return; // ignore non-change
            }
            if (log.isTraceEnabled())
                log.trace("Viewport rectangle was changed from %s to %s".formatted(rectangleInStr(oldRect), rectangleInStr(newRect)));
            canvas.setLayoutX(Math.max(0, newRect.getMinX()));// Use 0 as min value to prevent canvas from moving during zooming out.
            canvas.setLayoutY(Math.max(0, newRect.getMinY()));
            // Re-layout only if viewport size is changed.
            if (!RectangleUtils.sizeEquals(oldRect, newRect)) {
                log.debug("Size of viewport was changed from %s to %s".formatted(sizeInStr(oldRect), sizeInStr(newRect)));
                canvas.setWidth(newRect.getWidth());
                canvas.setHeight(newRect.getHeight());
                reLayout(this.control.getScale());
            }
            redraw();
        });
        //
        scaleListener = (observableValue, oldScale, newScale) -> {
            this.reLayout(newScale.doubleValue());
        };
        this.control.scaleProperty().addListener(scaleListener);
    }

    /**
     * Re-layout when needed like content bounds or scale changed.
     * Override me if very different calculation is required (call the fitViewport() in the end).
     *
     * @param newScale
     */
    public void reLayout(double newScale) {
        if (log.isDebugEnabled()) log.debug("reLayout()");
        double originalWidth = control.getOriginalDimension().getWidth();
        double originalHeight = control.getOriginalDimension().getHeight();
        double w = originalWidth * newScale;
        double h = originalHeight * newScale;
        if (log.isTraceEnabled()) log.trace("Set control prefSize and dimension to %.1f x %.1f".formatted(w, h));
        control.setPrefSize(w, h);
        control.getParent().layout(); // this is important, may cause parent container aware that the bounds is changed.
        control.setDimension(new Dimension2D(w, h)); // listeners will be triggered.
        control.fitViewport();
    }

    public void redraw() {
        this.drawBackground();
        this.drawContent();
    }

    protected void drawBackground() {
        if (log.isTraceEnabled()) log.trace("Draw background");
        gc = this.canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    protected abstract void drawContent();

    /**
     * Translate graphics context to fit the scroll viewport.
     * If the control bounds is not over viewport, the control will be center of the viewport.
     *
     * @param reverse if true, reverse back to original translation.
     * @return The point that translate to.
     */
    protected Point2D translateGraphicsContext(boolean reverse) {
        Rectangle2D vr = this.control.getViewportRectangle();
        double tx = -vr.getMinX();
        double ty = -vr.getMinY();
        tx = reverse ? -tx : tx;
        ty = reverse ? -ty : ty;
        gc.translate(tx, ty);
        if (log.isTraceEnabled()) log.trace("Translate graphics context to (%.1f, %.1f)".formatted(tx, ty));
        return new Point2D(tx, ty);
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return Double.MIN_VALUE;
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return Double.MIN_VALUE;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return Double.MAX_VALUE;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return Double.MAX_VALUE;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Limit x in the viewport of container.
     *
     * @param x position in the view
     * @return
     */
    protected double limitX(double x) {
        Rectangle2D vr = this.control.getViewportRectangle();
        return limitIn(x, vr.getMinX(), vr.getMaxX());
    }

    /**
     * Limit y in the viewport of container.
     *
     * @param y position in the view
     * @return
     */
    protected double limitY(double y) {
        Rectangle2D vr = this.control.getViewportRectangle();
        return limitIn(y, vr.getMinY(), vr.getMaxY());
    }

    protected Point2D limit(double x, double y) {
        Rectangle2D vr = this.control.getViewportRectangle();
        return new Point2D(limitIn(x, vr.getMinX(), vr.getMaxX()), limitIn(y, vr.getMinY(), vr.getMaxY()));
    }

    protected Point2D limit(Point2D p) {
        Rectangle2D vr = this.control.getViewportRectangle();
        return new Point2D(limitIn(p.getX(), vr.getMinX(), vr.getMaxX()), limitIn(p.getY(), vr.getMinY(), vr.getMaxY()));
    }

    @Override
    public void dispose() {
        super.dispose();
        this.control.scaleProperty().removeListener(scaleListener);
    }
}
