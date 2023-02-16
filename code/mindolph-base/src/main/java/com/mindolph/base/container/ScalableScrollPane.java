package com.mindolph.base.container;

import com.mindolph.base.control.BaseScalableView;
import com.mindolph.mfx.container.ExtendedScrollPane;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mindolph.mfx.util.BoundsUtils.boundsInString;

/**
 * Scroll panel with {@link BaseScalableView}.
 * Usage:
 * Construct with an instance of BaseScalableView or call setScalableView().
 * Call {@code calculateAndUpdateViewportRectangle()} after loading content view for no listeners will be triggered
 * when content width/height is smaller than viewport width/height.
 *
 * @author mindolph.com@gmail.com
 * @see BaseScalableView
 * @see com.mindolph.base.control.BaseScalableViewSkin
 */
public class ScalableScrollPane extends ExtendedScrollPane {

    private final Logger log = LoggerFactory.getLogger(ScalableScrollPane.class);

    private BaseScalableView scalableView;

    public ScalableScrollPane() {
    }

    public ScalableScrollPane(BaseScalableView scalableView) {
        init(scalableView);
    }

    private void init(BaseScalableView scalableView) {
        this.scalableView = scalableView;
        this.setContent(scalableView);
        this.scalableView.setScrollEventHandler((scrollPos, animate) -> {
            if (animate) {
                this.scrollAnimate(scrollPos, () -> this.scalableView.getScrollDoneEvents().push(null));
            }
            else {
                this.scrollTo(scrollPos.getX(), scrollPos.getY());
                this.scalableView.getScrollDoneEvents().push(null);
            }
        });
        this.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if (log.isTraceEnabled())
                log.trace("Viewport bounds was changed from %s to %s".formatted(boundsInString(oldValue), boundsInString(newValue)));
            calculateAndUpdateViewportRectangle();
        });
//  if only one direction scrolls, nothing will be triggered until another direction scrolls
//        EventStream<Change<Number>> hvalueChanges = EventStreams.changesOf(this.hvalueProperty());
//        EventStream<Change<Number>> vvalueChanges = EventStreams.changesOf(this.vvalueProperty());
//        EventStream<Tuple2<Change<Number>, Change<Number>>> combine = EventStreams.combine(hvalueChanges, vvalueChanges);
//        combine.subscribe(changeChangeTuple -> {
//            Change<Number> hChanges = changeChangeTuple.get1();
//            Change<Number> vChanges = changeChangeTuple.get2();
//            if (hChanges.getOldValue().doubleValue() != hChanges.getNewValue().doubleValue()
//                    || vChanges.getOldValue().doubleValue() != vChanges.getNewValue().doubleValue()) {
//                if (log.isTraceEnabled())
//                    log.trace("Scroll value was changed to %.4f to %.4f".formatted(hChanges.getNewValue().doubleValue(), vChanges.getNewValue().doubleValue()));
//                calculateAndUpdateViewportRectangle();
//            }
//        });
        this.hvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() != oldValue.doubleValue()) {
                if (log.isTraceEnabled())
                    log.trace("Horizontal scroll value was changed from %.4f to %.4f".formatted(oldValue.doubleValue(), newValue.doubleValue()));
                calculateAndUpdateViewportRectangle();
            }
        });
        this.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() != oldValue.doubleValue()) {
                if (log.isTraceEnabled())
                    log.trace("Vertical scroll value was changed from %.4f to %.4f".formatted(oldValue.doubleValue(), newValue.doubleValue()));
                calculateAndUpdateViewportRectangle();
            }
        });
        log.debug("ScalableScrollPane constructed");
    }

    /**
     * Should be called when content changes.
     */
    public void calculateAndUpdateViewportRectangle() {
        Bounds viewportBounds = this.getViewportBounds();
        Rectangle2D viewportRect = null;
        if (scalableView.getViewportRectangle() == null) {
            viewportRect = new Rectangle2D(this.getScrollX(), this.getScrollY(), viewportBounds.getWidth(), viewportBounds.getHeight());
        }
        else {
            double x = scalableView.isWidthOverViewport() ? this.getScrollX() : -(viewportBounds.getWidth() - scalableView.getLayoutBounds().getWidth()) / 2;
            double y = scalableView.isHeightOverViewport() ? this.getScrollY() : -(viewportBounds.getHeight() - scalableView.getLayoutBounds().getHeight()) / 2;
            viewportRect = new Rectangle2D(x, y, viewportBounds.getWidth(), viewportBounds.getHeight());
        }
        scalableView.setViewportRectangle(viewportRect);
    }

    public void setScalableView(BaseScalableView scalableView) {
        this.init(scalableView);
    }

    public BaseScalableView getScalableView() {
        return scalableView;
    }
}
