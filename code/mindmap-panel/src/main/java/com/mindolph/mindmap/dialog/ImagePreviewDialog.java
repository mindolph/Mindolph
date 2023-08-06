package com.mindolph.mindmap.dialog;

import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

/**
 * @author mindolph.com@gmail.com
 */
public class ImagePreviewDialog extends BaseDialogController<Image> {

    @FXML
    private ImageView ivPreview;
    @FXML
    private Slider sldSize;

    private double ratio;

    public ImagePreviewDialog(String title, Image originalImage) {
        super.origin = originalImage;
        super.result = originalImage;
        dialog = new CustomDialogBuilder<Image>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(title)
                .fxmlUri("dialog/image_preview_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .controller(ImagePreviewDialog.this)
                .build();

        ivPreview.setImage(originalImage);
        sldSize.setValue(50);
        this.scaleImage(50);

        sldSize.valueProperty().addListener((observableValue, oldRatio, newRatio) -> {
            this.scaleImage(newRatio.doubleValue());

        });
    }


    private void scaleImage(double percent) {
        this.ratio = percent / 100;
        int newWidth = (int) ((origin.getWidth() * percent) / 100);
        int newHeight = (int) ((origin.getHeight() * percent) / 100);
        ivPreview.setPreserveRatio(true);
        ivPreview.setFitWidth(newWidth);
        ivPreview.setFitHeight(newHeight);
        WritableImage scaledImage = new WritableImage((int) (origin.getWidth() * ratio), (int) (origin.getHeight() * ratio));
        this.ivPreview.snapshot(null, scaledImage);
        super.result = scaledImage;
    }
}
