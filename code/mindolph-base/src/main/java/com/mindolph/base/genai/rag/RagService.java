package com.mindolph.base.genai.rag;

import com.mindolph.core.llm.AgentMeta;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @since unknown
 */
public class RagService extends BaseEmbedding {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private static RagService instance;

    private StreamingLanguageModelAdapter streamingLanguageModelAdapter;

    private Agent agent;

    private ContentRetriever contentRetriever;
    private EmbeddingModel embeddingModel;
    private EmbeddingStore<TextSegment> embeddingStore;

    private final ChatMemory chatMemory;

    public static RagService getInstance() {
        if (instance == null) {
            instance = new RagService();
        }
        return instance;
    }

    private RagService() {
        embeddingModel = super.createEmbeddingModel();
        embeddingStore = super.createEmbeddingStore(true, false);
        chatMemory = MessageWindowChatMemory.withMaxMessages(10);
    }

    private ContentRetriever buildContentRetriever(String agentId) {
        List<String> docIds = this.findDocIdsByAgent(agentId);
        if (docIds == null || docIds.isEmpty()) {
            log.warn("No docs found for agent {}, the agent might not work.", agentId);
        }
        log.debug("Build content retriever with filter on doc ids: {}.", StringUtils.join(docIds, ","));
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .filter(new MetadataFilterBuilder("doc_id").isIn(docIds))
                .maxResults(2) // on each interaction we will retrieve the 2 most relevant segments
                .minScore(0.5) // we want to retrieve segments at least somewhat similar to user query
                .build();
    }

    private List<String> findDocIdsByAgent(String agentId) {
        List<String> docIdList = super.withJdbcConnection(connection -> {
            String sql = "select id, file_name, agent_id from mindolph_doc where agent_id = ?";
            List<String> docIds = new ArrayList<>();
            try {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, agentId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    docIds.add(rs.getString(1));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return docIds;
        });
        return docIdList;
    }

    public void useAgent(AgentMeta agentMeta, Runnable ready) {
        log.info("use agent: {}, with LLM {}-{}", agentMeta.getName(), agentMeta.getProvider().getName(), agentMeta.getChatModel().name());
        new Thread(() -> {
            this.streamingLanguageModelAdapter = new StreamingLanguageModelAdapter(agentMeta);
            this.contentRetriever = this.buildContentRetriever(agentMeta.getId());
            agent = AiServices.builder(Agent.class)
                    .streamingChatLanguageModel(streamingLanguageModelAdapter)
                    .contentRetriever(contentRetriever)
                    .chatMemory(chatMemory)
                    .build();
            ready.run();
        }).start();
    }

    public void chat(String message, Consumer<TokenStream> consumer) {
        log.info("Human: {}", message);
        if (agent == null) {
            throw new RuntimeException("Use agent before chatting");
        }
        new Thread(() -> {
            TokenStream tokenStream = agent.chat(message);
            consumer.accept(tokenStream);
        }).start();
    }
}
