package com.mindolph.base.control.snippet;

import com.mindolph.base.event.EventBus;
import com.mindolph.base.util.LayoutUtils;
import com.mindolph.core.model.Snippet;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

/**
 * View for snippets that contains a ListView.
 *
 * @since 1.10
 */
public class ListSnippetView extends AnchorPane implements SnippetViewable<Snippet> {

    private ListView<Snippet> listView;

    public ListSnippetView() {
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
        this.getChildren().add(listView);
    }

    @Override
    public void setItems(ObservableList<Snippet> items) {
        listView.setItems(items);
    }

    @Override
    public ObservableList<Snippet> getItems() {
        return listView.getItems();
    }
}
