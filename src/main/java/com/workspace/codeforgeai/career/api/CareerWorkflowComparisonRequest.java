package com.workspace.codeforgeai.career.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CareerWorkflowComparisonRequest(
        Integer memoryId,
        @NotBlank(message = "{career.workflow.candidateProfile.required}")
        String candidateProfile,
        List<String> focusAreas,
        boolean includeCompanyResearch,
        String locale,
        @Valid
        @Size(min = 2, max = 5, message = "{career.workflow.comparison.targets.size}")
        List<CareerWorkflowComparisonTarget> targets
) {
        public List<String> normalizedFocusAreas() {
                if (focusAreas == null) {
                        return List.of();
                }

                return focusAreas.stream()
                        .filter(item -> item != null && !item.isBlank())
                        .map(String::trim)
                        .distinct()
                        .toList();
        }
}
