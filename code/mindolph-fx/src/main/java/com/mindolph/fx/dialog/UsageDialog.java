package com.mindolph.fx.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.core.search.SearchParams;
import com.mindolph.core.search.SearchService;
import com.mindolph.fx.control.FileTreeView;
import com.mindolph.fx.control.FileTreeView.FileTreeViewData;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.AsyncUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
public class UsageDialog extends BaseDialogController<SearchParams> {

    private final Logger log = LoggerFactory.getLogger(UsageDialog.class);

    private SearchParams searchParams;

    @FXML
    private FileTreeView fileTreeView;

    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label lblMsg;

    private final TreeItem<FileTreeViewData> rootItem;

    private List<File> foundFiles;

    public UsageDialog(SearchParams searchParams) {
        this.searchParams = searchParams;
        dialog = new CustomDialogBuilder<SearchParams>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("File Usage")
                .fxmlUri("dialog/usage_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .defaultValue(null)
                .resizable(true)
                .controller(this)
                .build();

        rootItem = new TreeItem<>();
        rootItem.setExpanded(true);
        fileTreeView.setRoot(rootItem);
        fileTreeView.setShowRoot(false);
        fileTreeView.init(searchParams);
        reSearch();
    }

    private void reSearch() {
        log.debug("reSearch()");
        progressIndicator.setVisible(true);
        IOFileFilter newFileFilter = searchParams.getSearchFilter();
        AsyncUtils.fxAsync(() -> {
            foundFiles = SearchService.getIns().searchLinksInFilesIn(searchParams.getSearchInDir(), newFileFilter, searchParams);
        }, () -> {
            lblMsg.setText("Found %d files are using this file %s".formatted(foundFiles.size(), searchParams.getKeywords()));
            progressIndicator.setVisible(false);
            rootItem.getChildren().clear();
            for (File foundFile : foundFiles) {
                TreeItem<FileTreeViewData> item = new TreeItem<>(new FileTreeViewData(true, foundFile));
//                item.setValue(new FileTreeViewData(true, foundFile));
                rootItem.getChildren().add(item);
            }

        });
    }

}
