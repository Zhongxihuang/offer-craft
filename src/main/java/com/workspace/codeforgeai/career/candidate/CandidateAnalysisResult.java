package com.workspace.codeforgeai.career.candidate;

import com.workspace.codeforgeai.career.workflow.StageConfidence;

import java.util.List;

public record CandidateAnalysisResult(
        String candidateHeadline,
        String summary,
        List<String> strengths,
        List<String> evidence,
        List<String> missingSignals,
        List<String> likelyFitAreas,
        StageConfidence confidence
) {
}
