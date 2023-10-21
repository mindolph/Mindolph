package com.mindolph.mindmap.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.util.MindolphFileUtils;
import com.mindolph.core.util.FileNameUtils;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mindmap.model.FileLink;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static com.mindolph.core.constant.SceneStatePrefs.MINDOLPH_MMD_FILE_LINK_IS_OPEN_IN_SYS;
import static com.mindolph.core.constant.SceneStatePrefs.MINDOLPH_MMD_FILE_LINK_LAST_FOLDER;

/**
 * @author mindolph.com@gmail.com
 */
public class FileLinkDialog extends BaseDialogController<FileLink> {

    private static final Logger log = LoggerFactory.getLogger(FileLinkDialog.class);

    @FXML
    private TextField textField;
    @FXML
    private CheckBox checkBox; // is open in system
    @FXML
    private Button btnOpen;
    @FXML
    private Button btnClear;

    public FileLinkDialog(String title, File workspaceDir, FileLink initFileLink) {
        super(initFileLink);
        dialog = new CustomDialogBuilder<FileLink>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(title, 20)
                .fxmlUri("dialog/file_link_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .defaultValue(initFileLink)
                .resizable(true)
                .controller(FileLinkDialog.this)
                .build();

        dialog.setOnShown(event -> Platform.runLater(() -> textField.requestFocus()));

        dialog.setOnCloseRequest(dialogEvent -> {
            if (!confirmClosing("Link has been changed, are you sure to close the dialog")) {
                dialogEvent.consume();
            }
        });
        // init
        String filePath;
        Boolean isOpenInSystem;
        if (initFileLink == null) {
            filePath = FxPreferences.getInstance().getPreference(MINDOLPH_MMD_FILE_LINK_LAST_FOLDER, String.class);
            isOpenInSystem = FxPreferences.getInstance().getPreference(MINDOLPH_MMD_FILE_LINK_IS_OPEN_IN_SYS, Boolean.class);
        }
        else {
            filePath = initFileLink.getFilePathWithLine().getPath();
            isOpenInSystem = initFileLink.isShowWithSystemTool();
        }

        btnClear.setGraphic(FontIconManager.getIns().getIcon(IconKey.CLEAR));
        btnOpen.setGraphic(FontIconManager.getIns().getIcon(IconKey.OPEN));
        // listeners
        btnOpen.setOnAction(event -> {
            File initDir;
            if (initFileLink != null && initFileLink.getFilePathWithLine().getPath() != null) {
                initDir = new File(initFileLink.getFilePathWithLine().getPath());
                if (initDir.isFile()) {
                    initDir = initDir.getParentFile();
                }
                else if (workspaceDir != null) {
                    initDir = workspaceDir;
                }
                else {
                    initDir = SystemUtils.getUserHome();
                }
            }
            else if (StringUtils.isNotBlank(filePath)) {
                // from last used folder path
                File f = new File(filePath);
                if (f.isDirectory()) {
                    initDir = f;
                }
                else if (workspaceDir != null) {
                    initDir = workspaceDir;
                }
                else {
                    initDir = SystemUtils.getUserHome();
                }
            }
            else {
                initDir = SystemUtils.getUserHome();
            }
            log.debug("Open dir: " + initDir);
            File file = DialogFactory.openFileDialog(((Node) event.getSource()).getScene().getWindow(), initDir);
            if (file != null) {
                String finalPath = file.getPath();
                if (MindolphFileUtils.isOpenInternally(filePath)) {
                    checkBox.setSelected(false);
                }
                try {
                    boolean isRelevantPath = workspaceDir != null && FileUtils.directoryContains(workspaceDir, file);
                    if (isRelevantPath) {
                        finalPath = FileNameUtils.getRelativePath(file, workspaceDir);
                    }
                    log.debug("Got final path: " + finalPath);
                    textField.setText(finalPath);
                    result = new FileLink(finalPath, checkBox.isSelected());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        checkBox.setSelected(isOpenInSystem);
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> result = new FileLink(textField.getText(), newValue));
        textField.setText(filePath);
        textField.textProperty().addListener((observable, oldValue, newValue) -> result = new FileLink(newValue, checkBox.isSelected()));
    }

    @FXML
    public void onBtnClear(ActionEvent event) {
        textField.clear();
    }

}
