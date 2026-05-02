package com.workspace.codeforgeai.ai.model;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "career.ai", name = "mode", havingValue = "provider")
public class QwenChatModelConfig {

    @Value("${langchain4j.community.dashscope.chat-model.model-name:qwen-max}")
    private String chatModelName;

    @Value("${langchain4j.community.dashscope.chat-model.api-key:}")
    private String apiKey;

    @Value("${langchain4j.community.dashscope.chat-model.base-url:}")
    private String baseUrl;

    @Value("${langchain4j.community.dashscope.streaming-chat-model.model-name:qwen-max}")
    private String streamingModelName;

    @Value("${langchain4j.community.dashscope.embedding-model.model-name:text-embedding-v4}")
    private String embeddingModelName;

    @Resource
    private ChatModelListener chatModelListener;

    @Bean
    public ChatModel myQwenChatModel() {
        validateApiKey();
        QwenChatModel.QwenChatModelBuilder builder = QwenChatModel.builder()
                .modelName(chatModelName)
                .apiKey(apiKey)
                .listeners(List.of(chatModelListener));

        if (StringUtils.hasText(baseUrl)) {
            builder.baseUrl(baseUrl);
        }

        return builder.build();
    }

    @Bean
    public StreamingChatModel qwenStreamingChatModel() {
        validateApiKey();
        QwenStreamingChatModel.QwenStreamingChatModelBuilder builder = QwenStreamingChatModel.builder()
                .modelName(streamingModelName)
                .apiKey(apiKey)
                .listeners(List.of(chatModelListener));

        if (StringUtils.hasText(baseUrl)) {
            builder.baseUrl(baseUrl);
        }

        return builder.build();
    }

    @Bean
    public EmbeddingModel qwenEmbeddingModel() {
        validateApiKey();
        QwenEmbeddingModel.QwenEmbeddingModelBuilder builder = QwenEmbeddingModel.builder()
                .modelName(embeddingModelName)
                .apiKey(apiKey);

        if (StringUtils.hasText(baseUrl)) {
            builder.baseUrl(baseUrl);
        }

        return builder.build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    private void validateApiKey() {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("DASHSCOPE_API_KEY must be set when career.ai.mode=provider.");
        }
    }
}
