package com.workspace.codeforgeai.career.workflow;

import java.time.Instant;

public record WorkflowVersionSummary(
        String workflowId,
        String rootWorkflowId,
        String parentWorkflowId,
        int versionNumber,
        Instant generatedAt,
        String fitLevel,
        String contentLocale,
        String versionReason
) {
}
