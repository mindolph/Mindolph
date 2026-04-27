package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents;
import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * Use API instead of Dashscope.
 *
 * @since 1.14.2
 */
public class QwenProvider2 extends OpenAiProvider {

    private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    public QwenProvider2(ProviderMeta providerMeta, ModelMeta modelMeta) {
        super(providerMeta, modelMeta);
    }

    @Override
    protected ChatModel buildAI(Input input) {
        ModelMeta actualModelMeta = determineModel(input, super.modelMeta);
        super.providerMeta.setBaseUrl(BASE_URL);
        return buildChatModel(super.providerMeta, actualModelMeta, input.temperature(), super.proxyMeta, false).a();
    }

    @Override
    protected StreamingChatModel buildStreamingAI(Input input) {
        ModelMeta actualModelMeta = determineModel(input, super.modelMeta);
        super.providerMeta.setBaseUrl(BASE_URL);
        return buildStreamingChatModel(super.providerMeta, actualModelMeta, input.temperature(), super.proxyMeta, false).a();
    }
}
