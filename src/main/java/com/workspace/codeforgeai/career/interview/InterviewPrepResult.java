package com.workspace.codeforgeai.career.interview;

import com.workspace.codeforgeai.career.workflow.StageConfidence;

import java.util.List;

public record InterviewPrepResult(
        String prepSummary,
        List<String> technicalFocusAreas,
        List<String> behavioralQuestionPrompts,
        List<String> resumeDefensePoints,
        List<String> prepPlan,
        List<String> companyResearchSuggestions,
        StageConfidence confidence
) {
}
