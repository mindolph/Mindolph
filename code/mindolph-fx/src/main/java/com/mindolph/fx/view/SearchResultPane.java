package com.mindolph.fx.view;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.OpenFileEvent;
import com.mindolph.core.model.NodeData;
import com.mindolph.core.search.FoundFile;
import com.mindolph.core.search.MatchedItem;
import com.mindolph.core.search.SearchParams;
import com.mindolph.core.search.SearchService;
import com.mindolph.fx.control.FileFilterButtonGroup;
import com.mindolph.fx.control.FileTreeView;
import com.mindolph.fx.control.FileTreeView.FileTreeViewData;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mfx.util.AsyncUtils;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.mindolph.core.constant.SceneStatePrefs.MINDOLPH_FIND_FILES_KEYWORD;

/**
 * Panel for "Find in Files".
 *
 * @author mindolph.com@gmail.com
 */
public class SearchResultPane extends AnchorPane {

    private static final Logger log = LoggerFactory.getLogger(SearchResultPane.class);

    @FXML
    private Label label;

    @FXML
    private TextField tfKeywords;

    @FXML
    private ToggleButton tbCase;

    @FXML
    private Button btnSearch;

    @FXML
    private FileTreeView treeView;
    @FXML
    private FileFilterButtonGroup fileFilterButtonGroup;
    @FXML
    private ProgressIndicator progressIndicator;

    private final TreeItem<FileTreeViewData> rootItem;

    private SearchParams searchParams;

    private List<FoundFile> foundFiles;

    public SearchResultPane() {
        FxmlUtils.loadUri("/view/search_result_pane.fxml", this);
        rootItem = new TreeItem<>(null);
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);
        treeView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                TreeItem<FileTreeViewData> selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    FileTreeViewData data = selectedItem.getValue();
                    if (!data.isParent()) {
                        NodeData nodeData = new NodeData(data.getFile());
                        nodeData.setAnchor(data.getMatchedItem().getAnchor());
                        EventBus.getIns().notifyOpenFile(new OpenFileEvent(nodeData, true));
                    }
                }
            }
        });
        treeView.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                TreeItem<FileTreeViewData> selectedItem = treeView.getSelectionModel().getSelectedItem();
                FileTreeViewData file = selectedItem.getValue();
                EventBus.getIns().notifyOpenFile(new OpenFileEvent(file.getFile(), true, searchParams));
            }
        });

        tfKeywords.textProperty().addListener((observableValue, s, t1) -> searchParams.setKeywords(t1));
        tfKeywords.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                this.reSearch();
            }
        });
        tbCase.setGraphic(FontIconManager.getIns().getIcon(IconKey.CASE_SENSITIVITY));
        btnSearch.setOnAction(event -> {
            this.reSearch();
        });
        Platform.runLater(() -> {
            tfKeywords.requestFocus();
        });
    }

    public void init(SearchParams searchParams) {
        this.searchParams = searchParams;
        treeView.init(searchParams);
        tfKeywords.setText(searchParams.getKeywords());
        tbCase.setSelected(searchParams.isCaseSensitive());
        fileFilterButtonGroup.setSelectedFileType(searchParams.getFileTypeName());
        // start searching
        this.reSearch();
        // add listeners after search completed.
        tbCase.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            log.debug("Case sensitive option changed to: %s".formatted(t1));
            searchParams.setCaseSensitive(t1);
            this.reSearch();
        });
        fileFilterButtonGroup.selectedFileTypeProperty().addListener((observableValue, s, fileTypeName) -> {
            log.debug("File type changed to: %s".formatted(fileTypeName));
            // may be filtering searched files instead of research again for file type switching? TODO
            searchParams.setFileTypeName(fileFilterButtonGroup.getSelectedFileType());
            reSearch();
        });
    }

    /**
     * Do the searching asynchronously.
     */
    private void reSearch() {
        log.debug("reSearch()");
        String keyword = searchParams.getKeywords();
        if (StringUtils.isBlank(keyword)) {
            return;
        }
        progressIndicator.setVisible(true);
        IOFileFilter newFileFilter = searchParams.getSearchFilter();
        AsyncUtils.fxAsync(() -> {
            foundFiles = SearchService.getIns().searchInFilesIn(searchParams.getSearchInDir(), newFileFilter, searchParams);
        }, () -> {
            progressIndicator.setVisible(false);
            FxPreferences.getInstance().savePreference(MINDOLPH_FIND_FILES_KEYWORD, keyword);
            this.updateSearchResult();
        });
    }

    private void updateSearchResult() {
        label.setText("Found %d files in folder %s".formatted(foundFiles.size(), searchParams.getSearchInDir()));
        rootItem.getChildren().clear();
        for (FoundFile foundFile : foundFiles) {
            TreeItem<FileTreeViewData> item = new TreeItem<>(new FileTreeViewData(true, foundFile.getFile()));
            item.setExpanded(true);
//            item.setValue(file);
            rootItem.getChildren().add(item);
            if (CollectionUtils.isNotEmpty(foundFile.getInfos())) {
                for (MatchedItem info : foundFile.getInfos()) {
                    TreeItem<FileTreeViewData> infoNode = new TreeItem<>(new FileTreeViewData(false, foundFile.getFile(), info));
                    item.getChildren().add(infoNode);
                }
            }
        }
    }

}
