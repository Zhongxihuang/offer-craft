package com.workspace.codeforgeai.career.workflow;

import com.workspace.codeforgeai.career.api.CareerWorkflowResponse;

import java.util.List;

public record StoredWorkflowContext(
        CareerWorkflowResponse response,
        Integer memoryId,
        String targetRole,
        String targetLevel,
        String companyName,
        boolean includeCompanyResearch,
        List<String> focusAreas,
        String jobDescriptionText,
        String candidateProfileText,
        String contentLocale,
        WorkflowVersionInfo versionInfo
) {
}
