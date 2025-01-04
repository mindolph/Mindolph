package com.mindolph.base.genai.llm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.core.constant.GenAiConstants.ProviderProps;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.mfx.preference.FxPreferences;

import java.lang.reflect.Type;
import java.util.Map;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_PROVIDERS;
import static com.mindolph.core.constant.GenAiModelProvider.OPEN_AI;

/**
 * Manage llm config in java preference.
 *
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class LlmConfig {
    private static LlmConfig ins;
    private final FxPreferences fxPreferences;

    public LlmConfig() {
        fxPreferences = FxPreferences.getInstance();
    }

    public static synchronized LlmConfig getIns() {
        if (ins == null) ins = new LlmConfig();
        return ins;
    }

    /**
     *
     * @return
     */
    public String getActiveAiProvider() {
        return fxPreferences.getPreferenceAlias(PrefConstants.GEN_AI_PROVIDER_ACTIVE, PrefConstants.GENERAL_CONFIRM_BEFORE_QUITTING,
                OPEN_AI.getName());
    }

    /**
     * Save provider props, if the provider already exists, it will be overwritten.
     *
     * @param provider
     * @param providerProps
     */
    public void saveGenAiProvider(GenAiModelProvider provider, ProviderProps providerProps) {
        Map<String, ProviderProps> providerPropsMap = this.loadGenAiProviders();
        providerPropsMap.put(provider.getName(), providerProps);
        String json = new Gson().toJson(providerPropsMap);
        fxPreferences.savePreference(GEN_AI_PROVIDERS, json);
    }

    /**
     * @return
     */
    public Map<String, ProviderProps> loadGenAiProviders() {
        String json = fxPreferences.getPreferenceAlias(PrefConstants.GEN_AI_PROVIDERS,
                PrefConstants.GENERAL_AI_PROVIDERS, "{}");
        Type collectionType = new TypeToken<Map<String, ProviderProps>>() {
        }.getType();
        return new Gson().fromJson(json, collectionType);
    }
}
