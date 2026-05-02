package com.workspace.codeforgeai.career.api;

public record CareerWorkflowComparisonItem(
        String workflowId,
        String targetRole,
        String companyName,
        String fitLevel,
        String applyVerdict,
        String confidence,
        String topGap,
        String prepCost,
        int priorityScore,
        String recommendation
) {
}
