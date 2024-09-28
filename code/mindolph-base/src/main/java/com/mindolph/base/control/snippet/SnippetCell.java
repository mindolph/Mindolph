package com.mindolph.base.control.snippet;

import com.mindolph.base.util.ColorUtils;
import com.mindolph.core.model.Snippet;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author mindolph.com@gmail.com
 * @see SnippetView
 */
public class SnippetCell extends ListCell<Snippet> {

    @Override
    protected void updateItem(Snippet item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            FXMLLoader fxmlLoader = FxmlUtils.loadUri("/control/snippet_item.fxml", new ItemController(item));
            Node root = fxmlLoader.getRoot();
            setGraphic(root);
        }
        else {
            setText(null);
            setGraphic(null);
        }
    }

    private static class ItemController extends AnchorPane implements Initializable {
        @FXML
        private Label title;
        @FXML
        private ImageView icon;
        private Snippet snippet;

        public ItemController(Snippet snippet) {
            this.snippet = snippet;
        }

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            title.setText(snippet.getTitle());
            if (StringUtils.isNotBlank(snippet.getDescription())) {
                Tooltip tooltip = new Tooltip();
                tooltip.setText(snippet.getDescription());
                Tooltip.install(this, tooltip);
            }
            if (snippet instanceof ColorSnippet) {
                String colorName = StringUtils.remove(snippet.getTitle(), '#');
                Color color = Color.valueOf(colorName);
                if (color != null) {
                    Color textColor = ColorUtils.makeTextColorForBackground(color);
                    Background b = new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
                    this.setBackground(b);
                    title.setTextFill(textColor);
                }
            }
        }
    }
}
