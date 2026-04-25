package com.workspace.codeforgeai.career.config;

import com.workspace.codeforgeai.ai.tools.InterviewQuestionTool;
import com.workspace.codeforgeai.career.candidate.CandidateAnalysisAiService;
import com.workspace.codeforgeai.career.gap.GapAnalysisAiService;
import com.workspace.codeforgeai.career.interview.InterviewPrepAiService;
import com.workspace.codeforgeai.career.jd.JdParsingAiService;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "career.ai", name = "mode", havingValue = "provider")
public class CareerWorkflowServiceFactory {

    private final ChatModel myQwenChatModel;
    private final ContentRetriever contentRetriever;
    private final ObjectProvider<McpToolProvider> mcpToolProvider;

    public CareerWorkflowServiceFactory(ChatModel myQwenChatModel,
                                        ContentRetriever contentRetriever,
                                        ObjectProvider<McpToolProvider> mcpToolProvider) {
        this.myQwenChatModel = myQwenChatModel;
        this.contentRetriever = contentRetriever;
        this.mcpToolProvider = mcpToolProvider;
    }

    @Bean
    public JdParsingAiService jdParsingAiService() {
        return AiServices.builder(JdParsingAiService.class)
                .chatModel(myQwenChatModel)
                .build();
    }

    @Bean
    public CandidateAnalysisAiService candidateAnalysisAiService() {
        return AiServices.builder(CandidateAnalysisAiService.class)
                .chatModel(myQwenChatModel)
                .build();
    }

    @Bean
    public GapAnalysisAiService gapAnalysisAiService() {
        return AiServices.builder(GapAnalysisAiService.class)
                .chatModel(myQwenChatModel)
                .build();
    }

    @Bean
    public InterviewPrepAiService interviewPrepAiService() {
        AiServices<InterviewPrepAiService> builder = AiServices.builder(InterviewPrepAiService.class)
                .chatModel(myQwenChatModel)
                .contentRetriever(contentRetriever)
                .tools(new InterviewQuestionTool());

        McpToolProvider provider = mcpToolProvider.getIfAvailable();
        if (provider != null) {
            builder.toolProvider(provider);
        }

        return builder.build();
    }
}
