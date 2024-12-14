package com.mindolph.base.dialog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.container.PreferenceItem;
import com.mindolph.base.control.snippet.ImageSnippet;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.model.Snippet;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @since 1.10.1
 */
public class SnippetDialog extends BaseDialogController<Snippet<?>> {

    private static final Logger log = LoggerFactory.getLogger(SnippetDialog.class);

    @FXML
    private RadioButton rdTypeText;
    @FXML
    private RadioButton rdTypeImage;
    @FXML
    private TextField tfTitle;
    @FXML
    private TextArea taCode;
    @FXML
    private Button btnIconImage;
    @FXML
    private PreferenceItem itemImage;

    private final String fileType;

    private static final String FILE_TYPE_SNIPPET_MAP_IN_JSON = """
            {
                "mmd": ["image"],
                "md": ["text"],
                "puml": ["text"]
            }
            """;

    private Map<String, List<String>> fileTypeSnippetMap;

    public SnippetDialog(String fileType, Snippet<?> snippet) {
        this.fileType = fileType;

        dialog = new CustomDialogBuilder<Snippet<?>>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Snippet")
                .fxmlUri("dialog/snippet_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .icon(ButtonType.CANCEL, FontIconManager.getIns().getIcon(IconKey.CANCEL))
                .defaultValue(snippet)
                .resizable(false)
                .controller(this)
                .width(500)
                .build();

        ToggleGroup group = new ToggleGroup();
        group.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            if (t1 == rdTypeText) {
                disable(btnIconImage);
                hide(itemImage);
            }
            else if (t1 == rdTypeImage) {
                enable(btnIconImage);
                show(itemImage);
            }
        });

        rdTypeText.setToggleGroup(group);
        rdTypeImage.setToggleGroup(group);
        tfTitle.textProperty().addListener((observableValue, s, t1) -> updateSnippet(null));
        taCode.textProperty().addListener((observableValue, s, t1) -> updateSnippet(null));
        btnIconImage.setOnAction(actionEvent -> {
            log.debug("Load image");
            File defaultDir = SystemUtils.getUserHome();
            File file = DialogFactory.openFileDialog(DialogFactory.DEFAULT_WINDOW, defaultDir,
                    new FileChooser.ExtensionFilter("Icon/Image", "*.png"));
            if (file.exists()) {
                log.debug(file.getAbsolutePath());
                updateSnippet(this.loadImage(file));
            }
        });
        if (snippet != null) {
            disable(tfTitle);
            Platform.runLater(() -> {
                if (Snippet.TYPE_TEXT.equals(snippet.getType())) {
                    rdTypeText.setSelected(true);
                }
                else if (Snippet.TYPE_IMAGE.equals(snippet.getType())) {
                    rdTypeImage.setSelected(true);
                }
                else {
                    log.warn("Wrong snippet type: {}", snippet.getType());
                }
            });
            tfTitle.setText(snippet.getTitle());
            taCode.setText(snippet.getCode());
            if (StringUtils.isNotBlank(snippet.getFilePath())) {
                this.loadImage(new File(snippet.getFilePath()));
            }
        }
        this.initUI(fileType);
    }

    private void initUI(String fileType) {
        try {
            this.fileTypeSnippetMap = JsonUtils.jsonTo(FILE_TYPE_SNIPPET_MAP_IN_JSON, new TypeReference<Map<String, List<String>>>(){});
            log.trace(StringUtils.join(this.fileTypeSnippetMap.get(SupportFileTypes.TYPE_MARKDOWN), ","));
        } catch (IOException e) {
            log.warn("failed to parse file type and snippet type mapping", e);
            this.fileTypeSnippetMap = new HashMap<>();
        }
        List<String> supportedTypes = this.fileTypeSnippetMap.get(fileType);
        log.debug(StringUtils.join(supportedTypes, ","));
        if (supportedTypes != null) {
            if (!supportedTypes.contains(Snippet.TYPE_IMAGE)) {
                hide(itemImage, rdTypeImage);
            }
        }
    }

    private Image loadImage(File file) {
        Image image = new Image(file.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(160);
        imageView.setFitHeight(160);
        btnIconImage.setGraphic(imageView);
        btnIconImage.setUserData(file.getAbsolutePath());
        return image;
    }

    private void updateSnippet(Image image) {
        if (image == null) {
            Snippet<?> snippet = new Snippet<>();
            snippet.title(tfTitle.getText()).code(taCode.getText()).type(Snippet.TYPE_TEXT);
            result = snippet;
        }
        else {
            ImageSnippet imageSnippet = new ImageSnippet(image);
            imageSnippet.title(tfTitle.getText()).code(taCode.getText()).type(Snippet.TYPE_IMAGE)
                    .filePath(String.valueOf(btnIconImage.getUserData()));
            result = imageSnippet;
        }
    }

}
