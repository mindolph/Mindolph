package com.mindolph.base.genai.rag;

import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.genai.llm.LlmConfig;
import com.mindolph.base.genai.llm.OkHttpClientAdapter;
import com.mindolph.base.genai.model.LangChainSupport;
import com.mindolph.base.util.NetworkUtils;
import com.mindolph.core.config.ProxyMeta;
import com.mindolph.core.constant.GenAiModelProvider;
import com.mindolph.core.llm.AgentMeta;
import com.mindolph.core.llm.DatasetMeta;
import com.mindolph.core.llm.ModelMeta;
import com.mindolph.core.llm.ProviderMeta;
import com.mindolph.core.util.Tuple2;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mfx.util.GlobalExecutor;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
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

    private StreamingChatModel streamingChatModel;

    // Since the LangChain doesn't support cancel/stop the running http request(sometimes SSE), the OkHttpClientAdapter has to be here to do that.
    private OkHttpClientAdapter okHttpClientAdapter;

    private LangChainSupport langChainSupport;

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
        if (!agentMeta.isAllNecessarySetup()) {
            finished.accept(new RuntimeException("Agent is not well setup"));
            return;
        }
        // load here for the config might be changed on the fly.
        super.loadVectorStorePrefs();
        if (vectorStoreMeta == null || !vectorStoreMeta.isAllSetup()) {
            finished.accept(new RuntimeException("Vector store is not well setup"));
        }
        log.info("use agent: {}, with chat model {}-{}",
                agentMeta.getName(), agentMeta.getChatProvider().getDisplayName(), agentMeta.getChatModel());

        if (agentMeta.isAllSetup()) {
            log.info("  and with embedding model: %s".formatted(agentMeta.getEmbeddingProvider().getDisplayName()), agentMeta.getEmbeddingModel());
        }

        GlobalExecutor.submit(() -> {
            try {
                if (agentMeta.isAllSetup()) {
                    // build agent with embedding
                    this.switchModel(agentMeta, errorOrMessage -> {
                        if (errorOrMessage instanceof Exception) {
                            finished.accept(errorOrMessage);
                        }
                        else {
                            this.streamingChatModel = this.buildStreamingChatModel(agentMeta);
                            this.contentRetriever = this.buildContentRetriever(agentMeta.getId());
                            if (this.contentRetriever == null) {
                                finished.accept(new RuntimeException("Unable to use this agent"));
                                return;
                            }
                            agent = AiServices.builder(Agent.class)
                                    .streamingChatModel(streamingChatModel)
                                    .systemMessageProvider(o -> agentMeta.getPromptTemplate())
                                    .contentRetriever(contentRetriever)
                                    .chatMemory(chatMemory)
                                    .build();
                            log.debug(errorOrMessage.toString());
                            finished.accept("Switched to agent %s".formatted(agentMeta.getName()));
                        }
                    });
                }
                else {
                    // build agent as a chatbot.
                    this.streamingChatModel = this.buildStreamingChatModel(agentMeta);
                    AiServices<Agent> builder = AiServices.builder(Agent.class)
                            .streamingChatModel(streamingChatModel)
                            .systemMessageProvider(o -> agentMeta.getPromptTemplate())
                            .chatMemory(chatMemory);
                    agent = builder.build();
                    finished.accept("Switched to agent %s".formatted(agentMeta.getName()));
                }
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
        if (this.streamingChatModel instanceof StreamingChatModelAdapter scma) {
            scma.setStop(true);
        }
        else {
            if (okHttpClientAdapter != null) {
                okHttpClientAdapter.close();
            }
        }
    }

    public String extractErrorMessageFromLLM(GenAiModelProvider provider, String llmMsg) {
        if (this.supportedByLangchain(provider) && langChainSupport != null) {
            try {
                return langChainSupport.extractErrorMessageFromLLM(llmMsg);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                return llmMsg;
            }
        }
        else {
            return llmMsg;
        }
    }

    private StreamingChatModel buildStreamingChatModel(AgentMeta agentMeta) {
        GenAiModelProvider provider = agentMeta.getChatProvider();
        if (this.supportedByLangchain(provider)) {
            this.langChainSupport = LangChainSupport.createSupport(provider);
            ProviderMeta providerMeta = LlmConfig.getIns().loadProviderMeta(provider.name());
            ModelMeta modelMeta = LlmConfig.getIns().lookupModel(provider.name(), agentMeta.getChatModel());
            Boolean proxyEnabled = FxPreferences.getInstance().getPreference(PrefConstants.GENERAL_PROXY_ENABLE, false);
            ProxyMeta proxyMeta = NetworkUtils.getProxyMeta();
            // The Input is not for inputting but for compatible with the providers for Generate&Summarize features;
            // TODO temperature should be parameterized.
            Tuple2<StreamingChatModel, OkHttpClientAdapter> tuple = langChainSupport.buildStreamingChatModel(providerMeta, modelMeta, 0.5f, proxyMeta, proxyEnabled);
            this.okHttpClientAdapter = tuple.b();
            return tuple.a();
        }
        else {
            // TODO how about extracting err message like LangChainSupport?
            return new StreamingChatModelAdapter(agentMeta);
        }
    }

    private boolean supportedByLangchain(GenAiModelProvider genAiModelProvider) {
        // NOTE: some providers are supported by LangChain4j but not with streaming like ChatGLM, they are excluded.
        // some providers support like HuggingFace don't include some features like streaming, they are also excluded.
        return GenAiModelProvider.ALI_Q_WEN == genAiModelProvider
                || GenAiModelProvider.OPEN_AI == genAiModelProvider
                || GenAiModelProvider.OLLAMA == genAiModelProvider
                || GenAiModelProvider.GEMINI == genAiModelProvider;
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
