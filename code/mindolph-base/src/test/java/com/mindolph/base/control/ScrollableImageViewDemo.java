package com.mindolph.base.control;

import com.mindolph.mfx.util.FxImageUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 */
public class ScrollableImageViewDemo implements Initializable {
    private final Logger log = LoggerFactory.getLogger(ScrollableImageViewDemo.class);

    @FXML
    private ImageScrollPane scrollableImageView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Image img = FxImageUtils.readImageFromResource("/img_small.jpg");
            scrollableImageView.setImage(img);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Platform.runLater(() -> {
//            Bounds viewportBounds = scrollPane.getViewportBounds();
//            scalableImageView.setViewportRectangle(GeometryConvertUtils.boundsToRectangle(viewportBounds));
//            scrollPane.layout();
//        });
    }

    @FXML
    public void onChangeImage1() {
        try {
            Image img = FxImageUtils.readImageFromResource("/img_small.jpg");
            scrollableImageView.setImage(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onChangeImage2() {
        try {
            Image img = FxImageUtils.readImageFromResource("/img_medium.jpg");
            scrollableImageView.setImage(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void onChangeImage3() {
        try {
            Image img = FxImageUtils.readImageFromResource("/img_large.jpg");
            scrollableImageView.setImage(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
