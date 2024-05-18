package com.mindolph.fx.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.DirBreadCrumb;
import com.mindolph.core.search.SearchParams;
import com.mindolph.fx.control.FileFilterButtonGroup;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.preference.FxPreferences;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.PathUtils;

import java.io.File;

import static com.mindolph.core.constant.SceneStatePrefs.*;

/**
 * @author mindolph.com@gmail.com
 */
public class FindInFilesDialog extends BaseDialogController<SearchParams> {
    private static final Logger log = LoggerFactory.getLogger(FindInFilesDialog.class);

    @FXML
    private Label lbDesc;
    private TextField tfKeywords;
    @FXML
    private HBox hbKeywords;
    @FXML
    private ToggleButton tbCase;
    @FXML
    private FileFilterButtonGroup fileFilterButtonGroup;
    @FXML
    private DirBreadCrumb bcbDirPath;

    public FindInFilesDialog(File workspaceDir, File dir) {
        dialog = new CustomDialogBuilder<SearchParams>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Find in Files")
                .fxmlUri("dialog/find_in_files_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .defaultValue(null)
                .controller(this)
                .build();
        dialog.setOnShown(event -> Platform.runLater(() -> tfKeywords.requestFocus()));

//        String workspaceName = FilenameUtils.getBaseName(workspaceDir.getPath());
//        String relativePath = PathUtils.getRelativePath(dir, workspaceDir);
//        lbDesc.setText("%s%s%s".formatted(workspaceName, File.separator, relativePath));

        Object lastKeyword = FxPreferences.getInstance().getPreference(MINDOLPH_FIND_FILES_KEYWORD, "");
        Boolean lastCaseSensitivity = FxPreferences.getInstance().getPreference(MINDOLPH_FIND_FILES_CASE_SENSITIVITY, Boolean.FALSE);
        Object lastFileTypeName = FxPreferences.getInstance().getPreference(MINDOLPH_FIND_FILES_OPTIONS, FileFilterButtonGroup.FILE_OPTION_ALL);

        result = new SearchParams(String.valueOf(lastKeyword), lastCaseSensitivity);
        result.setFileTypeName(String.valueOf(lastFileTypeName));

        bcbDirPath.selectedCrumbProperty().addListener((observable, oldValue, newValue) -> {
            DirBreadCrumb.Crumb value = newValue.getValue();
            log.debug("move to dir: %s".formatted(value.path()));
            result.setSearchInDir(value.path().toFile());
        });
        bcbDirPath.init(workspaceDir, dir);

        tfKeywords = TextFields.createClearableTextField();
        tfKeywords.setPrefWidth(280);
        hbKeywords.getChildren().add(tfKeywords);
        tfKeywords.textProperty().addListener((observableValue, s, newKeyword) -> {
            result.setKeywords(newKeyword);
        });
        tfKeywords.setText(String.valueOf(lastKeyword));

        tbCase.setGraphic(FontIconManager.getIns().getIcon(IconKey.CASE_SENSITIVITY));
        tbCase.selectedProperty().addListener((observableValue, aBoolean, isSelected) -> {
            result.setCaseSensitive(isSelected);
        });
        tbCase.setSelected(lastCaseSensitivity);

        fileFilterButtonGroup.selectedFileTypeProperty().addListener((observableValue, s, fileTypeName) -> result.setFileTypeName(fileTypeName));
        fileFilterButtonGroup.setSelectedFileType(String.valueOf(lastFileTypeName));
        fileFilterButtonGroup.setAllowFolder(false);
    }

}
