package com.mindolph.base.dialog;

import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author mindolph.com@gmail.com
 */
public class ShowImageDialog extends BaseDialogController<Void> {

    public ShowImageDialog(Image image) {
        ImageView imageView = new ImageView(image);
        dialog = new CustomDialogBuilder<Void>()
                .title("Image Preview")
                .buttons(ButtonType.CLOSE)
                .controller(this)
                .fxContent(imageView)
                .build();
    }
}
