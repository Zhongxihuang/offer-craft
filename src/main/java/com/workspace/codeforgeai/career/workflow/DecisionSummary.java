package com.workspace.codeforgeai.career.workflow;

import java.util.List;

public record DecisionSummary(
        String fitLevel,
        String applyVerdict,
        String applyVerdictReason,
        String recommendedPositioning,
        String summary,
        String recommendedNextMove,
        List<String> topPriorities
) {
}
