package com.workspace.codeforgeai.career.api;

public record CareerWorkflowRefineRequest(
        Integer memoryId,
        String locale,
        String action,
        String message
) {
}
