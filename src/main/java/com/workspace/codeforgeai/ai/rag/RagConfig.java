package com.workspace.codeforgeai.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provider-mode RAG configuration with lazy, fail-soft initialization.
 */
@Configuration
@ConditionalOnProperty(prefix = "career.ai", name = "mode", havingValue = "provider")
@Slf4j
public class RagConfig {

    private final EmbeddingModel qwenEmbeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ResourcePatternResolver resourcePatternResolver;

    public RagConfig(EmbeddingModel qwenEmbeddingModel,
                     EmbeddingStore<TextSegment> embeddingStore,
                     ResourcePatternResolver resourcePatternResolver) {
        this.qwenEmbeddingModel = qwenEmbeddingModel;
        this.embeddingStore = embeddingStore;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @Bean
    public ContentRetriever contentRetriever() {
        return new LazyContentRetriever(this::buildRetriever, noOpRetriever());
    }

    private ContentRetriever buildRetriever() {
        List<Document> documents = loadDocuments();
        if (documents.isEmpty()) {
            log.warn("RAG document corpus is empty. Continuing without retrieval.");
            return noOpRetriever();
        }

        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(1000, 200);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .textSegmentTransformer(textSegment -> TextSegment.from(
                        textSegment.metadata().getString("file_name") + "\n" + textSegment.text(),
                        textSegment.metadata()
                ))
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(documents);
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(5)
                .minScore(0.75)
                .build();
    }

    private List<Document> loadDocuments() {
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath*:docs/**/*");
            List<Document> documents = new ArrayList<>();
            for (Resource resource : resources) {
                if (!resource.isReadable()) {
                    continue;
                }

                try (InputStream inputStream = resource.getInputStream()) {
                    String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
                    if (text.isEmpty()) {
                        continue;
                    }
                    documents.add(Document.from(text, Metadata.from(Document.FILE_NAME, resource.getFilename())));
                }
            }
            return documents;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load classpath RAG documents.", exception);
        }
    }

    private ContentRetriever noOpRetriever() {
        return query -> List.of();
    }

    private static final class LazyContentRetriever implements ContentRetriever {

        private final ThrowingSupplier<ContentRetriever> initializer;
        private final ContentRetriever fallback;
        private volatile ContentRetriever delegate;
        private final AtomicBoolean initialized = new AtomicBoolean(false);

        private LazyContentRetriever(ThrowingSupplier<ContentRetriever> initializer,
                                     ContentRetriever fallback) {
            this.initializer = initializer;
            this.fallback = fallback;
        }

        @Override
        public List<Content> retrieve(Query query) {
            ContentRetriever current = delegate;
            if (current == null && initialized.compareAndSet(false, true)) {
                try {
                    delegate = initializer.get();
                } catch (Exception exception) {
                    log.warn("RAG initialization failed. Continuing without retrieval: {}", exception.getMessage());
                    delegate = fallback;
                }
                current = delegate;
            } else if (current == null) {
                current = fallback;
            }
            return current.retrieve(query);
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
