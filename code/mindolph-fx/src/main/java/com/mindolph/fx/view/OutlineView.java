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
import org.swiftboot.collections.tree.Tree;

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

    // data tree for outline of one file.
    private Tree tree;

    public OutlineView() {
        super("/view/outline_view.fxml", false);
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
                        setGraphic(FontIconManager.getIns().getIcon(IconKey.OUTLINE_ITEM));
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

        EventBus.getIns().subscribeOutline(tree -> {
            this.tree = tree;
            this.refresh();
        });

        super.activeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.refresh();
            }
        });

    }

    private void refresh() {
        // run later since the events are emitted in threads.
        if (!super.getActive()) return; // avoid unnecessary refreshing.
        Platform.runLater(() -> {
            treeView.removeAll();
            if (tree != null && tree.getRootNode() != null) {
                treeView.setRoot(rootItem);
                loadOutlineTreeNode(rootItem, tree.getRootNode());
            }
            else {
                log.debug("No outline for this document");
            }
        });
    }

    /**
     * Recursive load tree node.
     *
     * @param treeItem
     * @param parentNode
     */
    private void loadOutlineTreeNode(TreeItem<OutlineItemData> treeItem, Node parentNode) {
        if (log.isTraceEnabled()) log.trace("Load items of a tree node to tree view node");
        for (Node node : parentNode.getChildren()) {
            TreeItem<OutlineItemData> childTreeItem = new TreeItem<>((OutlineItemData) node.getData());
            childTreeItem.setExpanded(true);
            treeItem.getChildren().add(childTreeItem);
            this.loadOutlineTreeNode(childTreeItem, node);
        }
    }
}
