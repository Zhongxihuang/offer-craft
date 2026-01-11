package com.workspace.codeforgeai;

import com.workspace.codeforgeai.ai.CodeForgeAi;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CodeForgeAiApplicationTests {

    @Resource
    private CodeForgeAi codeForgeAi;


    @Test
    void chat() {
        codeForgeAi.chat("Hello, I am a program tester!");
    }

    @Test
    void testChatWithMessage() {
        UserMessage userMessage = UserMessage.from(
                TextContent.from("Image description"),
                ImageContent.from("https://docs.langchain4j.info/img/logo.svg")
        );
        codeForgeAi.chatWithMessage(userMessage);
    }
}
