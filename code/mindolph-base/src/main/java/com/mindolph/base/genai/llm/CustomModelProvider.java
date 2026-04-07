package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * OpenAI like custom model API provider.
 * @since 1.14.1
 */
public class CustomModelProvider extends OpenAiProvider {

    public CustomModelProvider(ProviderMeta providerMeta, ModelMeta modelMeta) {
        super(providerMeta, modelMeta);
    }

    @Override
    protected ChatModel buildAI(GenAiEvents.Input input) {
        ModelMeta actualModelMeta = determineModel(input, super.modelMeta);
        return buildChatModel(super.providerMeta, actualModelMeta, input.temperature(), super.proxyMeta, super.proxyEnabled).a();
    }

    @Override
    protected StreamingChatModel buildStreamingAI(GenAiEvents.Input input) {
        ModelMeta actualModelMeta = determineModel(input, super.modelMeta);
        return buildStreamingChatModel(super.providerMeta, actualModelMeta, input.temperature(), super.proxyMeta, super.proxyEnabled).a();
    }
}

