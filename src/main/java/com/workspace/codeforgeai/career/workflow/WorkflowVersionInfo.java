package com.workspace.codeforgeai.career.workflow;

public record WorkflowVersionInfo(
        String rootWorkflowId,
        String parentWorkflowId,
        int versionNumber,
        String versionReason
) {
}
