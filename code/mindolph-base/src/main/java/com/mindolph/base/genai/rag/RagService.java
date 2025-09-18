package com.mindolph.base.genai.rag;

import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.mfx.util.GlobalExecutor;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.DatasetMeta;
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
 * @since 1.13.0
 */
public class RagService extends BaseEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private static RagService instance;

    private StreamingChatModelAdapter streamingChatModelAdapter;

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
        chatMemory = MessageWindowChatMemory.withMaxMessages(10);
    }

    /**
     * @param agentMeta
     * @param finished
     */
    public void useAgent(AgentMeta agentMeta, Consumer<Object> finished) {
        if (!agentMeta.isAllSetup()) {
            finished.accept(new RuntimeException("Agent is not well setup"));
            return;
        }
        // load here for the config might be changed on the fly.
        super.loadVectorStorePrefs();
        if (vectorStoreMeta == null || !vectorStoreMeta.isAllSetup()) {
            finished.accept(new RuntimeException("Vector store is not well setup"));
        }
        log.info("use agent: {}, with chat model {}-{} and embedding model {}-{}",
                agentMeta.getName(), agentMeta.getChatProvider().getDisplayName(), agentMeta.getChatModel() , agentMeta.getEmbeddingProvider().getDisplayName(), agentMeta.getEmbeddingModel());
        GlobalExecutor.submit(() -> {
            try {
                this.switchModel(agentMeta, o -> {
                    if (o instanceof Exception) {
                        finished.accept(o);
                    }
                    else {
                        this.streamingChatModelAdapter = new StreamingChatModelAdapter(agentMeta);
                        this.contentRetriever = this.buildContentRetriever(agentMeta.getId());
                        if (this.contentRetriever == null) {
                            finished.accept(new RuntimeException("Unable to use this agent"));
                            return;
                        }
                        agent = AiServices.builder(Agent.class)
                                .streamingChatModel(streamingChatModelAdapter)
                                .contentRetriever(contentRetriever)
                                .chatMemory(chatMemory)
                                .build();
                        log.debug(o.toString());
                        finished.accept("Switched to agent %s".formatted(agentMeta.getName()));
                    }
                });

            } catch (Exception e) {
                finished.accept(e);
            }
        });
    }

    public void switchModel(AgentMeta agentMeta, Consumer<Object> completed) {
        log.debug("Switch language and embedding model to %s, %s ".formatted(agentMeta.getLanguageCode(), agentMeta.getEmbeddingModel()));
        embeddingModel = super.createEmbeddingModel(agentMeta.getLanguageCode(), agentMeta.getEmbeddingModel());
        try {
            embeddingStore = super.createEmbeddingStore(embeddingModel, true, false);
        } catch (Exception e) {
            log.error("Failed create embedding store", e);
            throw new RuntimeException("Failed to prepare embedding store, check vector store setting or network", e);
        }
        completed.accept("RAG model is ready");
    }

    public void chat(String message, Consumer<TokenStream> consumer) {
        log.info("Human: {}", message);
        if (agent == null) {
            throw new RuntimeException("Use agent before chatting");
        }
        GlobalExecutor.submit(() -> {
            try {
                TokenStream tokenStream = agent.chat(message);
                consumer.accept(tokenStream);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    public void stop() {
        this.streamingChatModelAdapter.setStop(true);
    }

    private ContentRetriever buildContentRetriever(String agentId) {
        List<String> docIds = this.findDocIdsByAgent(agentId);
        if (docIds == null || docIds.isEmpty()) {
            log.warn("No docs found for agent {}, the agent might not work.", agentId);
            throw new RuntimeException("No docs found for agent");
        }
        if (embeddingStore == null || embeddingModel == null) {
            log.warn("No embedding store or embedding model for agent {}", agentId);
            throw new RuntimeException("No embedding store or embedding model for agent");
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
        List<DatasetMeta> datasetMetas = LlmConfig.getIns().getDatasetsFromAgentId(agentId);
        if (datasetMetas == null || datasetMetas.isEmpty()) {
            log.warn("No datasets are set for agent {}", agentId);
            return null;
        }
        List<String> docIdList = super.withJdbcConnection(connection -> {
            String params = StringUtils.repeat("?", ",", datasetMetas.size());
            String sql = "select id, file_name, dataset_id from mindolph_doc where dataset_id in (%s)".formatted(params);
            log.debug("findDocIdsByAgent sql: {}", sql);
            List<String> docIds = new ArrayList<>();
            try {
                PreparedStatement ps = connection.prepareStatement(sql);
                List<String> ids = datasetMetas.stream().map(DatasetMeta::getId).toList();
                for (int i = 0; i < ids.size(); i++) {
                    ps.setString(i + 1, ids.get(i));
                }
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

}
