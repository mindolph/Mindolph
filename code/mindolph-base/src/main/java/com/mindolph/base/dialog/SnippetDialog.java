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
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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
    private TextArea taDesc;
    @FXML
    private Button btnIconImage;
    @FXML
    private VBox vbox;
    @FXML
    private PreferenceItem itemCode;
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
                .title("%s Snippet".formatted(snippet == null ? "New" : "Edit"))
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
                switchCodeImage();
            }
            else if (t1 == rdTypeImage) {
                enable(btnIconImage);
                switchCodeImage();
            }
        });

        rdTypeText.setToggleGroup(group);
        rdTypeImage.setToggleGroup(group);
        tfTitle.textProperty().addListener((observableValue, s, t1) -> updateSnippet(null));
        taCode.textProperty().addListener((observableValue, s, t1) -> updateSnippet(null));
        taDesc.textProperty().addListener((observableValue, s, t1) -> updateSnippet(null));
        btnIconImage.setOnAction(actionEvent -> {
            log.debug("Load image");
            File defaultDir = SystemUtils.getUserHome();
            File file = DialogFactory.openFileDialog(DialogFactory.DEFAULT_WINDOW, defaultDir,
                    new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg"));
            if (file != null && file.exists()) {
                if (log.isTraceEnabled()) log.trace(file.getAbsolutePath());
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
                taCode.requestFocus();
            });
            tfTitle.setText(snippet.getTitle());
            taCode.setText(snippet.getCode());
            taDesc.setText(snippet.getDescription());
            if (StringUtils.isNotBlank(snippet.getFilePath())) {
                this.loadImage(new File(snippet.getFilePath()));
            }
        }
        else {
            Platform.runLater(() -> {tfTitle.requestFocus();});
        }
        this.initUI(fileType);
    }

    private void switchCodeImage() {
        ObservableList<Node> children = vbox.getChildren();
        if (children.contains(itemImage)) {
            children.remove(itemImage);
            if (!children.contains(itemCode)) {
                children.add(itemCode);
            }
        }
        else {
            children.remove(itemCode);
            if (!children.contains(itemImage)) {
                children.add(itemImage);
            }
        }
    }

    private void initUI(String fileType) {
        try {
            this.fileTypeSnippetMap = JsonUtils.jsonTo(FILE_TYPE_SNIPPET_MAP_IN_JSON, new TypeReference<>() {
            });
            if (log.isTraceEnabled())
                log.trace(StringUtils.join(this.fileTypeSnippetMap.get(SupportFileTypes.TYPE_MARKDOWN), ","));
        } catch (IOException e) {
            log.warn("failed to parse file type and snippet type mapping", e);
            this.fileTypeSnippetMap = new HashMap<>();
        }
        List<String> supportedTypes = this.fileTypeSnippetMap.get(fileType);
        if (log.isTraceEnabled()) log.trace(StringUtils.join(supportedTypes, ","));
        if (supportedTypes != null) {
            if (!supportedTypes.contains(Snippet.TYPE_IMAGE)) {
                hide(itemImage, rdTypeImage);
            }
        }
        vbox.getChildren().remove(itemImage); // Note: remove for switching between types
    }

    private Image loadImage(File file) {
        Image image = new Image(file.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(160);
        imageView.setFitHeight(160);
        btnIconImage.setText(StringUtils.EMPTY);
        btnIconImage.setGraphic(imageView);
        btnIconImage.setUserData(file.getAbsolutePath());
        return image;
    }

    private void updateSnippet(Image image) {
        if (image == null) {
            Snippet<?> snippet = new Snippet<>();
            snippet.title(tfTitle.getText()).code(taCode.getText()).type(Snippet.TYPE_TEXT).description(taDesc.getText());
            result = snippet;
        }
        else {
            ImageSnippet imageSnippet = new ImageSnippet(image);
            imageSnippet.title(tfTitle.getText()).code(taCode.getText()).type(Snippet.TYPE_IMAGE).description(taDesc.getText())
                    .filePath(String.valueOf(btnIconImage.getUserData()));
            result = imageSnippet;
        }
    }

}
