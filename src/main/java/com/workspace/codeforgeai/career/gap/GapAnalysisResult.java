package com.workspace.codeforgeai.career.gap;

import com.workspace.codeforgeai.career.workflow.StageConfidence;

import java.util.List;

public record GapAnalysisResult(
        String overallAssessment,
        String matchNarrative,
        List<String> matchingStrengths,
        List<GapItem> priorityGaps,
        List<String> positioningAdvice,
        StageConfidence confidence
) {
}
