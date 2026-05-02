package com.workspace.codeforgeai.career.jd;

import com.workspace.codeforgeai.career.workflow.StageConfidence;

import java.util.List;

public record JdAnalysisResult(
        String roleTitle,
        String seniority,
        String summary,
        List<String> mustHaveRequirements,
        List<String> niceToHaveRequirements,
        List<String> keywords,
        List<String> interviewFocusAreas,
        StageConfidence confidence
) {
}
