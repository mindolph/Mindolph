package com.mindolph.base.genai.llm;

import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;

/**
 * Use API instead of Dashscope.
 *
 * @since 1.14.2
 */
public class QwenProvider2 extends OpenAiProvider {

//    private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    public QwenProvider2(ProviderMeta providerMeta, ModelMeta modelMeta) {
        super(providerMeta, modelMeta);
    }

//    @Override
//    public String getBaseUrl() {
//        return BASE_URL;
//    }

}
