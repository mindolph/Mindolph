package com.mindolph.genai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mindolph.core.constant.AiConstants;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.mfx.preference.FxPreferences;
import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_OUTPUT_LANGUAGE;
import static com.mindolph.core.constant.AiConstants.lookupLanguage;
import static com.mindolph.genai.AiUiConstants.LANGUAGES_IN_JSON;
import static com.mindolph.genai.AiUiConstants.aiProviderConverter;

/**
 * @since 1.11.1
 */
public class ChoiceUtils {

    public static void loadEmbeddingLanguages(ChoiceBox<Pair<String, String>> choiceBox) {
        JsonArray langs = new Gson().fromJson(LANGUAGES_IN_JSON, JsonArray.class);
        List<Pair<String, String>> list = langs.asList().stream().map(e -> new Pair<>(((JsonObject) e).get("code").getAsString(), ((JsonObject) e).get("name").getAsString())).toList();
        choiceBox.getItems().clear();
        choiceBox.getItems().addAll(list);
    }

    public static void selectOrUnselectLanguage(ChoiceBox<Pair<String, String>> choiceBox, String languageCode) {
        if (StringUtils.isNotBlank(languageCode)) {
            String language = AiUiConstants.lookupLanguage(languageCode);
            if (StringUtils.isNotBlank(language)) {
                choiceBox.getSelectionModel().select(new Pair<>(languageCode, language));
            }
            else {
                choiceBox.getSelectionModel().clearSelection();
            }
        }
        else {
            choiceBox.getSelectionModel().clearSelection();
        }
    }

    public static void selectOrUnselectProvider(ChoiceBox<Pair<String, ProviderMeta>> cbProvider, String provider) {
        if (provider != null) {
            ChoiceUtils.selectProvider(cbProvider, provider);
        }
        else {
            cbProvider.getSelectionModel().clearSelection();
        }
    }

    public static void selectOrUnselectModel(ChoiceBox<Pair<String, ModelMeta>> cbModel, String modelMeta) {
        if (modelMeta != null) {
            ChoiceUtils.selectModel(cbModel, modelMeta);
        }
        else {
            cbModel.getSelectionModel().clearSelection();
        }
    }

    /**
     * Select the target provider in the choice box.
     *
     * @param choiceBox The provider choice box
     * @param providerId
     * @since 1.13.0
     */
    public static void selectProvider(ChoiceBox<Pair<String, ProviderMeta>> choiceBox, String providerId) {
        for (Pair<String, ProviderMeta> item : choiceBox.getItems()) {
            if (item != null && item.getKey().equals(providerId)) {
                choiceBox.getSelectionModel().select(item);
                break;
            }
        }
    }

    /**
     * Select the target model in the choice box.
     *
     * @param choiceBox The model choice box
     * @param modelMeta
     * @since 1.13.0
     */
    public static void selectModel(ChoiceBox<Pair<String, ModelMeta>> choiceBox, String modelMeta) {
        for (Pair<String, ModelMeta> item : choiceBox.getItems()) {
            if (item != null && item.getValue().getName().equals(modelMeta)) {
                choiceBox.getSelectionModel().select(item);
                break;
            }
        }
    }


    /**
     * Load pre-defined languages to a ChoiceBox and select the default one.
     * Only be used for LLM generating content.
     * @param choiceBox
     */
    public static void loadLanguagesToAndSelectDefault(ChoiceBox<Pair<String, String>> choiceBox) {
        String[][] langCodeNameMap = AiConstants.allLanguages();
        List<Pair<String, String>> list = Arrays.stream(langCodeNameMap).map(e -> new Pair<>(e[0], e[1])).toList();
        choiceBox.getItems().clear();
        choiceBox.getItems().addAll(list);
        String savedLangCode = FxPreferences.getInstance().getPreference(GEN_AI_OUTPUT_LANGUAGE, list.stream().findFirst().get().getKey());
        if (savedLangCode != null) {
//            log.debug("Saved language: %s".formatted(savedLang.getKey()));
            String language = lookupLanguage(savedLangCode);
            choiceBox.getSelectionModel().select(new Pair<>(savedLangCode, language));
        }
    }

    public static void initProviders(ChoiceBox<Pair<String, ProviderMeta>> choiceBox, Map<String, ProviderMeta> metaMap){
        choiceBox.getItems().clear();
        choiceBox.setConverter(aiProviderConverter);
        List<Pair<String, ProviderMeta>> providerPairs = metaMap.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue())).toList();
        choiceBox.getItems().addAll(providerPairs);
    }
}
