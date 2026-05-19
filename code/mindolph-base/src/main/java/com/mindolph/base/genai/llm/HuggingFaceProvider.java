package com.mindolph.base.genai.llm;

import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;

/**
 * HuggingFace requires proxy but LangChain doesn't support it yet.
 *
 */
public class HuggingFaceProvider extends OpenAiProvider {

    private static final String BASE_URL = "https://router.huggingface.co/v1";

    public HuggingFaceProvider(ProviderMeta providerMeta, ModelMeta modelMeta) {
        super(providerMeta, modelMeta);
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

}

