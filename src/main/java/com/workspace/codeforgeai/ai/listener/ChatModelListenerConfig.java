package com.workspace.codeforgeai.ai.listener;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ChatModelListenerConfig {

    @Bean
    ChatModelListener chatModelListener() {
        return new ChatModelListener() {
            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                log.debug("Chat model request started.");
            }

            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                log.debug("Chat model response received.");
            }

            @Override
            public void onError(ChatModelErrorContext errorContext) {
                log.warn("Chat model request failed: {}", errorContext.error().getMessage());
            }
        };
    }
}
