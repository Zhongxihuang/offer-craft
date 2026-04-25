package com.workspace.codeforgeai.career.workflow;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WorkflowCapabilityStatus {

    private final String mode;
    private final ObjectProvider<ContentRetriever> contentRetriever;
    private final ObjectProvider<McpToolProvider> mcpToolProvider;

    public WorkflowCapabilityStatus(@Value("${career.ai.mode:demo}") String mode,
                                    ObjectProvider<ContentRetriever> contentRetriever,
                                    ObjectProvider<McpToolProvider> mcpToolProvider) {
        this.mode = mode;
        this.contentRetriever = contentRetriever;
        this.mcpToolProvider = mcpToolProvider;
    }

    public boolean isDemoMode() {
        return !"provider".equalsIgnoreCase(mode);
    }

    public boolean retrievalAvailable() {
        return !isDemoMode() && contentRetriever.getIfAvailable() != null;
    }

    public boolean searchAvailable() {
        return !isDemoMode() && mcpToolProvider.getIfAvailable() != null;
    }
}
