package com.mindolph.fx.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.OpenFileEvent;
import com.mindolph.mfx.i18n.I18nHelper;
import com.mindolph.core.search.SearchParams;
import com.mindolph.fx.control.FileTreeView;
import com.mindolph.fx.control.FileTreeView.FileTreeViewData;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;

import java.io.File;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Related operations:
 * Rename a file or folder.
 * Delete a file or folder.
 * Move a file to another folder.
 *
 * @author mindolph.com@gmail.com
 * @since 1.3
 */
public class FileReferenceDialog extends BaseDialogController<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(FileReferenceDialog.class);

    private SearchParams searchParams;

    @FXML
    private FileTreeView fileTreeView;

    @FXML
    private Label lblMsg;

    private final TreeItem<FileTreeViewData> rootItem;

    private List<File> foundFiles;

    public FileReferenceDialog(SearchParams searchParams, List<File> foundFiles) {
        I18nHelper i18n = I18nHelper.getInstance();
        this.searchParams = searchParams;
        this.foundFiles = foundFiles;
        dialog = new CustomDialogBuilder<Boolean>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(i18n.get("dialog.file.reference", "File Reference"))
                .fxmlUri("dialog/file_reference_dialog.fxml")
                .buttons(ButtonType.YES, ButtonType.NO)
                .icon(ButtonType.YES, FontIconManager.getIns().getIcon(IconKey.YES))
                .icon(ButtonType.NO, FontIconManager.getIns().getIcon(IconKey.NO))
                .defaultValue(false)
                .resizable(true)
                .controller(this)
                .build();

        lblMsg.setText(i18n.get("file.reference.dialog.msg", searchParams.getKeywords()));

        rootItem = new TreeItem<>();
        rootItem.setExpanded(true);
        fileTreeView.setRoot(rootItem);
        fileTreeView.setShowRoot(false);
        fileTreeView.init(searchParams);
        fileTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<FileTreeViewData> selectedItem = fileTreeView.getSelectionModel().getSelectedItem();
                FileTreeViewData value = selectedItem.getValue();
                EventBus.getIns().notifyOpenFile(new OpenFileEvent(value.getFile(), true, searchParams));
                dialog.close();
                event.consume();
            }
        });

        rootItem.getChildren().clear();
        for (File foundFile : foundFiles) {
            TreeItem<FileTreeViewData> item = new TreeItem<>(new FileTreeViewData(true, foundFile));
            rootItem.getChildren().add(item);
        }

        this.result = true;
    }
}
