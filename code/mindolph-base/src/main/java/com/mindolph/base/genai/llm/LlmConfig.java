package com.mindolph.base.genai.llm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderProps;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.lang3.StringUtils;

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
     * Activate custom model directly for active provider.
     *
     * @param provider
     * @param modelMeta
     * @since 1.11
     */
    public void activateCustomModel(GenAiModelProvider provider, ModelMeta modelMeta) {
        ProviderProps providerProps = this.loadGenAiProviderProps(provider.getName());
        for (ModelMeta customModel : providerProps.customModels()) {
            customModel.setActive(customModel.name().equals(modelMeta.name()));
        }
        saveGenAiProvider(provider, providerProps);
    }

    /**
     * Load all LLM providers.
     *
     * @return Provider name -> Provider properties
     */
    public Map<String, ProviderProps> loadGenAiProviders() {
        String json = fxPreferences.getPreferenceAlias(PrefConstants.GEN_AI_PROVIDERS,
                PrefConstants.GENERAL_AI_PROVIDERS, "{}");
        Type collectionType = new TypeToken<Map<String, ProviderProps>>() {
        }.getType();
        return new Gson().fromJson(json, collectionType);
    }

    public ProviderProps loadGenAiProviderProps(String providerName) {
        Map<String, ProviderProps> providers = loadGenAiProviders();
        return providers.get(providerName);
    }

    /**
     * Get preferred model name for active LLM provider
     *
     * @return
     * @since 1.11
     */
    public ModelMeta preferredModelForActiveLlmProvider() {
        String activeProvider = LlmConfig.getIns().getActiveAiProvider();
        if (StringUtils.isNotBlank(activeProvider)) {
            Map<String, ProviderProps> providers = LlmConfig.getIns().loadGenAiProviders();
            if (providers.containsKey(activeProvider)) {
                ProviderProps props = providers.get(activeProvider);
                if (StringUtils.isNotBlank(props.aiModel())) {
                    if ("Custom".equals(props.aiModel())) {
                        if (props.customModels() == null) {
                            return null;
                        }
                        return props.customModels().stream().filter(ModelMeta::active).findFirst().orElse(null);
                    }
                    // for pre-defined models
                    return GenAiConstants.lookupModelMeta(activeProvider, props.aiModel());
                }
            }
        }
        return null;
    }
}
