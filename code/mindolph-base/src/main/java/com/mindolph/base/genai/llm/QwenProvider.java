package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.langchain.QwenLangChainSupport;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7.2
 */
public class QwenProvider extends BaseLangChainLlmProvider implements QwenLangChainSupport {

    public QwenProvider(ProviderMeta providerMeta, ModelMeta modelMeta) {
        super(providerMeta, modelMeta);
    }

    @Override
    protected ChatModel buildAI(Input input) {
        ModelMeta actualModelMeta = determineModel(input, super.modelMeta);
        return buildChatModel(super.providerMeta, actualModelMeta, input.temperature(), super.proxyMeta, super.proxyEnabled).a();
    }

    @Override
    protected StreamingChatModel buildStreamingAI(Input input) {
        ModelMeta actualModelMeta = determineModel(input, super.modelMeta);
        return buildStreamingChatModel(super.providerMeta, actualModelMeta, input.temperature(), super.proxyMeta, super.proxyEnabled).a();
    }

    @Override
    protected String extractErrorMessage(Throwable throwable) {
        return extractErrorMessageFromLLM(throwable.getMessage());
    }
}
