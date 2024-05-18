package com.mindolph.base.container;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 *
 * @author mindolph.com@gmail.com
 * @see PreferenceItem
 */
public class PreferenceItemSkin extends SkinBase<PreferenceItem> {
    private static final double DEFAULT_PADDING = 4f;
    private static final double DEFAULT_CONTAINER_PADDING = 8f;
    private static final double DEFAULT_CONTAINER_SPACING = 6f;

    private final HBox root;
    private final Pane container;
    private final Label label;

    public PreferenceItemSkin(PreferenceItem control) {
        super(control);
        label = new Label("Label");
        label.textProperty().bind(getSkinnable().nameProperty());
        root = new HBox();

        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            container = new HBox();
        }
        else {
            container = new VBox();
        }
        container.getChildren().addAll(getSkinnable().getItems());
        root.getChildren().add(label);
        root.getChildren().add(container);
        getChildren().clear();
        getChildren().add(root);
        getSkinnable().requestLayout();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        PreferenceItem preferenceItem = getSkinnable();
        label.setAlignment(Pos.TOP_RIGHT);
        label.setMinWidth(100);
        label.setPrefWidth(300);
        root.setMinHeight(24);
//        root.setPrefHeight(36); this should not be set since it causes multiple children vertical layout abnormal.
        root.setSpacing(16);
        root.setAlignment(Pos.CENTER);

//        container.setMinHeight(root.getMinHeight() + DEFAULT_CONTAINER_PADDING * 2);
        container.setPadding(new Insets(DEFAULT_CONTAINER_PADDING));
        HBox.setHgrow(container, Priority.ALWAYS); // always grow in root node.

        //
        if (preferenceItem.getOrientation() == Orientation.HORIZONTAL) {
            ((HBox) container).setAlignment(Pos.CENTER_LEFT);
            ((HBox) container).setSpacing(DEFAULT_CONTAINER_SPACING);
        }
        else {
            ((VBox) container).setAlignment(Pos.TOP_LEFT);
            ((VBox) container).setSpacing(DEFAULT_CONTAINER_SPACING);
        }

    }
}
