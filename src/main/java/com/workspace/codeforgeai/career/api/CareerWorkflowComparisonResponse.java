package com.workspace.codeforgeai.career.api;

import java.time.Instant;
import java.util.List;

public record CareerWorkflowComparisonResponse(
        Instant generatedAt,
        String contentLocale,
        String recommendedWorkflowId,
        String summary,
        List<CareerWorkflowComparisonItem> items
) {
}
