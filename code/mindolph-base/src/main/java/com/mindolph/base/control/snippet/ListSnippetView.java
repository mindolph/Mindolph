package com.mindolph.base.control.snippet;

import com.mindolph.base.dialog.SnippetDialog;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.util.LayoutUtils;
import com.mindolph.core.AppManager;
import com.mindolph.core.model.Snippet;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.function.Consumer;

/**
 * View for snippets that contains a ListView.
 *
 * @since 1.10
 */
public class ListSnippetView extends AnchorPane implements SnippetViewable<Snippet>, EventHandler<ActionEvent> {

    private static final Logger log = LoggerFactory.getLogger(ListSnippetView.class);

    private final AppManager appManager = AppManager.getInstance();

    private final BooleanProperty editableProperty = new SimpleBooleanProperty(false);

    private final ListView<Snippet> listView;

    private final MenuItem miNew = new MenuItem("New Snippet");
    private final MenuItem miEdit = new MenuItem("Edit Snippet");
    private final MenuItem miRemove = new MenuItem("Remove Snippet");

    // event to SnippetView after snippet changes
    private final EventSource<Snippet> snippetChanged = new EventSource<>();

    // used for custom snippet to load data for file type.
    private final String fileType;

    public ListSnippetView(String fileType) {
        this.fileType = fileType;
        this.listView = new ListView<>();
        LayoutUtils.anchor(this.listView, 0);
        this.listView.setCellFactory(param -> new SnippetCell());
        this.listView.setPrefHeight(9999); // extend the snippet view as possible
        this.listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Snippet selectedSnippet = listView.getSelectionModel().getSelectedItem();
                EventBus.getIns().notifySnippetApply(selectedSnippet);
            }
        });
        this.editableProperty.addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                ContextMenu contextMenu = new ContextMenu();
                miNew.setOnAction(this);
                miEdit.setOnAction(this);
                miRemove.setOnAction(this);
                contextMenu.getItems().addAll(miNew, miEdit, miRemove);
                this.listView.setContextMenu(contextMenu);
            }
        });
        this.getChildren().add(listView);
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.miNew) {
            SnippetDialog snippetDialog = new SnippetDialog(fileType, null);// TODO
            Snippet<?> snippet = snippetDialog.showAndWait();
            if (snippet != null) {
                log.info("Save new snippet '%s'".formatted(snippet.getTitle()));
                appManager.saveSnippet(fileType, "text", Collections.singletonList(snippet), false);
                snippetChanged.push(null);
            }
        }
        else if (actionEvent.getSource() == this.miEdit) {
            Snippet selectedItem = listView.getSelectionModel().getSelectedItem();
            SnippetDialog snippetDialog = new SnippetDialog(fileType, selectedItem);// TODO
            Snippet<?> snippet = snippetDialog.showAndWait();
            if (snippet != null) {
                log.info("Save edited snippet '%s'".formatted(snippet.getTitle()));
                appManager.saveSnippet(fileType, "text", Collections.singletonList(snippet), true);
                snippetChanged.push(null);
            }
        }
        else if (actionEvent.getSource() == this.miRemove) {
            Snippet selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                log.info("Remove snippet '%s'".formatted(selectedItem.getTitle()));
                appManager.deleteSnippets(fileType, Collections.singletonList(selectedItem));
                snippetChanged.push(null);
            }
        }
    }

    public void subscribeSnippetChanged(Consumer<Snippet> consumer) {
        this.snippetChanged.subscribe(consumer);
    }

    @Override
    public void setItems(ObservableList<Snippet> items) {
        listView.setItems(items);
    }

    @Override
    public ObservableList<Snippet> getItems() {
        return listView.getItems();
    }

    public boolean isEditableProperty() {
        return editableProperty.get();
    }

    public BooleanProperty editablePropertyProperty() {
        return editableProperty;
    }

    public void setEditable(boolean editable) {
        this.editableProperty.set(editable);
    }

}
