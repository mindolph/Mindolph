package com.mindolph.fx.control;

import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.llm.DatasetMeta;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.mindolph.genai.GenaiUiConstants.SUPPORTED_EMBEDDING_LANG;

/**
 * For displaying embedding datasets.
 *
 * @since 1.13.1
 */
public class DatasetTableView extends TableView<DatasetMeta> {


    private static final Logger log = LoggerFactory.getLogger(DatasetTableView.class);

    public DatasetTableView() {
    }

    public DatasetTableView(ObservableList<DatasetMeta> items) {
        super(items);
    }

    public void init() {
        TableColumn<DatasetMeta, String> colName = new TableColumn<>("Name");
        TableColumn<DatasetMeta, String> colFiles = new TableColumn<>("Files");
        TableColumn<DatasetMeta, String> colLang = new TableColumn<>("Language");
        TableColumn<DatasetMeta, String> colStatus = new TableColumn<>("Status");
        colName.setPrefWidth(120);
        colFiles.setPrefWidth(40);
        colLang.setPrefWidth(120);
        colStatus.setPrefWidth(80);
        colName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        colFiles.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getFiles() == null ? 0 : param.getValue().getFiles().size())));
        colLang.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLanguageCode()));
        colStatus.setCellValueFactory(param -> new SimpleStringProperty("%d%%".formatted(param.getValue().getProgress())));
        this.getColumns().addAll(List.of(colName, colFiles, colLang, colStatus));
    }

    public boolean replaceAll(List<DatasetMeta> datasetMetas) {
        // clear the table anyway.
        this.getItems().clear();
        if (datasetMetas != null && !datasetMetas.isEmpty()) {
            log.debug("Select datasets: {}", datasetMetas);
            // force to convert lang code to language.
            datasetMetas.forEach(datasetMeta -> {
                datasetMeta.setLanguageCode(GenAiConstants.lookupLanguage(datasetMeta.getLanguageCode()));
            });
            this.getItems().addAll(datasetMetas);
            super.refresh();
        }
        return true;
    }

}
