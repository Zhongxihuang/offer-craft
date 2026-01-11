package com.workspace.codeforgeai.ai.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Set;

public class SafeInputGuardrail implements InputGuardrail {
    // set of sensitive words
    private static final Set<String> sensitiveWords = Set.of("kill", "evil");

    /**
     * detect if users' input safe
     */
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        // get user's input and transfer to them lowercase
        String inputText = userMessage.singleText().toLowerCase();
        // divide text to words
        String[] words = inputText.split("\\W+");
        // check all words if exists sensitive words
        for (String word : words) {
            if (sensitiveWords.contains(word)) {
                return fatal("Sensitive word detected: " + word);
            }
        }
        return success();
    }
}
