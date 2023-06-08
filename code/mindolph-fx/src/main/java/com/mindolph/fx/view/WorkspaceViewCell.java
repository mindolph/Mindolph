package com.mindolph.fx.view;

import com.mindolph.base.FontIconManager;
import com.mindolph.core.model.NodeData;
import com.mindolph.fx.event.DragFileEventHandler;
import com.mindolph.mfx.util.FontUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

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

    private DragFileEventHandler dragFileEventHandler;

    private static NodeData draggingNodeData;

    private Background origBackground;// for drag&drop

    public WorkspaceViewCell() {
        this.setOnDragDetected(e -> {
            TreeItem<NodeData> draggedItem = this.getTreeItem();
            if (draggedItem == null) return;
            NodeData value = draggedItem.getValue();
            this.draggingNodeData = value;
            log.trace("dragged: " + value.getFile());
            Dragboard dragboard = this.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString(value.getFile().toString());
            content.putFiles(Collections.singletonList(value.getFile()));
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
                    if (draggingNodeData != null) {
                        log.info(String.format("Drag %s and drop to %s", dragEvent.getDragboard().getString(), getTreeItemData().getFile().toString()));
                        dragFileEventHandler.onFilesDragged(List.of(draggingNodeData), getTreeItemData());
                    }
                    else {
                        log.debug("No dragging node");
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
