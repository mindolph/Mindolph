package com.mindolph.fx.data;

import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.util.Tuple2;
import com.mindolph.mfx.preference.FxPreferences;

/**
 * Migrate active model to new preference.
 *
 * @since 1.13
 */
public class MigrationV4 implements Migration {

    @Override
    public int getVersion() {
        return 4;
    }

    @Override
    public void doMigration() {
        migrateActiveModel();
    }

    private static void migrateActiveModel() {
        Tuple2<GenAiModelProvider, String> providerModel = LlmConfig.getIns().getActiveProviderMeta();
        if (providerModel != null) {
            FxPreferences.getInstance().savePreference(PrefConstants.GEN_AI_GENERATE_MODEL, "%s:%s".formatted(providerModel.a(), providerModel.b()));
            FxPreferences.getInstance().savePreference(PrefConstants.GEN_AI_SUMMARIZE_MODEL, "%s:%s".formatted(providerModel.a(), providerModel.b()));
        }
    }
}
