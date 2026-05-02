package com.workspace.codeforgeai.career.api;

import com.workspace.codeforgeai.career.workflow.CareerWorkflowApplicationService;
import com.workspace.codeforgeai.career.workflow.WorkflowAccessGuard;
import com.workspace.codeforgeai.career.workflow.WorkflowSessionStore;
import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/career/workflow")
public class CareerWorkflowController {

    private final CareerWorkflowApplicationService careerWorkflowApplicationService;
    private final WorkflowSessionStore workflowSessionStore;
    private final WorkflowAccessGuard workflowAccessGuard;
    private final LocalizedMessages localizedMessages;

    public CareerWorkflowController(CareerWorkflowApplicationService careerWorkflowApplicationService,
                                    WorkflowSessionStore workflowSessionStore,
                                    WorkflowAccessGuard workflowAccessGuard,
                                    LocalizedMessages localizedMessages) {
        this.careerWorkflowApplicationService = careerWorkflowApplicationService;
        this.workflowSessionStore = workflowSessionStore;
        this.workflowAccessGuard = workflowAccessGuard;
        this.localizedMessages = localizedMessages;
    }

    @PostMapping("/analyze")
    public CareerWorkflowResponse analyze(@Valid @RequestBody CareerWorkflowRequest request) {
        return careerWorkflowApplicationService.analyze(request);
    }

    @PostMapping("/compare")
    public CareerWorkflowComparisonResponse compare(@Valid @RequestBody CareerWorkflowComparisonRequest request) {
        return careerWorkflowApplicationService.compare(request);
    }

    @PostMapping(value = "/analyze-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CareerWorkflowResponse analyzeUpload(@Valid @ModelAttribute CareerWorkflowUploadRequest request) {
        return careerWorkflowApplicationService.analyzeUpload(request);
    }

    @PostMapping("/{workflowId}/refine")
    public CareerWorkflowRefineResponse refineWorkflow(@PathVariable String workflowId,
                                                       @RequestHeader(value = "X-Workflow-Access-Token", required = false) String accessToken,
                                                       @Valid @RequestBody(required = false) CareerWorkflowRefineRequest request) {
        workflowAccessGuard.verifyReadAccess(accessToken);
        return careerWorkflowApplicationService.refine(workflowId, request);
    }

    @GetMapping("/{workflowId}")
    public CareerWorkflowResponse getWorkflow(@PathVariable String workflowId,
                                              @RequestHeader(value = "X-Workflow-Access-Token", required = false) String accessToken) {
        workflowAccessGuard.verifyReadAccess(accessToken);
        return workflowSessionStore.find(workflowId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, localizedMessages.get("errors.workflow.notFound")));
    }

    @GetMapping("/{workflowId}/versions")
    public java.util.List<com.workspace.codeforgeai.career.workflow.WorkflowVersionSummary> getWorkflowVersions(
            @PathVariable String workflowId,
            @RequestHeader(value = "X-Workflow-Access-Token", required = false) String accessToken) {
        workflowAccessGuard.verifyReadAccess(accessToken);
        if (workflowSessionStore.find(workflowId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, localizedMessages.get("errors.workflow.notFound"));
        }

        return careerWorkflowApplicationService.listVersions(workflowId);
    }
}
