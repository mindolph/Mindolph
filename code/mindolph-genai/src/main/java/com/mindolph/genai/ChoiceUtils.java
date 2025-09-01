package com.mindolph.genai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.mfx.preference.FxPreferences;
import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_OUTPUT_LANGUAGE;
import static com.mindolph.core.constant.GenAiConstants.LANGS_JSON;
import static com.mindolph.core.constant.GenAiConstants.lookupLanguage;
import static com.mindolph.genai.GenaiUiConstants.SUPPORTED_EMBEDDING_LANG;

/**
 * @since 1.11.1
 */
public class ChoiceUtils {

    /**
     * Load pre-defined languages to a ChoiceBox and select the default one.
     *
     * @param choiceBox
     */
    public static void loadLanguagesToAndSelectDefault(ChoiceBox<Pair<String, String>> choiceBox) {
        JsonArray langs = new Gson().fromJson(LANGS_JSON, JsonArray.class);
        List<Pair<String, String>> list = langs.asList().stream().map(e -> new Pair<>(((JsonObject) e).get("code").getAsString(), ((JsonObject) e).get("name").getAsString())).toList();
        choiceBox.getItems().clear();
        choiceBox.getItems().addAll(list);
        String savedLangCode = FxPreferences.getInstance().getPreference(GEN_AI_OUTPUT_LANGUAGE, list.stream().findFirst().get().getKey());
        if (savedLangCode != null) {
//            log.debug("Saved language: %s".formatted(savedLang.getKey()));
            String language = lookupLanguage(savedLangCode);
            choiceBox.getSelectionModel().select(new Pair<>(savedLangCode, language));
        }
    }

    public static void selectOrUnselectLanguage(ChoiceBox<Pair<String, String>> choiceBox, String languageCode) {
        if (StringUtils.isNotBlank(languageCode)) {
            String language = SUPPORTED_EMBEDDING_LANG.get(languageCode);
            if (StringUtils.isNotBlank(language)) {
                choiceBox.getSelectionModel().select(new Pair<>(languageCode, SUPPORTED_EMBEDDING_LANG.get(languageCode)));
            }
            else {
                choiceBox.getSelectionModel().clearSelection();
            }
        }
        else {
            choiceBox.getSelectionModel().clearSelection();
        }
    }

    public static void selectOrUnselectProvider(ChoiceBox<Pair<GenAiModelProvider, String>> cbProvider, GenAiModelProvider provider) {
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
     * @param provider
     * @since unknown
     */
    public static void selectProvider(ChoiceBox<Pair<GenAiModelProvider, String>> choiceBox, GenAiModelProvider provider) {
        for (Pair<GenAiModelProvider, String> item : choiceBox.getItems()) {
            if (item.getKey() == provider) {
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
     * @since unknown
     */
    public static void selectModel(ChoiceBox<Pair<String, ModelMeta>> choiceBox, String modelMeta) {
        for (Pair<String, ModelMeta> item : choiceBox.getItems()) {
            if (item.getValue().getName().equals(modelMeta)) {
                choiceBox.getSelectionModel().select(item);
                break;
            }
        }
    }
}
