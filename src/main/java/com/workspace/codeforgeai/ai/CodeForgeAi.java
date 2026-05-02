package com.workspace.codeforgeai.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "career.ai", name = "mode", havingValue = "provider")
@Slf4j
public class CodeForgeAi {
    @Resource
    private ChatModel myQwenChatModel;
    private static final String SYSTEM_MESSSAGE = """
            """;

    // Simple talk
    public String chat(String message) {
        SystemMessage systemMessage = SystemMessage.from(SYSTEM_MESSSAGE);

        UserMessage userMessage = UserMessage.from(message);
        ChatResponse chatResponse = myQwenChatModel.chat(userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("AI output: " + aiMessage.toString());
        return aiMessage.text();
    }
    // user define
    public String chatWithMessage(UserMessage userMessage) {
        ChatResponse chatResponse = myQwenChatModel.chat(userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("AI output: " + aiMessage.toString());
        return aiMessage.text();
    }
}
