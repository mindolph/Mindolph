package com.mindolph.base.control.snippet;

import com.mindolph.core.model.Snippet;
import javafx.collections.ObservableList;

/**
 *
 * @param <T>
 */
public interface SnippetViewable<T extends Snippet> {

    void setItems(ObservableList<T> items);

    ObservableList<T> getItems();
}
