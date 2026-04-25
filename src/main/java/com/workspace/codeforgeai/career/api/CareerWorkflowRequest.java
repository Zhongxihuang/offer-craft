package com.workspace.codeforgeai.career.api;

import jakarta.validation.constraints.NotBlank;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record CareerWorkflowRequest(
        Integer memoryId,
        String workflowId,
        String targetRole,
        String targetLevel,
        String companyName,
        @NotBlank(message = "{career.workflow.jobDescription.required}")
        String jobDescription,
        @NotBlank(message = "{career.workflow.candidateProfile.required}")
        String candidateProfile,
        List<String> focusAreas,
        boolean includeCompanyResearch,
        String locale
) {

    public List<String> normalizedFocusAreas() {
        if (focusAreas == null) {
            return Collections.emptyList();
        }

        return focusAreas.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
