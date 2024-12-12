package com.mindolph.base.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.snippet.ImageSnippet;
import com.mindolph.core.model.Snippet;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * @since 1.10.1
 */
public class SnippetDialog extends BaseDialogController<Snippet<?>> {

    private static final Logger log = LoggerFactory.getLogger(SnippetDialog.class);

    @FXML
    private RadioButton rdTypeText;
    @FXML
    private RadioButton rdTypeIcon;
    @FXML
    private TextField tfTitle;
    @FXML
    private TextArea taCode;
    @FXML
    private Button btnIconImage;

    private final String fileType;

    public SnippetDialog(String fileType, Snippet<?> snippet) {
        this.fileType = fileType;
        dialog = new CustomDialogBuilder<Snippet<?>>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("")
                .fxmlUri("dialog/snippet_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .icon(ButtonType.CANCEL, FontIconManager.getIns().getIcon(IconKey.CANCEL))
                .defaultValue(snippet)
                .resizable(false)
                .controller(this)
                .width(500)
                .height(600)
                .build();

        ToggleGroup group = new ToggleGroup();
        if (snippet != null) {
            disable(tfTitle);
            tfTitle.setText(snippet.getTitle());
            taCode.setText(snippet.getCode());
            if (snippet instanceof ImageSnippet imgSnippet) {
                btnIconImage.setGraphic(new ImageView(imgSnippet.getImage()));
            }
        }

        group.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            if (t1 == rdTypeText) {
                disable(btnIconImage);
                hide(btnIconImage);
            }
            else if (t1 == rdTypeIcon) {
                enable(btnIconImage);
                show(btnIconImage);
            }
        });

        rdTypeText.setToggleGroup(group);
        rdTypeIcon.setToggleGroup(group);
        tfTitle.textProperty().addListener((observableValue, s, t1) -> updateSnippet(null));
        taCode.textProperty().addListener((observableValue, s, t1) -> updateSnippet(null));
        btnIconImage.setOnAction(actionEvent -> {
            log.debug("Load");
            File defaultDir = SystemUtils.getUserHome();
            File file = DialogFactory.openFileDialog(DialogFactory.DEFAULT_WINDOW, defaultDir,
                    new FileChooser.ExtensionFilter("Icon/Image", "*.png"));
            if (file.exists()) {
                log.debug(file.getAbsolutePath());
                Image image = new Image(file.toURI().toString());
                btnIconImage.setGraphic(new ImageView(image));
                updateSnippet(image);
            }
        });
    }

    private void updateSnippet(Image image) {
        if (image == null) {
            Snippet<?> snippet = new Snippet<>();
            snippet.title(tfTitle.getText()).code(taCode.getText());
            result = snippet;
        }
        else {
            ImageSnippet imageSnippet = new ImageSnippet(image);
            imageSnippet.title(tfTitle.getText()).code(taCode.getText());
            result = imageSnippet;
        }
    }

}
