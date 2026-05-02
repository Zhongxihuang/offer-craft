package com.workspace.codeforgeai;

import com.workspace.codeforgeai.ai.CodeForgeAi;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "career.ai.mode=provider")
@EnabledIfEnvironmentVariable(named = "DASHSCOPE_API_KEY", matches = ".+")
class CodeForgeAiApplicationTests {

    @Resource
    private CodeForgeAi codeForgeAi;


    @Test
    void chat() {
        codeForgeAi.chat("Hello, I am a program tester!");
    }

    @Test
    void testChatWithMessage() {
        UserMessage userMessage = UserMessage.from(TextContent.from("Give me a short hello."));
        codeForgeAi.chatWithMessage(userMessage);
    }
}
