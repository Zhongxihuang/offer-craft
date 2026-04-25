package com.workspace.codeforgeai.ai.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "career.ai", name = "mode", havingValue = "provider")
public class McpConfig {

    @Value("${bigmodel.api-key}")
    private String apiKey;

    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${bigmodel.api-key:}')")
    public McpToolProvider mcpToolProvider() {
        // transport with MCP
        McpTransport transport = new HttpMcpTransport.Builder()
                .sseUrl("https://open.bigmodel.cn/api/mcp/web_search/sse?Authorization=" + apiKey)
                .logRequests(true) // open logs
                .logResponses(true)
                .build();
        // create MCP client
        McpClient mcpClient = new DefaultMcpClient.Builder()
                .key("bigmodel-web-search")
                .transport(transport)
                .build();
        // get tools from MCP client
        McpToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClient)
                .build();
        return toolProvider;
    }
}
