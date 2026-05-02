package com.workspace.codeforgeai.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "career.ai.mode=provider")
@EnabledIfEnvironmentVariable(named = "DASHSCOPE_API_KEY", matches = ".+")
class CodeForgeAiServiceTest {

    @Autowired
    private CodeForgeAiService codeForgeAiService;

    @Test
    void chat() {
        String result = codeForgeAiService.chat("Hello, I am an AI programmer.");
        System.out.println(result);
    }

    @Test
    void chatWithMemory() {
        String result = codeForgeAiService.chat("Hello, I am an AI programmer Alice.");
        System.out.println(result);
        result = codeForgeAiService.chat("Who am I?");
        System.out.println(result);

    }

    @Test
    void chatForReport() {
        String userMessage = codeForgeAiService.chat("Hello, I am an AI programmer Alice learning programming for one year. Please guide me to study.");
        CodeForgeAiService.Report report = codeForgeAiService.chatForReport(userMessage);
        System.out.println(report);

    }

    @Test
    void chatWithRag(){
        String result = codeForgeAiService.chat("How to learn java? What are common questions in interview? ");
        System.out.println(result);
    }

    @Test
    void chatWithTools() {
        String result = codeForgeAiService.chat("What are common questions in interview?");
        System.out.println(result);

    }

    @Test
    @EnabledIfEnvironmentVariable(named = "BIGMODEL_API_KEY", matches = ".+")
    void chatWithMcp() {
        String result = codeForgeAiService.chat("What is leetcode?");
        System.out.println(result);

    }

    @Test
    void chatWithGuardrail() {
        String result = codeForgeAiService.chat("kill them all!");
        System.out.println(result);

    }
}
