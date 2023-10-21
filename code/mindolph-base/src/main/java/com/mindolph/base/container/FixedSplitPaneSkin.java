package com.mindolph.base.container;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.control.SkinBase;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindolph.com@gmail.com
 * @see FixedSplitPane
 */
public class FixedSplitPaneSkin extends SkinBase<FixedSplitPane> {
    private static final Logger log = LoggerFactory.getLogger(FixedSplitPaneSkin.class);

    private final SplitPane splitPane;

    protected FixedSplitPaneSkin(FixedSplitPane control) {
        super(control);
        FixedSplitPane pane = getSkinnable();

        splitPane = new SplitPane();
        getChildren().add(splitPane);

        log.debug("%s: primary %s".formatted(pane.getUserData(), pane.getPrimary()));
        log.debug("%s: secondary: %s".formatted(pane.getUserData(), pane.getSecondary()));

        splitPane.getItems().add(pane.getPrimary());
        splitPane.getItems().add(pane.getSecondary());

        // initialize before setting listeners.
        splitPane.setOrientation(pane.getOrientation());

        // change splitter position directly.
        pane.splitterPositionProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<SplitPane.Divider> dividers = splitPane.getDividers();
            if (!dividers.isEmpty()){
                dividers.get(0).setPosition(newValue.doubleValue());
            }
        });

        pane.orientationProperty().addListener((observable, oldValue, newValue) -> {
            log.debug("%s: orientation %s".formatted(pane.getUserData(), newValue));
            splitPane.setOrientation(newValue);
        });
        log.debug("%s: listening orientation change.".formatted(pane.getUserData()));

        pane.fixedProperty().addListener((observable, oldValue, newValue) -> {
            log.trace("fixed changes");
            applyFixedSize();
        });

        pane.fixedSizeProperty().addListener((observable, oldValue, newValue) -> {
            log.trace("fixed size changes");
            applyFixedSize();
        });

        splitPane.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            applyFixedSize();
        });

        this.enableDividerListener();

        pane.hiddenProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                log.debug("show all");
                this.addItemIfNotThere(pane.getPrimary(), 0);
                this.addItemIfNotThere(pane.getSecondary(), 1);
                this.enableDividerListener();
                this.applyFixedSize();
            }
            else {
                splitPane.getItems().remove(newValue);
                if (newValue == pane.getPrimary()) {
                    log.debug("hide primary");
                    this.addItemIfNotThere(pane.getSecondary(), 0);
                }
                else {
                    log.debug("hide secondary");
                    this.addItemIfNotThere(pane.getPrimary(), 0);
                }
            }
        });
        log.debug("Skin initialization is done.");
    }

    /**
     * The divider is disposed when hide primary/secondary, use this method to re-register listener.
     */
    private void enableDividerListener() {
        if (splitPane.getDividers().isEmpty()) {
            log.warn("No divider exists");
            return;
        }
        log.debug("enable divider listener");
        SplitPane.Divider divider = splitPane.getDividers().get(0);
        divider.positionProperty().addListener((observable, oldValue, newValue) -> {
            // to keep size fixed when divider's position is changing.
            double size = fromPositionToSize(newValue.doubleValue());
            log.trace("convert position from %.2f to size %.2f".formatted(newValue.doubleValue(), size));
            getSkinnable().setFixedSize(size);
        });
    }

    private void addItemIfNotThere(Region item, int idx) {
        if (!splitPane.getItems().contains(item)) {
            splitPane.getItems().add(idx, item);
        }
    }

    private void applyFixedSize() {
        log.trace("apply fixed size");
        // Reset the divider position
        double pos = fromSizeToPosition(getSkinnable().getFixedSize());
        log.trace("%s - from size %.2f to pos %.2f".formatted(getSkinnable().getFixed(), getSkinnable().getFixedSize(), pos));
        if (isValidNumber(pos) && !splitPane.getDividers().isEmpty()) {
            splitPane.getDividers().get(0).setPosition(pos);
            getSkinnable().setSplitterPosition(pos); // reset to align the position value.
        }
        else {
            log.debug("invalid position(%.2f) or no dividers to apply the fixed size".formatted(pos));
        }
    }

    private boolean isValidNumber(double v) {
        return !Double.isInfinite(v) && !Double.isNaN(v);
    }

    private double fromSizeToPosition(double size) {
        double total = splitPane.getOrientation() == Orientation.HORIZONTAL ? splitPane.getWidth() : splitPane.getHeight();
        if (total == 0) {
            return Double.POSITIVE_INFINITY;
        }
        double newPosition;
        if (getSkinnable().isSecondaryFixed()) {
            newPosition = 1 - size / total;
        }
        else {
            newPosition = size / total;
        }
        return newPosition;
    }


    private double fromPositionToSize(double position) {
        return fromPositionToSize(splitPane.getLayoutBounds(), position);
    }

    private double fromPositionToSize(Bounds bounds, double position) {
        double totalSize = splitPane.getOrientation() == Orientation.HORIZONTAL ? bounds.getWidth() : bounds.getHeight();
        if (totalSize == 0) {
            return Double.NaN;
        }
        if (getSkinnable().isSecondaryFixed()) {
            return totalSize * (1 - position);
        }
        else {
            return totalSize * position;
        }
    }

    private void makeFixed() {
        FixedSplitPane pane = getSkinnable();
        Region toBeFixed = pane.getFixed();
        if (toBeFixed != null) {
            if (pane.getOrientation() == Orientation.HORIZONTAL) {
                toBeFixed.setMinWidth(pane.getFixedSize());
                toBeFixed.setMaxWidth(pane.getFixedSize());
            }
            else if (pane.getOrientation() == Orientation.VERTICAL) {
                toBeFixed.setMinHeight(pane.getFixedSize());
                toBeFixed.setMaxHeight(pane.getFixedSize());
            }
            clearFixed(pane.getNotFixed());
        }
    }

    private void clearFixed(Region fixed) {
        if (fixed != null) {
            fixed.setMinWidth(-1);
            fixed.setMaxWidth(-1);
            fixed.setMinHeight(-1);
            fixed.setMaxHeight(-1);
        }
    }
}
