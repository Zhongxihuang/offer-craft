package com.workspace.codeforgeai.career.workflow;

import com.workspace.codeforgeai.career.api.CareerWorkflowRequest;
import com.workspace.codeforgeai.career.api.CareerWorkflowResponse;

import java.util.Collections;
import java.util.List;

public record WorkflowPersistenceRecord(
        CareerWorkflowResponse response,
        String rootWorkflowId,
        String parentWorkflowId,
        Integer workflowVersion,
        String versionReason,
        Integer memoryId,
        String targetRole,
        String targetLevel,
        String companyName,
        boolean includeCompanyResearch,
        List<String> focusAreas,
        WorkflowDocumentInput jobDescription,
        WorkflowDocumentInput candidateProfile
) {

    public WorkflowPersistenceRecord {
        focusAreas = focusAreas == null ? List.of() : List.copyOf(focusAreas);
        jobDescription = jobDescription == null ? WorkflowDocumentInput.unknown() : jobDescription;
        candidateProfile = candidateProfile == null ? WorkflowDocumentInput.unknown() : candidateProfile;
    }

    public static WorkflowPersistenceRecord from(CareerWorkflowResponse response,
                                                 CareerWorkflowRequest request,
                                                 WorkflowDocumentInput jobDescription,
                                                 WorkflowDocumentInput candidateProfile) {
        return new WorkflowPersistenceRecord(
                response,
                response.versionInfo() == null ? response.workflowId() : response.versionInfo().rootWorkflowId(),
                response.versionInfo() == null ? null : response.versionInfo().parentWorkflowId(),
                response.versionInfo() == null ? 1 : response.versionInfo().versionNumber(),
                response.versionInfo() == null ? null : response.versionInfo().versionReason(),
                request.memoryId(),
                request.targetRole(),
                request.targetLevel(),
                request.companyName(),
                request.includeCompanyResearch(),
                request.normalizedFocusAreas(),
                jobDescription,
                candidateProfile
        );
    }

    public static WorkflowPersistenceRecord fromResponseOnly(CareerWorkflowResponse response) {
        return new WorkflowPersistenceRecord(
                response,
                response.versionInfo() == null ? response.workflowId() : response.versionInfo().rootWorkflowId(),
                response.versionInfo() == null ? null : response.versionInfo().parentWorkflowId(),
                response.versionInfo() == null ? 1 : response.versionInfo().versionNumber(),
                response.versionInfo() == null ? null : response.versionInfo().versionReason(),
                null,
                null,
                null,
                null,
                false,
                Collections.emptyList(),
                WorkflowDocumentInput.unknown(),
                WorkflowDocumentInput.unknown()
        );
    }
}
