package com.mindolph.fx.view;

import com.mindolph.base.BaseView;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.MTreeView;
import com.mindolph.base.event.EventBus;
import com.mindolph.core.model.OutlineItemData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.collections.tree.Node;

import java.util.Optional;

/**
 * @see OutlineItemData
 * @since 1.9.0
 */
public class OutlineView extends BaseView {

    private static final Logger log = LoggerFactory.getLogger(OutlineView.class);

    @FXML
    private MTreeView<OutlineItemData> treeView;
    private final TreeItem<OutlineItemData> rootItem; // root node is not visible

    public OutlineView() {
        super("/view/outline_view.fxml");
        rootItem = new TreeItem<>(new OutlineItemData("Outline Stub"));
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);


        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeView.setCellFactory(tv -> {
            TreeCell<OutlineItemData> cell = new TreeCell<>() {
                @Override
                protected void updateItem(OutlineItemData item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    }
                    else {
                        setText(item.getName());
                        setGraphic(FontIconManager.getIns().getIcon(IconKey.WORKSPACE_TREE));
                    }
                }
            };
            cell.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 1) {
                    Optional<OutlineItemData> selectedData = treeView.getSelectedData();
                    selectedData.ifPresent(outlineItemData -> {
                        EventBus.getIns().notifyLocateInFile(outlineItemData.getAnchor());
                    });
                    mouseEvent.consume();
                }
            });
            return cell;
        });
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
        });

        EventBus.getIns().subscribeOutline(tree -> {
            // since the events are in threads.
            Platform.runLater(() -> {
                treeView.removeAll();
                if (tree != null && tree.getRootNode() != null) {
                    treeView.setRoot(rootItem);
                    loadOutlineTreeNode(rootItem, tree.getRootNode());
//                treeView.refresh();
                }
                else {
                    log.debug("No outline for this document");
                }
            });
        });


    }

    private void loadOutlineTreeNode(TreeItem<OutlineItemData> treeItem, Node parentNode) {
        for (Node node : parentNode.getChildren()) {
            TreeItem<OutlineItemData> childTreeItem = new TreeItem<>((OutlineItemData) node.getData());
            childTreeItem.setExpanded(true);
            treeItem.getChildren().add(childTreeItem);
            this.loadOutlineTreeNode(childTreeItem, node);
        }
    }
}
