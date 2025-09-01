package com.mindolph.base.genai.llm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.constant.VectorStoreProvider;
import com.mindolph.core.llm.*;
import com.mindolph.core.util.AppUtils;
import com.mindolph.core.util.GsonUtils;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.JsonUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_PROVIDERS;
import static com.mindolph.base.constant.PrefConstants.GEN_AI_VECTOR_STORE_PROVIDER_ACTIVE;

/**
 * Manage llm config in java preference.
 *
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class LlmConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmConfig.class);

    private static LlmConfig ins;
    private final FxPreferences fxPreferences;

    private final Type collectionType = new TypeToken<Map<String, DatasetMeta>>() {
    }.getType();

    private final Type vectorStoreMetaType = new TypeToken<Map<String, VectorStoreMeta>>() {
    }.getType();

    public LlmConfig() {
        fxPreferences = FxPreferences.getInstance();
    }

    public static synchronized LlmConfig getIns() {
        if (ins == null) ins = new LlmConfig();
        return ins;
    }

    /**
     * Save provider meta, if the provider already exists, it will be overwritten.
     *
     * @param provider
     * @param providerMeta
     */
    public void saveProviderMeta(GenAiModelProvider provider, ProviderMeta providerMeta) {
        Map<String, ProviderMeta> providerPropsMap = this.loadAllProviderMetas();
        providerPropsMap.put(provider.name(), providerMeta);
        String json = new Gson().toJson(providerPropsMap);
        fxPreferences.savePreference(GEN_AI_PROVIDERS, json);
    }

    /**
     * Get filtered custom models of the provider for the type.
     *
     * @param providerName
     * @param modelType
     * @return
     */
    public Collection<ModelMeta> getFilteredCustomModels(String providerName, int modelType) {
        ProviderMeta providerMeta = this.loadProviderMeta(providerName);
        if (providerMeta != null) {
            List<ModelMeta> customModels = providerMeta.customModels();
            if (customModels != null && !customModels.isEmpty()) {
                customModels = customModels.stream().filter(mm -> mm.getType() == modelType).toList();
                return customModels;
            }
        }
        return null;
    }

    public ModelMeta lookupCustomModel(String providerName, String modelName) {
        Map<String, ProviderMeta> map = this.loadAllProviderMetas();
        ProviderMeta providerMeta = map.get(providerName);
        if (providerMeta != null) {
            for (ModelMeta modelMeta : providerMeta.customModels()) {
                if (modelMeta.getName().equals(modelName)) {
                    return modelMeta;
                }
            }
        }
        return null;
    }

    /**
     * Activate a custom model directly for active provider.
     *
     * @param provider
     * @param modelMeta
     * @since 1.11
     * @deprecated
     */
    public void activateCustomModel(GenAiModelProvider provider, ModelMeta modelMeta) {
        ProviderMeta providerMeta = this.loadProviderMeta(provider.name());
        for (ModelMeta customModel : providerMeta.customModels()) {
            customModel.setActive(customModel.getName().equals(modelMeta.getName()));
        }
        saveProviderMeta(provider, providerMeta);
    }

    /**
     * Load all LLM providers.
     *
     * @return Provider name -> Provider properties
     */
    public Map<String, ProviderMeta> loadAllProviderMetas() {
        String json = fxPreferences.getPreference(PrefConstants.GEN_AI_PROVIDERS, "{}");
        Type collectionType = new TypeToken<Map<String, ProviderMeta>>() {
        }.getType();
        return new Gson().fromJson(json, collectionType);
    }

    public ProviderMeta loadProviderMeta(String providerName) {
        Map<String, ProviderMeta> providers = loadAllProviderMetas();
        return providers.get(providerName);
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
        // the ids and the dataset metas might don't match in development.
        return datasetIds.stream().map(datasetMap::get).filter(Objects::nonNull).toList();
    }

    public Map<String, DatasetMeta> loadAllDatasets() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(this.datasetConfigFile()), StandardCharsets.UTF_8))) {
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
            throw new IllegalStateException("Dataset id or dataset is null");
        }
        log.debug("save dataset {}", JsonUtils.object2PrettyJson(datasetMeta));
        Map<String, DatasetMeta> datasetMap = this.loadAllDatasets();
        datasetMeta.setId(datasetId);
        datasetMap.put(datasetId, datasetMeta);
        String json = JsonUtils.object2PrettyJson(datasetMap);
//        String json = GsonUtils.newGson().toJson(datasetMap, collectionType);
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

    public VectorStoreMeta loadActiveVectorStorePrefs() {
        String activeVectorStoreProvider = fxPreferences.getPreference(GEN_AI_VECTOR_STORE_PROVIDER_ACTIVE, String.class);
        if (StringUtils.isBlank(activeVectorStoreProvider)) {
            return null;
        }
        return loadVectorStorePrefs(VectorStoreProvider.valueOf(activeVectorStoreProvider));
    }

    public VectorStoreMeta loadVectorStorePrefs(VectorStoreProvider provider) {
        String json = fxPreferences.getPreference(PrefConstants.GEN_AI_VECTOR_STORE_PROVIDERS, "{}");
        Map<String, VectorStoreMeta> map = GsonUtils.newGson().fromJson(json, vectorStoreMetaType);
        VectorStoreMeta vectorStoreMeta = map.get(provider.name());
        if (vectorStoreMeta != null) {
            vectorStoreMeta.setProvider(provider);
        }
        return vectorStoreMeta;
    }

    public void saveVectorStorePrefs(VectorStoreProvider provider, VectorStoreMeta vectorStoreMeta) {
        String json = fxPreferences.getPreference(PrefConstants.GEN_AI_VECTOR_STORE_PROVIDERS, "{}");
        Map<String, VectorStoreMeta> map = GsonUtils.newGson().fromJson(json, vectorStoreMetaType);
        map.put(provider.name(), vectorStoreMeta);
        fxPreferences.savePreference(PrefConstants.GEN_AI_VECTOR_STORE_PROVIDERS, GsonUtils.newGson().toJson(map));
    }

    private File datasetConfigFile() {
        String path = "%s/conf/datasets.json".formatted(AppUtils.getAppBaseDir());
        File f = new File(path);
        if (!f.exists()) {
            try {
                if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
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
