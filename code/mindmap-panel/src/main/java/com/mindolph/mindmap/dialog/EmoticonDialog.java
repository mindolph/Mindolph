package com.mindolph.mindmap.dialog;

import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.control.IconView;
import com.mindolph.base.control.snippet.ImageSnippet;
import com.mindolph.base.util.LayoutUtils;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mindmap.icon.EmoticonService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author mindolph.com@gmail.com
 * @since 1.10
 */
public class EmoticonDialog extends BaseDialogController<String> implements EventHandler<ActionEvent> {

    private final Logger log = LoggerFactory.getLogger(EmoticonDialog.class);

    @FXML
    private AnchorPane iconPane;

    @FXML
    private HBox hbRecentIcons;

    @FXML
    private HBox hBox;

    private TextField tfKeyword;

    @FXML
    private Button btnClear;

    private IconView iconView;

    // for unselect
    private ToggleButton selectedButton;

    private Set<String> recentIconNames;

    public EmoticonDialog(String iconName) {
        super(iconName);
        origin = iconName;
        dialog = new CustomDialogBuilder<String>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Emoticons")
                .fxmlUri("dialog/emoticon_dialog.fxml")
                .defaultValue(iconName)
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .controller(EmoticonDialog.this)
                .height(480)
                .resizable(true)
                .build();

        dialog.setOnCloseRequest(dialogEvent -> {
            if (!confirmClosing("Icon has benn changed, are you sure to close the dialog?")) {
                dialogEvent.consume();
            }
        });
        tfKeyword = TextFields.createClearableTextField();
        tfKeyword.setPrefWidth(600);
        hBox.getChildren().add(tfKeyword);
        tfKeyword.textProperty().addListener((observable, oldValue, newValue) -> {
            this.filter(newValue);
        });
        recentIconNames = FxPreferences.getInstance().getPreference(PrefConstants.PREF_KEY_MMD_RECENT_ICONS, new LinkedHashSet<>());
        this.initIconView();
        Platform.runLater(() -> {
            tfKeyword.requestFocus();
        });
    }

    public void initIconView() {
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

        // All icons by IconView
        this.iconView = new IconView();
        LayoutUtils.anchor(iconView, 0);
        iconView.selectedItemsProperty().addListener((observable, oldValue, newValue) -> {
            result = newValue.getFirst().getCode();
        });
        iconPane.getChildren().add(iconView);
        this.filter(null);
    }

    // Filter all icons by keyword, if keyword is not provided, all icons will be displayed.
    private void filter(String keyword) {
        iconView.setItems(null);
        List<ImageSnippet> snippets = new ArrayList<>();
        // init image snippets from emotion library
        snippets.add(new ImageSnippet("empty").code("empty"));// for empty icon
        for (String iconName : EmoticonService.getInstance().getIconNames()) {
            if (StringUtils.isBlank(keyword) || iconName.toLowerCase().contains(keyword.toLowerCase())) {
                Image img = EmoticonService.getInstance().getIcon(iconName);
                snippets.add(new ImageSnippet().title(iconName).code(iconName).image(img));
            }
        }
        if (StringUtils.isNotBlank(super.origin)) {
            log.debug("Pre-select icon %s".formatted(super.origin));
            Image preSelectIcon = EmoticonService.getInstance().getIcon(super.origin);
            ImageSnippet preSelectSnippet = new ImageSnippet(super.origin).code(super.origin).image(preSelectIcon);
            iconView.setSelectedItems(FXCollections.observableList(Collections.singletonList(preSelectSnippet)));
        }
        iconView.setItems(FXCollections.observableList(snippets));
    }

    @Override
    public void handle(ActionEvent event) {
        result = iconView.getSelectedItems().getFirst().getCode();
    }

    @Override
    public void onPositive(String result) {
        log.debug("Select %s".formatted(result));
        recentIconNames.remove(result); // remove first if already there.
        Set<String> set = new LinkedHashSet<>();
        set.add(result);
        set.addAll(recentIconNames);
        FxPreferences.getInstance().savePreference(PrefConstants.PREF_KEY_MMD_RECENT_ICONS, set.stream().limit(12).collect(Collectors.toList()));
    }

}
