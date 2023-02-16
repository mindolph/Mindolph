package com.mindolph.base.control;

import com.mindolph.base.container.ScalableScrollPane;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends {@link ScalableScrollPane} with {@link ScalableImageView} inside.
 *
 * @author mindolph.com@gmail.com
 * @see ScalableScrollPane
 * @see ScalableImageView
 */
public class ImageScrollPane extends ScalableScrollPane {
    private final Logger log = LoggerFactory.getLogger(ImageScrollPane.class);

    private final ScalableImageView scalableImageView;

    public ImageScrollPane() {
        this.scalableImageView = new ScalableImageView();
        super.setScalableView(this.scalableImageView);
        Bounds viewportBounds = this.getViewportBounds();
        this.scalableImageView.setViewportRectangle(new Rectangle2D(0, 0, viewportBounds.getWidth(), viewportBounds.getHeight()));
        this.scalableImageView.setOnMouseClicked(event -> {
            log.debug("Mouse clicked on image view");
            this.scalableImageView.requestFocus();// focus to enable key event handlers
            fireEvent(event); // this is a workaround for container scroll panel can't get mouse clicked event from its content view(don't know why)
        });
    }

    public void setImage(Image image) {
        if (image != null) {
            log.debug("Set image: %sx%s".formatted(image.getWidth(), image.getHeight()));
            this.scalableImageView.setImage(image);
            this.scalableImageView.setOriginalDimension(new Dimension2D(image.getWidth(), image.getHeight()));
            log.debug("Calculate and update viewport rectangle");
            super.calculateAndUpdateViewportRectangle();// for smaller images.
        }
        else {
            this.scalableImageView.setImage(null);
        }
    }

    public Image getImage() {
        return this.scalableImageView.getImage();
    }

}
