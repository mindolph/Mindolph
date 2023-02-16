package com.mindolph.base.control;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindolph.com@gmail.com
 * @see ScalableImageView
 */
public class ScalableImageViewSkin extends BaseScalableViewSkin<ScalableImageView> {

    private final Logger log = LoggerFactory.getLogger(ScalableImageViewSkin.class);

    public ScalableImageViewSkin(ScalableImageView control) {
        super(control);
        log.info("ScalableImageViewSkin constructed.");
    }

    @Override
    public void reLayout(double newScale) {
        Image image = this.control.getImage();
        if (image != null) {
            double w = control.getScale() * image.getWidth();
            double h = control.getScale() * image.getHeight();
            this.control.setPrefSize(w, h);
            this.control.getParent().layout(); // this is important, may cause parent container aware that the bounds is changed.
            this.control.setDimension(new Dimension2D(w, h)); // listeners will act by the dimension changes.
        }
    }

    @Override
    protected void drawContent() {
        if (log.isTraceEnabled()) log.trace("Draw image");
        Image image = this.control.getImage();
        if (image != null && !image.isError()) {
            super.translateGraphicsContext(false);
            gc.drawImage(image, 0, 0, this.control.getScale() * image.getWidth(), this.control.getScale() * image.getHeight());
            super.translateGraphicsContext(true);
        }
        else {
            if (log.isDebugEnabled()) log.debug("No image specified to draw");
        }
    }
}
