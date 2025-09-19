package com.mindolph.base.container;

import javafx.beans.DefaultProperty;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * @author mindolph.com@gmail.com
 * @see PreferenceItemSkin
 */
@DefaultProperty("items")
public class PreferenceItem extends Control {

    private final StringProperty name = new SimpleStringProperty("Label");

    private final DoubleProperty labelMinWidth = new SimpleDoubleProperty(250);

    private final DoubleProperty labelPrefWidth = new SimpleDoubleProperty(300);

    /**
     *
     */
    private ObjectProperty<Orientation> orientation;

    /**
     * Content items displaying in a preferences item.
     */
    private final ObservableList<Node> items = FXCollections.observableArrayList();

    public PreferenceItem() {
        super();
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
    }

    public PreferenceItem(Node... items) {
        this();
        this.items.addAll(items);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PreferenceItemSkin(this);
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


    public ObservableList<Node> getItems() {
        return items;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public final void setOrientation(Orientation value) {
        orientationProperty().set(value);
    };
    public final Orientation getOrientation() {
        return orientation == null ? Orientation.HORIZONTAL : orientation.get();
    }
    public final ObjectProperty<Orientation> orientationProperty() {
        if (orientation == null) {
            orientation = new SimpleObjectProperty<>();
        }
        return orientation;
    }

    public double getLabelMinWidth() {
        return labelMinWidth.get();
    }

    public void setLabelMinWidth(double labelMinWidth) {
        this.labelMinWidth.set(labelMinWidth);
    }

    public DoubleProperty labelMinWidthProperty() {
        return labelMinWidth;
    }

    public void setLabelPrefWidth(double value) {
        labelPrefWidth.set(value);
    }

    public double getLabelPrefWidth() {
        return labelPrefWidth.get();
    }

    public DoubleProperty labelPrefWidthProperty() {
        return labelPrefWidth;
    }
}
