package com.mindolph.genai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mindolph.mfx.preference.FxPreferences;
import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;

import java.util.List;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_OUTPUT_LANGUAGE;
import static com.mindolph.core.constant.GenAiConstants.LANGS_JSON;

/**
 * @since 1.11.1
 */
public class ChoiceUtils {

    public static void loadLanguagesTo(ChoiceBox<Pair<String, String>> choiceBox) {
        JsonArray langs = new Gson().fromJson(LANGS_JSON, JsonArray.class);
        List<Pair<String, String>> list = langs.asList().stream().map(e -> new Pair<>(((JsonObject) e).get("code").getAsString(), ((JsonObject) e).get("name").getAsString())).toList();
        choiceBox.getItems().clear();
        choiceBox.getItems().addAll(list);
        Pair<String, String> savedLang = FxPreferences.getInstance().getPreference(GEN_AI_OUTPUT_LANGUAGE, list.stream().findFirst().get());
        if (savedLang != null) {
//            log.debug("Saved language: %s".formatted(savedLang.getKey()));
            choiceBox.getSelectionModel().select(savedLang);
        }
    }
}
