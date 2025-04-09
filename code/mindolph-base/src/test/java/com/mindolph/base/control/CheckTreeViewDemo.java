package com.mindolph.base.control;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBoxTreeItem;
import org.controlsfx.control.CheckTreeView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 */
public class CheckTreeViewDemo implements Initializable {

    @FXML
    private CheckTreeView checkTreeView;

    private CheckBoxTreeItem<String> rootItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootItem = new CheckBoxTreeItem<>("ROOT");
        rootItem.setExpanded(true);
        new Thread(() -> {
            CheckBoxTreeItem<String> subItem = new CheckBoxTreeItem<>("SUB");
            rootItem.getChildren().add(subItem);
            checkTreeView.setRoot(rootItem);
        }).start();

        Platform.runLater(() -> {
            checkTreeView.getCheckModel().getCheckedItems().addListener((ListChangeListener<CheckBoxTreeItem<String>>) c -> {
                System.out.println("Check items changed");
                while (c.next()) {
                    System.out.println("added %d, removed %d".formatted(c.getAddedSize(), c.getRemovedSize()));
                }
            });
            checkTreeView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<CheckBoxTreeItem<String>>) c -> {
                System.out.println("Selected items changed");
            });
        });


    }
}
