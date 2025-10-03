package com.mindolph.base.genai.llm;

import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.langchain.OllamaLangChainSupport;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.core.util.Tuple2;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7.3
 */
public class OllamaProvider extends BaseLangChainLlmProvider implements OllamaLangChainSupport {

    public OllamaProvider(ProviderMeta providerMeta, ModelMeta modelMeta) {
        super(providerMeta, modelMeta);
    }

    @Override
    protected ChatModel buildAI(Input input) {
        ModelMeta actualModelMeta = determineModel(input, super.modelMeta);
        Tuple2<ChatModel, OkHttpClientAdapter> tp2 = buildChatModel(super.providerMeta, actualModelMeta, input.temperature(), super.proxyMeta, super.proxyEnabled);
        return tp2.a();
    }

    @Override
    protected StreamingChatModel buildStreamingAI(Input input) {
        ModelMeta actualModelMeta = determineModel(input, super.modelMeta);
        Tuple2<StreamingChatModel, OkHttpClientAdapter> tp2 = buildStreamingChatModel(super.providerMeta, actualModelMeta, input.temperature(), super.proxyMeta, super.proxyEnabled);
        return tp2.a();
    }

    @Override
    protected String extractErrorMessage(Throwable throwable) {
        return throwable.getLocalizedMessage();
    }
}
