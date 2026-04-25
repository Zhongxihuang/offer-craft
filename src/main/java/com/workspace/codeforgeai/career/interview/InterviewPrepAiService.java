package com.workspace.codeforgeai.career.interview;

import dev.langchain4j.service.SystemMessage;

public interface InterviewPrepAiService {

    @SystemMessage(fromResource = "prompts/interview-prep-system-prompt.txt")
    InterviewPrepResult analyze(String workflowInput);
}
