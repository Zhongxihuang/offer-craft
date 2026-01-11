package com.workspace.codeforgeai.controller;


import com.workspace.codeforgeai.ai.CodeForgeAiService;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private CodeForgeAiService codeForgeAiService;

    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(int memoryId, String message) {
        return codeForgeAiService.chatWithStream(memoryId, message)
                .map(chunk -> ServerSentEvent.<String>builder()
                .data(chunk)
                .build());
    }
}
