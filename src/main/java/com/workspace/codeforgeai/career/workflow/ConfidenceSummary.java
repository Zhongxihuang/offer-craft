package com.workspace.codeforgeai.career.workflow;

import java.util.List;

public record ConfidenceSummary(
        String overallConfidence,
        List<String> strongestEvidence,
        List<String> missingEvidence,
        List<String> inferenceNotes,
        List<String> mostInfluentialGaps,
        List<String> blockingUncertainties,
        String confidenceRationale
) {
}
