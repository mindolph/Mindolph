package com.mindolph.base.container;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 * @deprecated
 */
public class HiddenSplitPaneDemo implements Initializable {

    @FXML
    private HiddenSplitPane hiddenSplitPane;

    @FXML
    private HBox hBox;

    @FXML
    private TreeView<String> treeView;
    private TreeItem<String> rootItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Border border = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
                new CornerRadii(1), BorderStroke.THIN));
        for (int i = 0; i < 10; i++) {
            Label label = new Label("" + i);
            label.setPrefWidth(100);
            label.setMinWidth(100);
            label.setMaxWidth(100);
            label.setBorder(border);
            hBox.getChildren().add(label);
        }
        this.rootItem = new TreeItem<>("root item");
        this.rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setCellFactory(new Callback<>() {
            @Override
            public TreeCell<String> call(TreeView<String> param) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        }
                        else {
                            setText(item);
                        }
                    }
                };
            }
        });
        for (int i = 0; i < 50; i++) {
            TreeItem<String> item = new TreeItem<>("Item" + i);
            rootItem.getChildren().add(item);
        }

        hiddenSplitPane.fixedPartialSizeProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("## event: partial size to : " + newValue);
            }
        });

        // init the partial size to avoid the default value 0.5 ?
        hiddenSplitPane.setFixedPartial(HiddenSplitPaneSkin.Partial.PRIMARY);
        Platform.runLater(() -> {
            hiddenSplitPane.setFixedPartialSize(60);
        });

    }

    @FXML
    public void onHideLeft(ActionEvent event) {
        hiddenSplitPane.hidePrimary();
    }

    @FXML
    public void onHideRight(ActionEvent event) {
        hiddenSplitPane.hideSecondary();
    }

    @FXML
    public void onShowAll(ActionEvent event) {
        hiddenSplitPane.showAll();
    }

    @FXML
    public void onToggleOrientation(ActionEvent event) {
        hiddenSplitPane.toggleOrientation();
    }

    @FXML
    public void onFixedPartialLeft(ActionEvent event) {
        hiddenSplitPane.setFixedPartialSize(100);
        hiddenSplitPane.setFixedPartial(HiddenSplitPaneSkin.Partial.PRIMARY);
    }

    @FXML
    public void onFixedPartialRight(ActionEvent event) {
        hiddenSplitPane.setFixedPartialSize(100);
        hiddenSplitPane.setFixedPartial(HiddenSplitPaneSkin.Partial.SECONDARY);
    }

}
