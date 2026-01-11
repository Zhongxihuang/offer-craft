package com.workspace.codeforgeai.ai;

import com.workspace.codeforgeai.ai.tools.InterviewQuestionTool;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodeForgeAiServiceFactory {

    @Resource
    private ChatModel myQwenChatModel;

    @Resource
    private ContentRetriever contentRetriever;

    @Resource
    private McpToolProvider mcpToolProvider;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    @Bean
    public CodeForgeAiService codeForgeAiService(){
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // build
        CodeForgeAiService codeForgeAiService = AiServices.builder(CodeForgeAiService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10)) // store independently
                .contentRetriever(contentRetriever) // RAG
                .tools(new InterviewQuestionTool()) // run with tools
                .toolProvider(mcpToolProvider) // use MCP
                .build();
        return codeForgeAiService;

    }
}

