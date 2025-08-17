package com.mindolph.base.control;

import com.mindolph.core.async.GlobalExecutor;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBoxTreeItem;
import org.controlsfx.control.CheckTreeView;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Future;

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

        Future<?> future = GlobalExecutor.submit(() -> {
            CheckBoxTreeItem<String> subItema1 = new CheckBoxTreeItem<>("A-1");
            CheckBoxTreeItem<String> subItema2 = new CheckBoxTreeItem<>("A-2");
            CheckBoxTreeItem<String> subItema3 = new CheckBoxTreeItem<>("A-3");
            CheckBoxTreeItem<String> subMenu = new CheckBoxTreeItem<>("B");
            subMenu.setExpanded(true);
            CheckBoxTreeItem<String> subItemb1 = new CheckBoxTreeItem<>("B-1");
            CheckBoxTreeItem<String> subItemb2 = new CheckBoxTreeItem<>("B-2");
            subMenu.getChildren().addAll(subItemb1, subItemb2);
            rootItem.getChildren().addAll(subItema1, subItema2, subItema3, subMenu);
            checkTreeView.setRoot(rootItem);
        });


        Platform.runLater(() -> {
            checkTreeView.getCheckModel().getCheckedItems().addListener((ListChangeListener<CheckBoxTreeItem<String>>) c -> {
                System.out.println("== Check items changed ==");
                while (c.next()) {
                    System.out.printf("from %d to %d%n", c.getFrom(), c.getTo());
                    System.out.println("Add: " + c.getAddedSize());
                    c.getAddedSubList().forEach(System.out::println);
                    System.out.println("Remove: " + c.getRemovedSize());
                    c.getRemoved().forEach(System.out::println);
                    System.out.println("All: " + c.getList().size());
                    c.getList().forEach(System.out::println);
                    System.out.println("--------");
                }
            });
            checkTreeView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<CheckBoxTreeItem<String>>) c -> {
                System.out.println("Selected items changed");
            });
        });


    }
}
