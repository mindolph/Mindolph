package com.mindolph.base.container;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.commons.lang3.RandomUtils;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 */
public class FixedSplitPaneDemo implements Initializable {

    @FXML
    private FixedSplitPane fixedSplitPane;

    @FXML
    private HBox hBox;

    @FXML
    private TreeView<String> treeView;
    private TreeItem<String> rootItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (RandomUtils.nextBoolean()) {
            fixedSplitPane.setFixed(fixedSplitPane.getPrimary());
            fixedSplitPane.setFixedSize(150);
        }
        fixedSplitPane.skinProperty().addListener((observable, oldValue, newValue) -> {
            fixedSplitPane.setOrientation(Orientation.VERTICAL); // this doesn't work
        });

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

//        fixedSplitPane.fixedPartialSizeProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                System.out.println("## event: partial size to : " + newValue);
//            }
//        });
//
//        // init the partial size to avoid the default value 0.5 ?
//        fixedSplitPane.setFixedPartial(HiddenSplitPaneSkin.Partial.PRIMARY);
//        Platform.runLater(() -> {
//            fixedSplitPane.setFixedPartialSize(60);
//        });

    }

    @FXML
    public void onHidePrimary() {
        fixedSplitPane.hidePrimary();
    }

    @FXML
    public void onHideSecondary() {
        fixedSplitPane.hideSecondary();
    }

    @FXML
    public void onShowAll() {
        fixedSplitPane.showAll();
    }

    @FXML
    public void onToggleOrientation() {
        fixedSplitPane.toggleOrientation();
    }

    @FXML
    public void onNoFixed() {
        fixedSplitPane.setFixed(null);
    }

    @FXML
    public void onPrimaryFixed() {
        fixedSplitPane.setFixedSize(250);
        fixedSplitPane.setFixed(fixedSplitPane.getPrimary());
    }

    @FXML
    public void onSecondaryFixed() {
        fixedSplitPane.setFixedSize(250);
        fixedSplitPane.setFixed(fixedSplitPane.getSecondary());
    }

    @FXML
    public void onSetFixedSize() {
        fixedSplitPane.setFixedSize(100);
    }

    @FXML
    public void onCenterSplitter() {
        fixedSplitPane.setSplitterPosition(0.5);
    }

}
