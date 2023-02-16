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
 * Note: abnormal layout for vertical TODO
 *
 * @author mindolph.com@gmail.com
 */
public class PreferenceItemSkin extends SkinBase<PreferenceItem> {
    private final double DEFAULT_PADDING = 4f;
    private final double DEFAULT_CONTAINER_PADDING = 8f;
    private final double DEFAULT_CONTAINER_SPACING = 6f;

    private final Pane container;
    private final Label label;
    private final HBox content;

    public PreferenceItemSkin(PreferenceItem control) {
        super(control);
        label = new Label("Label");
        label.textProperty().bind(getSkinnable().nameProperty());
        content = new HBox();

        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            container = new HBox();
        }
        else {
            container = new VBox();
        }
        container.getChildren().addAll(getSkinnable().getItems());
        content.getChildren().add(label);
        content.getChildren().add(container);
        getChildren().clear();
        getChildren().add(content);
        getSkinnable().requestLayout();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        PreferenceItem preferenceItem = getSkinnable();
        label.setAlignment(Pos.TOP_RIGHT);
        label.setMinWidth(100);
        label.setPrefWidth(300);
        content.setMinHeight(32);
        content.setPrefHeight(36);
        content.setSpacing(16);
        content.setAlignment(Pos.CENTER);

        container.setMinHeight(content.getMinHeight() + DEFAULT_CONTAINER_PADDING * 2);
        container.setPadding(new Insets(DEFAULT_CONTAINER_PADDING));

        // content
        if (preferenceItem.getOrientation() == Orientation.HORIZONTAL) {
            ((HBox) container).setAlignment(Pos.CENTER_LEFT);
            ((HBox) container).setSpacing(DEFAULT_CONTAINER_SPACING);
        }
        else {
            ((VBox) container).setAlignment(Pos.CENTER_LEFT);
            ((VBox) container).setSpacing(DEFAULT_CONTAINER_SPACING);
        }
        HBox.setHgrow(container, Priority.ALWAYS); // always grow in parent container.
    }
}
