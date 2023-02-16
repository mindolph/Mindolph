package com.mindolph.base.container;

import com.mindolph.base.container.HiddenSplitPaneSkin.Partial;
import javafx.beans.DefaultProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * features:
 * Only 2 panel inside.
 * Hide tabs.
 * Fixed size primary or secondary panel.
 * Toggle orientation.
 *
 * @author mindolph.com@gmail.com
 * @see HiddenSplitPaneSkin
 * @deprecated
 */
@DefaultProperty("items")
public class HiddenSplitPane extends Control {

    private final ObservableList<Node> items = FXCollections.observableArrayList();

    private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(Orientation.HORIZONTAL);
    private final DoubleProperty fixedPartialSize = new SimpleDoubleProperty(0);
    private ObjectProperty<Partial> fixedPartial = new SimpleObjectProperty<>(Partial.PRIMARY);

    private Partial hiddenPartial; // remember the hidden partial to recover.

    public HiddenSplitPane() {
    }

    protected Skin<?> createDefaultSkin() {
        return new HiddenSplitPaneSkin(this);
    }

    public void enableDivider() {
        ((HiddenSplitPaneSkin) getSkin()).enableDividerListeners();
    }

    public void hidePrimary() {
        HiddenSplitPaneSkin skin = (HiddenSplitPaneSkin) getSkin();
        skin.hide(Partial.PRIMARY);
        this.hiddenPartial = Partial.PRIMARY;
    }

    public void hideSecondary() {
        HiddenSplitPaneSkin skin = (HiddenSplitPaneSkin) getSkin();
        skin.hide(Partial.SECONDARY);
        this.hiddenPartial = Partial.SECONDARY;
    }

    public void showAll() {
        HiddenSplitPaneSkin skin = (HiddenSplitPaneSkin) getSkin();
        skin.show();
        this.hiddenPartial = null;
    }

    public void toggleOrientation() {
        HiddenSplitPaneSkin skin = (HiddenSplitPaneSkin) getSkin();
        skin.toggleOrientation();
    }

    public Partial getHiddenPartial() {
        return hiddenPartial;
    }

    public void setHiddenPartial(Partial hiddenPartial) {
        this.hiddenPartial = hiddenPartial;
    }

    public ObservableList<Node> getItems() {
        return items;
    }

    public Partial getFixedPartial() {
        return fixedPartial.get();
    }

    public void setFixedPartial(Partial fixedPartial) {
        this.fixedPartial.set(fixedPartial);
    }

    public ObjectProperty<Partial> fixedPartialProperty() {
        if (fixedPartial == null) {
            fixedPartial = new StyleableObjectProperty<>() {
                @Override
                public Object getBean() {
                    return HiddenSplitPane.this;
                }

                @Override
                public String getName() {
                    return "fixedPartial";
                }

                @Override
                public CssMetaData<? extends Styleable, Partial> getCssMetaData() {
                    return null;
                }
            };
        }
        return fixedPartial;
    }

    public double getFixedPartialSize() {
        return fixedPartialSize.get();
    }

    public void setFixedPartialSize(double fixedPartialSize) {
        this.fixedPartialSize.set(fixedPartialSize);
    }

    public DoubleProperty fixedPartialSizeProperty() {
        return fixedPartialSize;
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

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        switch (attribute) {
            case CHILDREN:
                return getItems();
            default:
                return super.queryAccessibleAttribute(attribute, parameters);
        }
    }
}
