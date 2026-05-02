package com.workspace.codeforgeai.career.jd;

import dev.langchain4j.service.SystemMessage;

public interface JdParsingAiService {

    @SystemMessage(fromResource = "prompts/jd-parser-system-prompt.txt")
    JdAnalysisResult analyze(String workflowInput);
}
