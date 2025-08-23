package com.mindolph.genai;

import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.util.Tuple2;
import com.mindolph.mfx.preference.FxPreferences;
import org.apache.commons.lang3.StringUtils;

/**
 * @since 1.11.4
 */
public class GenAiUtils {

    public static String displayGenAiTokens(int tokenCount) {
        return "%sK".formatted(tokenCount / 1024);
    }

    /**
     * Parse provider and model from preference.
     *
     * @param prefKey
     * @return
     * @since unknown
     */
    public static Tuple2<GenAiModelProvider, ModelMeta> parseModelPreference(String prefKey) {
        String preferenceValue = FxPreferences.getInstance().getPreference(prefKey, String.class);
        if (StringUtils.isNotBlank(preferenceValue)) {
            String[] split = StringUtils.split(preferenceValue, ":");
            return new Tuple2<>(GenAiModelProvider.fromName(split[0]), new ModelMeta(split[1], 0)); // maxTokens doesn't work here.
        }
        return null;
    }
}
