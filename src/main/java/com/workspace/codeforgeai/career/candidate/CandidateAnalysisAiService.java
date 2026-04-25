package com.workspace.codeforgeai.career.candidate;

import dev.langchain4j.service.SystemMessage;

public interface CandidateAnalysisAiService {

    @SystemMessage(fromResource = "prompts/candidate-analysis-system-prompt.txt")
    CandidateAnalysisResult analyze(String workflowInput);
}
