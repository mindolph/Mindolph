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
    public void loadFile(Runnable afterLoading) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(editorContext.getFileData().getFile());
        Image image = new Image(fileInputStream);
        Platform.runLater(() -> {
            scrollableImageView.setImage(image);
            afterLoading.run();
        });
        this.scrollableImageView.getScalableView().scaleProperty().addListener((observable, oldValue, newValue) -> {
            EventBus.getIns().notifyStatusMsg(editorContext.getFileData().getFile(), new StatusMsg("%.0f%%".formatted(newValue.doubleValue() * 100)));
        });
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
