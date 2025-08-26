package com.mindolph.fx.data;

import com.google.gson.Gson;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ProviderProps;
import com.mindolph.mfx.preference.FxPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.mindolph.base.constant.PrefConstants.GEN_AI_PROVIDERS;
import static com.mindolph.base.constant.PrefConstants.GEN_AI_PROVIDER_ACTIVE;

/**
 * Fix gen-ai settings
 * @since 1.12.5
 */
public class MigrationV3 implements Migration {


    private static final Logger log = LoggerFactory.getLogger(MigrationV3.class);

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public void doMigration() {
        Map<String, ProviderProps> providerProps = LlmConfig.getIns().loadGenAiProviders();
        Map<String, ProviderProps> newProviderProps = new HashMap<>();
        for (String k : providerProps.keySet()) {
            ProviderProps p = providerProps.get(k);
            GenAiModelProvider provider = GenAiModelProvider.fromName(k);
            if (provider != null) {
                log.info("Convert provider from %s to %s".formatted(k, provider.name()));
                newProviderProps.put(provider.name(), p);
            }
            else {
                // keep the correct ones just for re-run the migration.
                log.info("Keep the provider: %s".formatted(k));
                newProviderProps.put(k, p);
            }
        }
        String json = new Gson().toJson(newProviderProps);
        FxPreferences.getInstance().savePreference(GEN_AI_PROVIDERS, json);

        // it's the display name
        String activeProvider = FxPreferences.getInstance().getPreference(GEN_AI_PROVIDER_ACTIVE, String.class);
        if (activeProvider != null) {
            GenAiModelProvider provider = GenAiModelProvider.fromName(activeProvider);
            if (provider != null) {
                FxPreferences.getInstance().savePreference(GEN_AI_PROVIDER_ACTIVE, provider.name());
            }
        }
    }
}
