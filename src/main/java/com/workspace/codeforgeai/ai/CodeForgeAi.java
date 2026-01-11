package com.workspace.codeforgeai.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CodeForgeAi {
    @Resource
    private ChatModel qwenChatModel;
    private static final String SYSTEM_MESSSAGE = """
            """;

    // Simple talk
    public String chat(String message) {
        SystemMessage systemMessage = SystemMessage.from(SYSTEM_MESSSAGE);

        UserMessage userMessage = UserMessage.from(message);
        ChatResponse chatResponse = qwenChatModel.chat(userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("AI output: " + aiMessage.toString());
        return aiMessage.text();
    }
    // user define
    public String chatWithMessage(UserMessage userMessage) {
        ChatResponse chatResponse = qwenChatModel.chat(userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("AI output: " + aiMessage.toString());
        return aiMessage.text();
    }
}
