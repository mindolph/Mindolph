package com.mindolph.base.container;

import com.mindolph.core.util.MathUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindolph.com@gmail.com
 * @see HiddenSplitPane
 * @deprecated
 */
public class HiddenSplitPaneSkin extends SkinBase<HiddenSplitPane> {
    private static final Logger log = LoggerFactory.getLogger(HiddenSplitPaneSkin.class);

    private final SplitPane splitPane;

    /**
     * Remember origin divider position when partial hiding.
     * This value will be updated when move divider, layout changes and partial hides.
     */
    private double originDividerPosition = 0;

    private Node leftNode;

    private Node rightNode;

    protected HiddenSplitPaneSkin(HiddenSplitPane control) {
        super(control);
        splitPane = new SplitPane();
        splitPane.getItems().addAll(control.getItems());
        splitPane.setOrientation(control.getOrientation());
//        if (control.getFixedPartial() == Partial.PRIMARY)
//            splitPane.setDividerPositions(0);
//        else if (control.getFixedPartial() == Partial.SECONDARY) {
//            splitPane.setDividerPositions(1);
//        }
        updateDividerPosition(control.getFixedPartialSize());

        originDividerPosition = splitPane.getDividerPositions()[0];
        if (splitPane.getItems().size() > 1) {
            leftNode = splitPane.getItems().get(0);
            rightNode = splitPane.getItems().get(1);
        }
        else {
            log.info("At least 2 items");
            return;
        }
        splitPane.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
            if (!newBounds.equals(oldBounds)) {
                log.debug(String.format("Layout changes from %sx%s to %sx%s", oldBounds.getWidth(), oldBounds.getHeight(), newBounds.getWidth(), newBounds.getHeight()));
                if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                    if (oldBounds.getWidth() == newBounds.getWidth()) return;
                }
                else if (splitPane.getOrientation() == Orientation.VERTICAL) {
                    if (oldBounds.getHeight() == newBounds.getHeight()) return;
                }
                double[] dividerPositions = splitPane.getDividerPositions();
                if (ArrayUtils.isNotEmpty(dividerPositions)) {
                    double dividerPosition = dividerPositions[0];
                    log.debug(String.format("Layout changes position %s with old value %sx%s", dividerPosition, oldBounds.getWidth(), oldBounds.getHeight()));
                    // keep the one partial width/height fixed by re-assign it's position from old bounds.
                    double dynamicFixedSize = fromPositionToSize(oldBounds, dividerPosition);
                    updateDividerPosition(dynamicFixedSize);
                }
            }
        });

        getChildren().clear();
        getChildren().add(splitPane);
        getSkinnable().requestLayout();

        // init the divider position by pre-defined partial size, update divider position directly is impossible
        // because the layout bounds is not ready yet.
        getSkinnable().fixedPartialSizeProperty().addListener((observable, oldSize, newSize) -> {
            if (!MathUtils.equalsIgnoreScale(oldSize.doubleValue(), newSize.doubleValue(), 0)) {
                log.debug(String.format("Default partial size changed from %s to %s", oldSize, newSize));
                updateDividerPosition(newSize.doubleValue());
            }
        });

        getSkinnable().fixedPartialProperty().addListener((observable, oldPartial, newPartial) -> {
            if (!newPartial.equals(oldPartial)) {
                log.debug(String.format("Default partial from %s to %s", oldPartial, newPartial));
                updateDividerPosition(getSkinnable().getFixedPartialSize());
            }
        });

        // Enable divider listeners later to avoid conflict with initialization.
        Platform.runLater(this::enableDividerListeners);
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
//        log.debug(String.format("layoutChildren: %sx%s", contentWidth, contentHeight));
    }

    /**
     * Enable handling position change for single divider.
     */
    public void enableDividerListeners() {
//        System.out.println("Enable divider listeners");
        Divider divider = splitPane.getDividers().get(0);
        divider.positionProperty().addListener((observable, oldValue, newValue) -> {
            //log.debug("Divider position changed from %s to %s", oldValue, newValue);
            double newPos = newValue.doubleValue();
            if (!Double.isInfinite(newPos) && !Double.isNaN(newPos)
                    && newPos != 0 && newValue.doubleValue() != oldValue.doubleValue()) { // avoid 0.0 position from construction.
                double initialPos = fromSizeToPosition(getSkinnable().getFixedPartialSize());
                if (!MathUtils.equalsIgnoreScale(newPos, initialPos, 3)) {
                    originDividerPosition = newPos;
                    double newSize = fromPositionToSize(newPos);
                    log.debug(String.format("Divider position changed: %s(%s)", newValue, newSize));
                    getSkinnable().fixedPartialSizeProperty().set(newSize);
                }
                else {
                    log.debug(String.format("Ignore %s -> %s", initialPos, newPos));
                }
            }
            else {
                log.debug("Illegal number: " + newValue);
            }
        });
    }

    /**
     * @param size
     */
    private void updateDividerPosition(double size) {
        if (Double.isNaN(size)) {
            return;
        }
        double newPos = fromSizeToPosition(size);
        if (!Double.isInfinite(newPos)) {
            log.debug(String.format("Update divider position: %s(%s)", newPos, size));
            originDividerPosition = newPos;
            splitPane.setDividerPosition(0, newPos);
        }
    }

    private double fromSizeToPosition(double size) {
        double total = splitPane.getOrientation() == Orientation.HORIZONTAL ? splitPane.getWidth() : splitPane.getHeight();
        if (total == 0) {
            return Double.POSITIVE_INFINITY;
        }
        double newPosition;
        if (getSkinnable().getFixedPartial() == Partial.PRIMARY) {
            newPosition = size / total;
        }
        else {
            newPosition = 1 - size / total;
        }
        return newPosition;
    }

    private double fromPositionToSize(Bounds bounds, double position) {
        double totalSize = splitPane.getOrientation() == Orientation.HORIZONTAL ? bounds.getWidth() : bounds.getHeight();
        if (totalSize == 0) {
            return Double.NaN;
        }
        return totalSize * position;
    }

    private double fromPositionToSize(double position) {
        return fromPositionToSize(splitPane.getLayoutBounds(), position);
    }

    /**
     * Hide partial of this panel.
     *
     * @param partial
     */
    public void hide(Partial partial) {
        if (splitPane.getDividerPositions().length == 0) {
            return;
        }
        originDividerPosition = splitPane.getDividerPositions()[0];
        ObservableList<Node> items = splitPane.getItems();
        if (partial == Partial.PRIMARY) {
            items.remove(this.leftNode);
            if (!items.contains(this.rightNode)) items.add(this.rightNode);
        }
        else if (partial == Partial.SECONDARY) {
            items.remove(this.rightNode);
            if (!items.contains(this.leftNode)) items.add(this.leftNode);
        }

    }

    public void show() {
        ObservableList<Node> items = splitPane.getItems();
        if (!items.contains(this.leftNode)) items.add(0, this.leftNode);
        if (!items.contains(this.rightNode)) items.add(1, this.rightNode);
        splitPane.setDividerPosition(0, originDividerPosition);
    }

    public void toggleOrientation() {
        if (splitPane.getOrientation() == Orientation.VERTICAL) {
            splitPane.setOrientation(Orientation.HORIZONTAL);
        }
        else {
            splitPane.setOrientation(Orientation.VERTICAL);
        }
    }

    public enum Partial {
        PRIMARY, SECONDARY
    }
}
