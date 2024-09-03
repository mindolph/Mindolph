package com.mindolph.base.editor;

import com.mindolph.base.EditorContext;
import com.mindolph.base.control.ImageScrollPane;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.StatusMsg;
import com.mindolph.core.search.Anchor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author mindolph.com@gmail.com
 */
public class ImageViewerEditor extends BaseViewerEditor {

    @FXML
    private ImageScrollPane scrollableImageView;

    public ImageViewerEditor(EditorContext editorContext) {
        super("/editor/image_viewer_editor.fxml", editorContext);
    }

    @Override
    public void loadFile() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(editorContext.getFileData().getFile());
        Image image = new Image(fileInputStream);
        Platform.runLater(() -> {
            scrollableImageView.setImage(image);
            showImageInfo(1.0f);
        });
        this.scrollableImageView.getScalableView().scaleProperty().addListener((observable, oldValue, newValue) -> {
            showImageInfo(newValue.doubleValue());
        });
        this.outline();
    }

    private void showImageInfo(double scale) {
        Image image = scrollableImageView.getImage();
        double width = image.getWidth();
        double height = image.getHeight();
        String info = "%.0fx%.0f  %.0f%%".formatted(width, height, scale * 100);
        EventBus.getIns().notifyStatusMsg(editorContext.getFileData().getFile(), new StatusMsg(info));
    }

    @Override
    public void locate(Anchor anchor) {
        // DO NOTHING.
    }

    public Image getImage() {
        return scrollableImageView.getImage();
    }

    @Override
    public String getSelectionText() {
        return null;
    }
}
