package com.mindolph.fx.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.OpenFileEvent;
import com.mindolph.mfx.i18n.I18nHelper;
import com.mindolph.core.search.SearchParams;
import com.mindolph.core.search.SearchService;
import com.mindolph.fx.control.FileTreeView;
import com.mindolph.fx.control.FileTreeView.FileTreeViewData;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.AsyncUtils;

import java.io.File;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindolph.com@gmail.com
 */
public class UsageDialog extends BaseDialogController<SearchParams> {

    private static final Logger log = LoggerFactory.getLogger(UsageDialog.class);

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
        I18nHelper i18n = I18nHelper.getInstance();
        this.searchParams = searchParams;
        dialog = new CustomDialogBuilder<SearchParams>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(i18n.get("dialog.usage", "File Usage"))
                .fxmlUri("dialog/usage_dialog.fxml")
                .buttons(ButtonType.CLOSE)
                .icon(ButtonType.CLOSE, FontIconManager.getIns().getIcon(IconKey.CLOSE))
                .defaultValue(null)
                .resizable(true)
                .controller(this)
                .build();

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
        reSearch();
    }

    private void reSearch() {
        log.debug("reSearch()");
        progressIndicator.setVisible(true);
        IOFileFilter newFileFilter = searchParams.getSearchFilter();
        I18nHelper i18n = I18nHelper.getInstance();
        AsyncUtils.fxAsync(
                () -> {
                    foundFiles = SearchService.getIns().searchLinksInFilesIn(searchParams.getSearchInDir(), newFileFilter, searchParams);
                },
                () -> {
                    lblMsg.setText(i18n.get("usage.dialog.found", foundFiles.size(), searchParams.getKeywords()));
                    progressIndicator.setVisible(false);
                    rootItem.getChildren().clear();
                    for (File foundFile : foundFiles) {
                        TreeItem<FileTreeViewData> item = new TreeItem<>(new FileTreeViewData(true, foundFile));
                        rootItem.getChildren().add(item);
                    }
                }
        );
    }
}
