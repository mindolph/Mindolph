package com.mindolph.fx.preference;

import com.mindolph.base.control.BaseOrganizedPrefsPane;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.core.util.Tuple2;
import com.mindolph.genai.ChoiceUtils;
import com.mindolph.genai.GenAiUtils;
import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

import static com.mindolph.core.constant.GenAiConstants.PROVIDER_MODELS;
import static com.mindolph.genai.GenaiUiConstants.*;

/**
 * Include choice boxes for LLM providers and their models.
 *
 * @since unknown
 */
public class BaseModelProviderPrefPane extends BaseOrganizedPrefsPane {

    private static final Logger log = LoggerFactory.getLogger(BaseModelProviderPrefPane.class);

    public BaseModelProviderPrefPane(String fxmlResourceUri) {
        super(fxmlResourceUri);
    }


    /**
     *
     * @param cbProvider
     * @param cbModel
     * @param type 1 is chat model, 2 is embedding model, see {@link GenAiConstants}
     */
    protected void initProviderAndModelComponents(ChoiceBox<Pair<GenAiModelProvider, String>> cbProvider, ChoiceBox<Pair<String, ModelMeta>> cbModel, int type) {
        cbProvider.setConverter(new ProviderConverter());
        List<Pair<GenAiModelProvider, String>> providerPairs = EnumUtils.getEnumList(GenAiModelProvider.class).stream().map(p -> new Pair<>(p, p.getName())).toList();
        cbProvider.getItems().addAll(providerPairs);
        cbProvider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            String providerName = newValue.getKey().getName();
            log.debug("selected provider: %s".formatted(providerName));
            Collection<ModelMeta> preDefinedModels = PROVIDER_MODELS.get(providerName)
                    .stream().filter(mm -> mm.getType() == type).toList();


            cbModel.getItems().clear();
            cbModel.getSelectionModel().selectFirst();


            if (CollectionUtils.isNotEmpty(preDefinedModels)) {
                log.debug("Found %d predefined models for provider %s and type %s".formatted(preDefinedModels.size(), providerName, type));
                cbModel.getItems().addAll(preDefinedModels.stream().map(mm -> new Pair<>(mm.getName(), mm)).sorted(MODEL_COMPARATOR).toList());
            }
            ProviderMeta providerMeta = LlmConfig.getIns().loadProviderMeta(providerName);
            if (providerMeta != null) {
                List<ModelMeta> customModels = providerMeta.customModels();
                if (customModels != null && !customModels.isEmpty()) {
                    customModels = customModels.stream().filter(mm -> mm.getType() == type).toList();
                    if (CollectionUtils.isNotEmpty(customModels)) {
                        log.debug("Found %d custom models for provider %s and type %s".formatted(customModels.size(), providerName, type));
                        cbModel.getItems().addAll(customModels.stream().map(mm -> new Pair<>(mm.getName(), mm)).sorted(MODEL_COMPARATOR).toList());
                    }
                }
            }
            super.saveChanges();
        });
        cbModel.setConverter(new ModelMetaConverter());
        cbModel.valueProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue == null) return;
            super.saveChanges();
        });
    }

    protected void selectProviderAndModel(ChoiceBox<Pair<GenAiModelProvider, String>> cbProvider, ChoiceBox<Pair<String, ModelMeta>> cbModel, String prefKey) {
        if (StringUtils.isNotBlank(prefKey)) {
            Tuple2<GenAiModelProvider, ModelMeta> providerModel = GenAiUtils.parseModelPreference(prefKey);
            if (providerModel != null) {
                ChoiceUtils.selectOrUnselectProvider(cbProvider, providerModel.a());
                ChoiceUtils.selectOrUnselectModel(cbModel, providerModel.b());
            }
        }
    }


    protected void saveProviderAndModelSelection(String prefKey, ChoiceBox<Pair<GenAiModelProvider, String>> cbProvider, ChoiceBox<Pair<String, ModelMeta>> cbModel) {
        if (cbProvider.getSelectionModel().getSelectedItem() == null || cbModel.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        super.fxPreferences.savePreference(prefKey,
                "%s:%s".formatted(cbProvider.getSelectionModel().getSelectedItem().getValue(), cbModel.getSelectionModel().getSelectedItem().getValue().getName()));
    }


    @Override
    protected void onSave(boolean notify) {

    }
}
