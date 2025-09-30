package com.mindolph.fx.preference;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.genai.GenaiUiConstants;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.ControlUtils;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.CheckBoxListCell;
import org.controlsfx.control.CheckListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author unknown
 */
public class DatasetSelectDialog extends BaseDialogController<List<DatasetMeta>> {

    @FXML
    private CheckListView<DatasetMeta> clvDatasets;

    public DatasetSelectDialog(List<DatasetMeta> datasets, String langCode) {
        dialog = new CustomDialogBuilder<List<DatasetMeta>>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Select datasets")
                .fxmlUri("dialog/datasets_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .icon(ButtonType.CANCEL, FontIconManager.getIns().getIcon(IconKey.CANCEL))
                .defaultValue(datasets)
                .resizable(true)
                .controller(this)
                .width(200)
                .build();

        clvDatasets.setCellFactory(listView -> new CheckBoxListCell<>(item -> clvDatasets.getItemBooleanProperty(item)) {
            @Override
            public void updateItem(DatasetMeta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                }
                else {
                    setText("%s (%s)".formatted(item.getName(), GenaiUiConstants.SUPPORTED_EMBEDDING_LANG.get(item.getLanguageCode())));
                    if (!item.getLanguageCode().equals(langCode)) {
                        this.setDisable(true);
                    }
                }
            }
        });

        Map<String, DatasetMeta> datasetMap = LlmConfig.getIns().loadAllDatasets();
        List<DatasetMeta> allDatasets = datasetMap.values().stream().toList();
        clvDatasets.getItems().addAll(allDatasets);
        List<String> ids = allDatasets.stream().map(DatasetMeta::getId).toList();
        for (DatasetMeta dataset : datasets) {
            clvDatasets.getCheckModel().check(ids.indexOf(dataset.getId()));
        }

        clvDatasets.getCheckModel().getCheckedItems().addListener((ListChangeListener<DatasetMeta>) c -> {
            while (c.next()) {
                result = new ArrayList<>(c.getList().stream().toList());
            }
        });

        ControlUtils.escapableControl(() -> {
            dialog.close();
        }, clvDatasets);
    }
}
