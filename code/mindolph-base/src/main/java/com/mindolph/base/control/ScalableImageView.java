package com.mindolph.base.control;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindolph.com@gmail.com
 * @see ScalableImageViewSkin
 */
@DefaultProperty("image")
public class ScalableImageView extends BaseScalableView {
    private static final Logger log = LoggerFactory.getLogger(ScalableImageView.class);

    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    public ScalableImageView() {
        log.info("ScalableImageView constructed.");
    }

    protected void onMouseClicked(MouseEvent mouseEvent){
        // INHERIT ME
    }

    @Override
    public WritableImage takeSnapshot() {
        return (WritableImage) image.get();
    }

    @Override
    protected Skin<ScalableImageView> createDefaultSkin() {
        ScalableImageViewSkin skin = new ScalableImageViewSkin(this);
        this.image.addListener((observableValue, oldImage, newImage) -> {
            if (newImage != null) {
                double w = newImage.getWidth() * getScale();
                double h = newImage.getHeight() * getScale();
                log.debug("Set prefSize from (%.2f x %.2f) to (%.2f x %.2f)".formatted(getPrefWidth(), getPrefHeight(), w, h));
                setPrefSize(w, h);
            }
            // force refresh because the viewport is already layouted before setting image.
            // what's bad is if the image larger than viewport, the refresh will be performed again.
            forceRefresh();
        });
        return skin;
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public void setImage(Image image) {
        this.image.set(image);
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object... parameters) {
        switch (accessibleAttribute) {
            case CONTENTS:
                return getImage();
            default:
                return super.queryAccessibleAttribute(accessibleAttribute, parameters);
        }
    }
}
