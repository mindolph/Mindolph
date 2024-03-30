package com.mindolph.base.genai.llm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.genai.llm.Constants.ProviderProps;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.mfx.preference.FxPreferences;

import java.lang.reflect.Type;
import java.util.Map;

import static com.mindolph.base.constant.PrefConstants.GENERAL_AI_PROVIDERS;
import static com.mindolph.core.constant.GenAiModelProvider.OPEN_AI;

/**
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
        return fxPreferences.getPreference(PrefConstants.GENERAL_AI_PROVIDER_ACTIVE, OPEN_AI.getName());
    }

    /**
     * @param provider
     * @param providerProps
     */
    public void saveGenAiProvider(GenAiModelProvider provider, ProviderProps providerProps) {
        Map<String, ProviderProps> providerPropsMap = this.loadGenAiProviders();
        providerPropsMap.put(provider.getName(), providerProps);
        String json = new Gson().toJson(providerPropsMap);
        fxPreferences.savePreference(GENERAL_AI_PROVIDERS, json);
    }

    /**
     * @return
     */
    public Map<String, ProviderProps> loadGenAiProviders() {
        String json = fxPreferences.getPreference(PrefConstants.GENERAL_AI_PROVIDERS, "{}");
        Type collectionType = new TypeToken<Map<String, ProviderProps>>() {
        }.getType();
        return new Gson().fromJson(json, collectionType);
    }
}
