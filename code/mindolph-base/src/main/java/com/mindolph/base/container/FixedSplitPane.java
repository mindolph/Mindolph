package com.mindolph.base.container;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;

/**
 * features:
 * Only 2 panel inside.
 * Hide panels.
 * Fixed size primary or secondary panel.
 * Toggle orientation.
 *
 * @author mindolph.com@gmail.com
 * @see FixedSplitPaneSkin
 */
public class FixedSplitPane extends Control {

    private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(Orientation.HORIZONTAL);

    private final DoubleProperty fixedSize = new SimpleDoubleProperty(0);

    private final ObjectProperty<Region> primary = new SimpleObjectProperty<>();

    private final ObjectProperty<Region> secondary = new SimpleObjectProperty<>();

    private final ObjectProperty<Region> fixed = new SimpleObjectProperty<>();

    private final ObjectProperty<Region> hidden = new SimpleObjectProperty<>();

    // Splitter position property to set the inner splitter's position
    private final DoubleProperty splitterPosition = new SimpleDoubleProperty(0);

    public FixedSplitPane() {
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FixedSplitPaneSkin(this);
    }

    public void toggleOrientation() {
        if (orientation.get() == Orientation.VERTICAL) {
            orientation.set(Orientation.HORIZONTAL);
        }
        else {
            orientation.set(Orientation.VERTICAL);
        }
    }

    public void hidePrimary() {
        this.hidden.set(this.primary.get());
    }

    public void hideSecondary() {
        this.hidden.set(this.secondary.get());
    }

    public void showAll() {
        this.hidden.set(null);
    }

    public boolean isPrimaryFixed() {
        return this.fixed.get() == this.primary.get();
    }

    public boolean isSecondaryFixed() {
        return this.fixed.get() == this.secondary.get();
    }

    public boolean isPrimaryHidden() {
        return this.hidden.get() == this.primary.get();
    }

    public boolean isSecondaryHidden() {
        return this.hidden.get() == this.secondary.get();
    }

    public Orientation getOrientation() {
        return orientation.get();
    }

    public ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation.set(orientation);
    }

    public double getFixedSize() {
        return fixedSize.get();
    }

    public DoubleProperty fixedSizeProperty() {
        return fixedSize;
    }

    public void setFixedSize(double fixedSize) {
        this.fixedSize.set(fixedSize);
    }

    public Region getPrimary() {
        return primary.get();
    }

    public ObjectProperty<Region> primaryProperty() {
        return primary;
    }

    public void setPrimary(Region primary) {
        this.primary.set(primary);
    }

    public Region getSecondary() {
        return secondary.get();
    }

    public ObjectProperty<Region> secondaryProperty() {
        return secondary;
    }

    public void setSecondary(Region secondary) {
        this.secondary.set(secondary);
    }

    public Region getFixed() {
        return fixed.get();
    }

    public ObjectProperty<Region> fixedProperty() {
        return fixed;
    }

    public void setFixed(Region fixed) {
        this.fixed.set(fixed);
    }

    public Region getNotFixed() {
        if (fixed.get() == primary.get()) {
            return secondary.get();
        }
        else {
            return primary.get();
        }
    }

    public Region getHidden() {
        return hidden.get();
    }

    public ObjectProperty<Region> hiddenProperty() {
        return hidden;
    }

    public void setHidden(Region hidden) {
        this.hidden.set(hidden);
    }

    public double getSplitterPosition() {
        return splitterPosition.get();
    }

    public DoubleProperty splitterPositionProperty() {
        return splitterPosition;
    }

    public void setSplitterPosition(double splitterPosition) {
        this.splitterPosition.set(splitterPosition);
    }
}
