package com.workspace.codeforgeai.career.gap;

import dev.langchain4j.service.SystemMessage;

public interface GapAnalysisAiService {

    @SystemMessage(fromResource = "prompts/gap-analysis-system-prompt.txt")
    GapAnalysisResult analyze(String workflowInput);
}
