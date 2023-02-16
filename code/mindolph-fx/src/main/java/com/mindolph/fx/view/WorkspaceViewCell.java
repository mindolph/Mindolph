package com.mindolph.fx.view;

import com.mindolph.core.model.NodeData;
import com.mindolph.fx.IconManager;
import com.mindolph.fx.constant.IconName;
import com.mindolph.fx.event.DragFileEventHandler;
import com.mindolph.mfx.util.FontUtils;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

import java.util.List;

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

    private final ImageView iconView = new ImageView();

    private DragFileEventHandler dragFileEventHandler;

    private static NodeData draggingNodeData;

    private Background origBackground;// for drag&drop

    public WorkspaceViewCell() {
        iconView.setFitWidth(16);
        iconView.setFitHeight(16);
        this.setOnDragDetected(e -> {
            TreeItem<NodeData> draggedItem = this.getTreeItem();
            if (draggedItem == null) return;
            NodeData value = draggedItem.getValue();
            this.draggingNodeData = value;
            log.info("dragged: " + value.getFile());
            Dragboard dragboard = this.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString(value.getFile().toString());
            dragboard.setContent(content);
            e.consume();
        });
        this.setOnDragOver(dragEvent -> {
            if (dragEvent.getDragboard().hasString()) {
                if (getTreeItemData() != null && (getTreeItemData().isFolder() || getTreeItemData().isWorkspace())) {
                    dragEvent.acceptTransferModes(TransferMode.MOVE);
                    log.trace("ok to drop");
                }
            } else {
                log.debug("no string");
            }
            dragEvent.consume();
        });
        this.setOnDragEntered(dragEvent -> {
            log.debug("drag entered: " + this.getTreeItem().getValue());
            if (getTreeItemData() != null && (getTreeItemData().isFolder() || getTreeItemData().isWorkspace())) {
                origBackground = this.getBackground();
                this.setBorder(new Border(new BorderStroke(Color.DARKBLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            }
            dragEvent.consume();
        });
        this.setOnDragExited(dragEvent -> {
            log.debug("drag exited");
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
                    } else {
                        log.debug("No dragging node");
                    }
                } else {
                    log.debug("Dropped on a non folder node");
                }
            } else {
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
        } else {
            setText(item.toString());
            Image icon = null;
            if (item.isFile()) {
//                setFont(defaultFont);
                icon = IconManager.getInstance().getFileIcon(item);
            } else if (item.isFolder()) {
//                setFont(defaultFont);
                icon = IconManager.getInstance().getIcon(IconName.FOLDER);
            } else if (item.isWorkspace()) {
//                setFont(boldFont);
                icon = IconManager.getInstance().getIcon(IconName.WORKSPACE);
            } else {
//                setFont(defaultFont);
                setGraphic(null);
                return; // no icon for this cell.
            }
            iconView.setImage(icon);
            setGraphic(iconView);
        }
    }

    public void setDragFileEventHandler(DragFileEventHandler dragFileEventHandler) {
        this.dragFileEventHandler = dragFileEventHandler;
    }
}
