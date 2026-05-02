package com.workspace.codeforgeai.ai;

import com.workspace.codeforgeai.ai.tools.InterviewQuestionTool;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "career.ai", name = "mode", havingValue = "provider")
public class CodeForgeAiServiceFactory {

    private final ChatModel myQwenChatModel;
    private final ContentRetriever contentRetriever;
    private final StreamingChatModel qwenStreamingChatModel;
    private final ObjectProvider<McpToolProvider> mcpToolProvider;

    public CodeForgeAiServiceFactory(ChatModel myQwenChatModel,
                                     ContentRetriever contentRetriever,
                                     StreamingChatModel qwenStreamingChatModel,
                                     ObjectProvider<McpToolProvider> mcpToolProvider) {
        this.myQwenChatModel = myQwenChatModel;
        this.contentRetriever = contentRetriever;
        this.qwenStreamingChatModel = qwenStreamingChatModel;
        this.mcpToolProvider = mcpToolProvider;
    }

    @Bean
    public CodeForgeAiService codeForgeAiService(){
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        AiServices<CodeForgeAiService> builder = AiServices.builder(CodeForgeAiService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10)) // store independently
                .contentRetriever(contentRetriever) // RAG
                .tools(new InterviewQuestionTool()); // run with tools

        McpToolProvider provider = mcpToolProvider.getIfAvailable();
        if (provider != null) {
            builder.toolProvider(provider);
        }

        return builder.build();

    }
}

