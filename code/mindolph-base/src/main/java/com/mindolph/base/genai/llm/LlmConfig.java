package com.mindolph.base.genai.llm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.core.util.AppUtils;
import com.mindolph.core.util.GsonUtils;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
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

    private final Type collectionType = new TypeToken<Map<String, DatasetMeta>>() {
    }.getType();

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
    public String getActiveProviderMeta() {
        return fxPreferences.getPreferenceAlias(PrefConstants.GEN_AI_PROVIDER_ACTIVE, PrefConstants.GENERAL_CONFIRM_BEFORE_QUITTING,
                OPEN_AI.getName());
    }

    /**
     * Save provider props, if the provider already exists, it will be overwritten.
     *
     * @param provider
     * @param providerMeta
     */
    public void saveProviderMeta(GenAiModelProvider provider, ProviderMeta providerMeta) {
        Map<String, ProviderMeta> providerPropsMap = this.loadAllProviderMetas();
        providerPropsMap.put(provider.getName(), providerMeta);
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
        ProviderMeta providerMeta = this.loadProviderMeta(provider.getName());
        for (ModelMeta customModel : providerMeta.customModels()) {
            customModel.setActive(customModel.name().equals(modelMeta.name()));
        }
        saveProviderMeta(provider, providerMeta);
    }

    /**
     * Load all LLM providers.
     *
     * @return Provider name -> Provider properties
     */
    public Map<String, ProviderMeta> loadAllProviderMetas() {
        String json = fxPreferences.getPreferenceAlias(PrefConstants.GEN_AI_PROVIDERS,
                PrefConstants.GENERAL_AI_PROVIDERS, "{}");
        Type collectionType = new TypeToken<Map<String, ProviderMeta>>() {
        }.getType();
        return new Gson().fromJson(json, collectionType);
    }

    public ProviderMeta loadProviderMeta(String providerName) {
        Map<String, ProviderMeta> providers = loadAllProviderMetas();
        return providers.get(providerName);
    }

    /**
     * Get preferred model name for active LLM provider
     *
     * @return
     * @since 1.11
     */
    public ModelMeta preferredModelForActiveLlmProvider() {
        String activeProvider = LlmConfig.getIns().getActiveProviderMeta();
        if (StringUtils.isNotBlank(activeProvider)) {
            Map<String, ProviderMeta> providers = LlmConfig.getIns().loadAllProviderMetas();
            if (providers.containsKey(activeProvider)) {
                ProviderMeta props = providers.get(activeProvider);
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
        return GsonUtils.newGson().fromJson(json, collectionType);
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
        fxPreferences.savePreference(PrefConstants.GEN_AI_AGENTS, GsonUtils.newGson().toJson(agentMap));
    }

    public void removeAgent(String agentId) {
        Map<String, AgentMeta> agentMap = loadAgents();
        agentMap.remove(agentId);
        fxPreferences.savePreference(PrefConstants.GEN_AI_AGENTS, new Gson().toJson(agentMap));
    }

    public List<DatasetMeta> getDatasetsFromAgentId(String agentId) {
        AgentMeta agentMeta = this.loadAgent(agentId);
        return this.getDatasetsFromIds(agentMeta.getDatasetIds());
    }

    public List<DatasetMeta> getDatasetsFromIds(List<String> datasetIds) {
        Map<String, DatasetMeta> datasetMap = loadAllDatasets();
        return datasetIds.stream().map(datasetMap::get).toList();
    }

    public Map<String, DatasetMeta> loadAllDatasets() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(datasetConfigFile()), StandardCharsets.UTF_8))) {
            Object o = GsonUtils.newGson().fromJson(reader, collectionType);
            if (o == null) {
                return new HashMap<>();
            }
            return (Map<String, DatasetMeta>) o;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveDataset(String datasetId, DatasetMeta datasetMeta) {
        if (StringUtils.isBlank(datasetId) || datasetMeta == null) {
            throw new IllegalStateException("Agent id or agent is null");
        }
        Map<String, DatasetMeta> datasetMap = loadAllDatasets();
        datasetMeta.setId(datasetId);
        datasetMap.put(datasetId, datasetMeta);
        String json = GsonUtils.newGson().toJson(datasetMap, collectionType);
        try {
            IOUtils.write(json, new FileOutputStream(datasetConfigFile()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeDataset(String datasetId) {
        Map<String, DatasetMeta> datasetMap = this.loadAllDatasets();
        datasetMap.remove(datasetId);
        String json = GsonUtils.newGson().toJson(datasetMap, collectionType);
        try {
            IOUtils.write(json, new FileOutputStream(datasetConfigFile()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File datasetConfigFile() {
        String path = "%s/conf/datasets.json".formatted(AppUtils.getAppBaseDir());
        File f = new File(path);
        if (!f.exists()) {
            try {
                if (f.createNewFile()) {
                    return f;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return f;
    }

//    public List<DatasetMeta> getDatasetsFromAgentId(String agentId) {
//        AgentMeta agentMeta = LlmConfig.getIns().loadAgent(agentId);
//        Map<String, DatasetMeta> datasetMap = LlmConfig.getIns().loadAllDatasets();
//        if (agentMeta.getDatasetIds() == null || agentMeta.getDatasetIds().isEmpty()) {
//            return null;
//        }
//        return agentMeta.getDatasetIds().stream().map(datasetMap::get).toList();
//    }
//
//    public List<DatasetMeta> getDatasetsFromIds(List<String> datasetIds) {
//        Map<String, DatasetMeta> datasetMap = LlmConfig.getIns().loadAllDatasets();
//        return datasetIds.stream().map(datasetMap::get).toList();
//    }
//
//    public Map<String, DatasetMeta> loadAllDatasets() {
//        String json = fxPreferences.getPreference(PrefConstants.GEN_AI_DATASETS, "{}");
//        Type collectionType = new TypeToken<Map<String, DatasetMeta>>() {
//        }.getType();
//        return GsonUtils.newGson().fromJson(json, collectionType);
//    }
//
//    public void saveDataset(String datasetId, DatasetMeta datasetMeta) {
//        if (StringUtils.isBlank(datasetId) || datasetMeta == null) {
//            throw new IllegalStateException("Agent id or agent is null");
//        }
//        Map<String, DatasetMeta> datasetMap = loadAllDatasets();
//        datasetMeta.setId(datasetId);
//        datasetMap.put(datasetId, datasetMeta);
//        fxPreferences.savePreference(PrefConstants.GEN_AI_DATASETS, GsonUtils.newGson().toJson(datasetMap));
//    }
//
//    public void removeDataset(String datasetId) {
//        Map<String, DatasetMeta> datasetMap = this.loadAllDatasets();
//        datasetMap.remove(datasetId);
//        fxPreferences.savePreference(PrefConstants.GEN_AI_DATASETS, GsonUtils.newGson().toJson(datasetMap));
//    }


}
