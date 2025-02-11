package com.mindolph.genai;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.util.converter.PairStringStringConverter;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderProps;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Map;

import static com.mindolph.core.constant.GenAiConstants.PROVIDER_MODELS;
import static com.mindolph.genai.GenaiUiConstants.MODEL_COMPARATOR;

/**
 *
 * @since 1.11.1
 */
public abstract class BaseAiPane extends StackPane {
    @FXML
    protected ChoiceBox<Pair<String, ModelMeta>> cbModel;
    @FXML
    protected ChoiceBox<Pair<String, String>> cbLanguage;
    @FXML
    protected Button btnClose;
    @FXML
    protected Label lbIcon;
    @FXML
    protected Label lbTitle;

    protected final Object editorId;
    protected final String fileType;

    public BaseAiPane(String res, Object editorId, String fileType) {
        this.editorId = editorId;
        this.fileType = fileType;
        FxmlUtils.loadUri(res, this);

        lbIcon.setGraphic(FontIconManager.getIns().getIcon(IconKey.MAGIC));
        btnClose.setGraphic(FontIconManager.getIns().getIcon(IconKey.CLOSE));


        String activeProvider = LlmConfig.getIns().getActiveAiProvider();
        Map<String, ProviderProps> map = LlmConfig.getIns().loadGenAiProviders();
        ProviderProps providerProps = map.get(activeProvider);
        Pair<String, ModelMeta> targetItem;
        if ("Custom".equals(providerProps.aiModel())) {
            ModelMeta customModelMeta = providerProps.customModels().stream().filter(ModelMeta::active).findFirst().orElse(null);
            targetItem = new Pair<>(customModelMeta.name(), customModelMeta);
        }
        else {
            targetItem = new Pair<>(providerProps.aiModel(), GenAiConstants.lookupModelMeta(activeProvider, providerProps.aiModel()));
        }
        List<Pair<String, ModelMeta>> models = PROVIDER_MODELS.get(activeProvider)
                .stream().map(m -> new Pair<>(m.name(), m)).sorted(MODEL_COMPARATOR).toList();
        cbModel.getItems().clear();
        cbModel.getItems().addAll(models);
        if (!models.contains(targetItem)) {
            cbModel.getItems().add(targetItem); // exclude same model
        }

        if (cbModel.getItems().contains(targetItem)) {
            cbModel.getSelectionModel().select(targetItem);
        }
        cbModel.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<String, ModelMeta> object) {
                return object == null ? "" : object.getValue().name();
            }

            @Override
            public Pair<String, ModelMeta> fromString(String string) {
                return null;
            }
        });

        cbLanguage.setConverter(new PairStringStringConverter());
        ChoiceUtils.loadLanguagesTo(cbLanguage);
    }
}
