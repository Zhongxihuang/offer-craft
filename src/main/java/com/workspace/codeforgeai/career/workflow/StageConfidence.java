package com.workspace.codeforgeai.career.workflow;

import java.util.List;

public record StageConfidence(
        String evidenceStrength,
        List<String> strongestEvidence,
        List<String> missingEvidence,
        List<String> inferenceNotes,
        Integer observedSignalCount,
        Integer inferredSignalCount,
        Integer missingSignalCount,
        Boolean fallbackUsed
) {
}
