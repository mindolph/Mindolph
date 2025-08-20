package com.mindolph.base.genai.rag;

import com.mindolph.base.genai.GenAiEvents.Input;
import com.mindolph.base.genai.InputBuilder;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.llm.LlmProvider;
import com.mindolph.base.genai.llm.LlmProviderFactory;
import com.mindolph.base.genai.llm.OutputParams;
import com.mindolph.core.constant.GenAiConstants;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.ProviderMeta;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * To utilize the RAG from LangChain4j, an adapter for LangChain's chat model is needed, although it seems redundant,
 * since the LLM provider implementations didn't follow the LangChain4j practices.
 *
 * @since unknown
 */
public class StreamingChatModelAdapter implements StreamingChatModel {

    private static final Logger log = LoggerFactory.getLogger(StreamingChatModelAdapter.class);

    private final LlmProvider llmProvider;
    private final AgentMeta agentMeta;

    public StreamingChatModelAdapter(AgentMeta agentMeta) {
        this.agentMeta = agentMeta;
        String providerName = agentMeta.getProvider().getName();
        ProviderMeta providerMeta = LlmConfig.getIns().loadProviderMeta(providerName);
        ProviderMeta propsForAgent = new ProviderMeta(providerMeta.apiKey(), providerMeta.baseUrl(), providerMeta.aiModel(), providerMeta.useProxy());
        this.llmProvider = LlmProviderFactory.create(agentMeta.getProvider().getName(), propsForAgent);
    }

    @Override
    public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
        log.debug("Do chat with LLM");
        List<ChatMessage> messages = chatRequest.messages();
        List<String> msgs = messages.stream().map(chatMessage -> {
            if (chatMessage instanceof AiMessage am) {
                return "AI: " + am.text();
            }
            else if (chatMessage instanceof UserMessage um) {
                return "Human: " + StringUtils.join(um.contents(), ", ");
            }
            else if (chatMessage instanceof SystemMessage sm) {
                return "System: " + sm.text();
            }
            else {
                return StringUtils.EMPTY;
            }
        }).toList();
        String collectedMsg = String.join("\n", msgs);
        Input input = new InputBuilder().text(collectedMsg)
                .model(agentMeta.getChatModel().getName()).maxTokens(agentMeta.getChatModel().maxTokens())
                .isStreaming(true).temperature(0.5f).createInput();
        OutputParams oparams = new OutputParams(null, GenAiConstants.OutputFormat.TEXT);
        this.llmProvider.stream(input, oparams, streamToken -> {
            if (streamToken.isError()) {
                log.error("Streaming error: {}", streamToken.text());
                handler.onError(new RuntimeException(streamToken.text()));
            }
            else {
                if (streamToken.isStop()) {
                    ChatResponse resp = ChatResponse.builder().aiMessage(new AiMessage(streamToken.text()))
                            .tokenUsage(new TokenUsage(streamToken.outputTokens()))
                            .finishReason(FinishReason.STOP).build();
                    handler.onCompleteResponse(resp);
                }
                else {
                    handler.onPartialResponse(streamToken.text());
                }
            }
        });
    }
}
