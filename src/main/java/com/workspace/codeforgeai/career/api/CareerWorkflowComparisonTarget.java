package com.workspace.codeforgeai.career.api;

import jakarta.validation.constraints.NotBlank;

public record CareerWorkflowComparisonTarget(
        @NotBlank(message = "{career.workflow.targetRole.required}")
        String targetRole,
        String targetLevel,
        String companyName,
        @NotBlank(message = "{career.workflow.jobDescription.required}")
        String jobDescription
) {
}
