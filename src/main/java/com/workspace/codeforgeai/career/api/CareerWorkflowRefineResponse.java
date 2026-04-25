package com.workspace.codeforgeai.career.api;

import com.workspace.codeforgeai.career.workflow.WorkflowVersionSummary;

import java.util.List;

public record CareerWorkflowRefineResponse(
        String actionTaken,
        String assistantMessage,
        CareerWorkflowResponse workflow,
        List<WorkflowVersionSummary> versions
) {
}
