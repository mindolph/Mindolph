package com.mindolph.fx.view;

import com.mindolph.base.FontIconManager;
import com.mindolph.core.model.NodeData;
import com.mindolph.fx.event.DragFileEventHandler;
import com.mindolph.mfx.util.FontUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * A tree cell component for workspace tree,
 * supports drag and drops.
 *
 * @author mindolph.com@gmail.com
 */
public class WorkspaceViewCell extends TreeCell<NodeData> {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceViewCell.class);

    private static final Font defaultFont = Font.font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, Font.getDefault().getSize());
    private static final Font boldFont = FontUtils.newFontWithWeight(defaultFont, FontWeight.BOLD);

    // static for sharing.
    private static List<NodeData> draggingNodeDatas;

    private DragFileEventHandler dragFileEventHandler;
    private Background origBackground;// for drag&drop

    public WorkspaceViewCell() {
        this.setOnDragDetected(e -> {
            ObservableList<TreeItem<NodeData>> selectedItems = getTreeView().getSelectionModel().getSelectedItems();
            if (selectedItems == null || selectedItems.isEmpty()) return;
            draggingNodeDatas = selectedItems.stream().map(TreeItem::getValue).toList();
            log.trace("dragged %d files".formatted(draggingNodeDatas.size()));
            Dragboard dragboard = this.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            List<File> files = draggingNodeDatas.stream().map(NodeData::getFile).toList();
            content.putString(StringUtils.join(files, LINE_SEPARATOR));
            content.putFiles(files);
            dragboard.setContent(content);
            e.consume();
        });
        this.setOnDragOver(dragEvent -> {
            if (dragEvent.getDragboard().hasString()) {
                if (getTreeItemData() != null && (getTreeItemData().isFolder() || getTreeItemData().isWorkspace())) {
                    dragEvent.acceptTransferModes(TransferMode.MOVE);
                    log.trace("ok to drop");
                }
            }
            else {
                log.trace("no string");
            }
            dragEvent.consume();
        });
        this.setOnDragEntered(dragEvent -> {
            log.trace("drag entered: " + (this.getTreeItem() == null ? EMPTY : this.getTreeItem().getValue()));
            if (getTreeItemData() != null && (getTreeItemData().isFolder() || getTreeItemData().isWorkspace())) {
                origBackground = this.getBackground();
                this.setBorder(new Border(new BorderStroke(Color.DARKBLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            }
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!getTreeItem().isExpanded()) getTreeItem().setExpanded(true);
                }
            }, 1000);
            dragEvent.consume();
        });
        this.setOnDragExited(dragEvent -> {
            log.trace("drag exited");
            if (getTreeItemData() != null && (getTreeItemData().isFolder() || getTreeItemData().isWorkspace())) {
                this.setBorder(null);
            }
            dragEvent.consume();
        });
        this.setOnDragDropped(dragEvent -> {
            if (dragEvent.getDragboard().hasString()) {
                if (getTreeItemData() != null && (getTreeItemData().isFolder() || getTreeItemData().isWorkspace())) {
                    if (draggingNodeDatas != null && !draggingNodeDatas.isEmpty()) {
                        log.info(String.format("Drag %s and drop to %s", dragEvent.getDragboard().getString(), getTreeItemData().getFile().toString()));
                        dragFileEventHandler.onFilesDragged(draggingNodeDatas, getTreeItemData());
                    }
                    else {
                        log.debug("No dragging nodes");
                    }
                }
                else {
                    log.debug("Dropped on a non folder node");
                }
            }
            else {
                log.warn("no string");
            }
            dragEvent.consume();
        });
    }

    private NodeData getTreeItemData() {
        TreeItem<NodeData> ti = this.getTreeItem();
        if (ti != null && ti.getValue() != null) return ti.getValue();
        return null;
    }

    @Override
    protected void updateItem(NodeData item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
//            setFont(defaultFont);
            setGraphic(null);
        }
        else {
            setText(item.toString());
            if (item.isFile()) {
//                setFont(defaultFont);
                setGraphic(FontIconManager.getIns().getIconForFile(item));
            }
            else if (item.isFolder()) {
//                setFont(defaultFont);
                setGraphic(FontIconManager.getIns().getIconForFile(item));
            }
            else {
//                setFont(defaultFont);
                setGraphic(null);
                return; // no icon for this cell.
            }
        }
    }

    public void setDragFileEventHandler(DragFileEventHandler dragFileEventHandler) {
        this.dragFileEventHandler = dragFileEventHandler;
    }
}
