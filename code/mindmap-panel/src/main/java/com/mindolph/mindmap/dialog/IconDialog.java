package com.mindolph.mindmap.dialog;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mindmap.icon.EmoticonService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.swiftboot.collections.Matrix;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mindolph.com@gmail.com
 */
public class IconDialog extends BaseDialogController<String> implements EventHandler<ActionEvent> {

    private final int MAX_COL_SIZE = 8;

    @FXML
    private TableView<List<String>> tableView;

    @FXML
    private HBox hbRecentIcons;

    @FXML
    private TextField tfKeyword;

    @FXML
    private Button btnClear;

    // for unselect
    private ToggleButton selectedButton;

    private Set<String> recentIconNames;

    public IconDialog(String iconName) {
        super(iconName);
        origin = iconName;
        dialog = new CustomDialogBuilder<String>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Emoticons")
                .fxmlUri("dialog/icon_dialog.fxml")
                .defaultValue(iconName)
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .controller(IconDialog.this)
                .build();

        dialog.setOnCloseRequest(dialogEvent -> {
            if (!confirmClosing("Icon has benn changed, are you sure to close the dialog?")) {
                dialogEvent.consume();
            }
        });
        btnClear.setGraphic(FontIconManager.getIns().getIcon(IconKey.CLOSE));
        btnClear.setOnAction(event -> {
            tfKeyword.setText(null);
        });
        tfKeyword.textProperty().addListener((observable, oldValue, newValue) -> {
            updateIcons(newValue);
            btnClear.setDisable(StringUtils.isEmpty(newValue));
        });
        tableView.setSelectionModel(null); // let only buttons can be selected.
        recentIconNames = FxPreferences.getInstance().getPreference(PrefConstants.PREF_KEY_MMD_RECENT_ICONS, new LinkedHashSet<>());
        this.initIcons();
        Platform.runLater(() -> {
            tfKeyword.requestFocus();
        });
    }


    Callback<TableColumn.CellDataFeatures<List<String>, ToggleButton>, ObservableValue<ToggleButton>> cellValueFactory = param -> {
        List<String> row = param.getValue();
        TableColumn<List<String>, ToggleButton> column = param.getTableColumn();
        int cols = tableView.getColumns().indexOf(column);
        if (cols >= row.size()) {
            return null;
        }
        String name = row.get(cols);
        Image image = EmoticonService.getInstance().getIcon(name);
        ToggleButton tbtn = createIconButton(name, image);
        return new SimpleObjectProperty<>(tbtn);
    };

    private void initIcons() {
        // recent icons
        for (String recentIconName : recentIconNames) {
            Image image = EmoticonService.getInstance().getIcon((recentIconName));
            ImageView imageView = new ImageView(image);
            Tooltip tooltip = new Tooltip(recentIconName);
            Tooltip.install(imageView, tooltip);
            imageView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    dialog.setResult(recentIconName);
                    dialog.close();
                }
            });
            hbRecentIcons.getChildren().add(imageView);
        }

        for (int i = 0; i < MAX_COL_SIZE; i++) {
            TableColumn<List<String>, ToggleButton> col = new TableColumn<>(String.valueOf(i));
            col.setSortable(false);
            col.setEditable(false);
            col.setPrefWidth(50);
            col.setCellValueFactory(cellValueFactory);
            tableView.getColumns().add(col);
        }
        // all icons
        List<String> names = this.updateIcons(null);

        // auto-scroll to selected
        Platform.runLater(() -> {
            int idx = names.indexOf(origin);
            int row = idx / MAX_COL_SIZE;
            tableView.scrollTo(row);
        });
    }

    private List<String> updateIcons(String filter) {
        tableView.getItems().clear();
        List<String> names = new ArrayList<>(EmoticonService.getInstance().getIconNames());
        if (!StringUtils.isBlank(filter)) {
            names = names.stream().filter(s -> StringUtils.containsIgnoreCase(s, filter)).collect(Collectors.toList());
        }
        names.add(0, "empty");
        Matrix<String> matrix = new Matrix<>(names, MAX_COL_SIZE);
        for (int i = 0; i < matrix.rowCount(); i++) {
            List<String> row = matrix.getRow(i);
            tableView.getItems().add(row);
        }
        return names;
    }

    private ToggleButton createIconButton(String name, Image image) {
        ImageView imageView = null;
        if (image != null) {
            imageView = new ImageView(image);
        }
        ToggleButton tbtn = new ToggleButton(StringUtils.EMPTY, imageView);
        if (Objects.equals(name, origin)) {
            tbtn.setSelected(true);
        }
        tbtn.setPrefWidth(45);
        tbtn.setPrefHeight(40);
        tbtn.setUserData(name);
        Tooltip tooltip = new Tooltip(name);
        tbtn.setTooltip(tooltip);
        tbtn.setOnAction(this); // action
        return tbtn;
    }


    @Override
    public void handle(ActionEvent event) {
        Node node = (Node) event.getSource();
        selectedButton = (ToggleButton) node;
        result = (String) node.getUserData();
        if (selectedButton != null) {
            selectedButton.setSelected(false);
            selectedButton.requestFocus();
        }
    }


    @Override
    public void onPositive(String result) {
        recentIconNames.remove(result);
        Set<String> set = new LinkedHashSet<>();
        set.add(result);
        set.addAll(recentIconNames);
        FxPreferences.getInstance().savePreference(PrefConstants.PREF_KEY_MMD_RECENT_ICONS, set.stream().limit(12).collect(Collectors.toList()));
    }

}
