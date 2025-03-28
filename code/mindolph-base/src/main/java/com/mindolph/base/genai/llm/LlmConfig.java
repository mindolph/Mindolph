package com.mindolph.base.genai.llm;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderProps;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

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

    public AgentMeta loadAgent(String id) {
        return loadAgents().get(id);
    }

    public Map<String, AgentMeta> loadAgents() {
        String json = fxPreferences.getPreference(PrefConstants.GEN_AI_AGENTS, "{}");
        Type collectionType = new TypeToken<Map<String, AgentMeta>>() {
        }.getType();
        return newGson().fromJson(json, collectionType);
    }

    public boolean saveAgent(AgentMeta agentMeta) {
        Map<String, AgentMeta> agentMap = loadAgents();
        if (agentMap.values().stream().anyMatch(a -> a.getName().equals(agentMeta.getName()))) {
            return false;
        }
        agentMap.put(String.valueOf(UUID.randomUUID()), agentMeta);
        fxPreferences.savePreference(PrefConstants.GEN_AI_AGENTS, new Gson().toJson(agentMap));
        return true;
    }

    public void saveAgent(String agentId, AgentMeta agentMeta) {
        if (StringUtils.isBlank(agentId) || agentMeta == null) {
            throw new IllegalStateException("Agent id or agent is null");
        }
        Map<String, AgentMeta> agentMap = loadAgents();
        agentMeta.setId(agentId);
        agentMap.put(agentId, agentMeta);
        fxPreferences.savePreference(PrefConstants.GEN_AI_AGENTS, newGson().toJson(agentMap));
    }

    public void removeAgent(String agentId) {
        Map<String, AgentMeta> agentMap = loadAgents();
        agentMap.remove(agentId);
        fxPreferences.savePreference(PrefConstants.GEN_AI_AGENTS, new Gson().toJson(agentMap));
    }

    private Gson newGson() {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(File.class, new FileSerializer());
        return builder.create();
    }

    static class FileSerializer implements JsonSerializer<File>, JsonDeserializer<File> {

        @Override
        public File deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return json.isJsonNull() ? null : new File(json.getAsString());
        }

        @Override
        public JsonElement serialize(File json, Type typeOfSrc, JsonSerializationContext context) {
            return json == null ? JsonNull.INSTANCE : new JsonPrimitive(json.getPath());
        }
    }
}
