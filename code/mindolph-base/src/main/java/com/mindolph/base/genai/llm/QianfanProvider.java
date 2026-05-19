package com.mindolph.base.genai.llm;

import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;

/**
 * Baidu Qianfan models
 *
 * @since 1.15
 */
public class QianfanProvider extends OpenAiProvider {

    private static final String BASE_URL = "https://qianfan.baidubce.com/v2";

    public QianfanProvider(ProviderMeta providerMeta, ModelMeta modelMeta) {
        super(providerMeta, modelMeta);
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }
}
