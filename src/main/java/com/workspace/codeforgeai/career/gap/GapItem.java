package com.workspace.codeforgeai.career.gap;

public record GapItem(
        String requirement,
        String candidateEvidence,
        String gapLevel,
        String hiringImpact,
        String interviewRisk,
        String evidenceStrength,
        String recommendation,
        String rankingReason
) {
}
