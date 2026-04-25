package com.workspace.codeforgeai.career.api;

import com.workspace.codeforgeai.career.workflow.CareerWorkflowApplicationService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/career/workflow")
public class CareerWorkflowController {

    private final CareerWorkflowApplicationService careerWorkflowApplicationService;
    private final WorkflowSessionStore workflowSessionStore;
    private final LocalizedMessages localizedMessages;

    public CareerWorkflowController(CareerWorkflowApplicationService careerWorkflowApplicationService,
                                    WorkflowSessionStore workflowSessionStore,
                                    LocalizedMessages localizedMessages) {
        this.careerWorkflowApplicationService = careerWorkflowApplicationService;
        this.workflowSessionStore = workflowSessionStore;
        this.localizedMessages = localizedMessages;
    }

    @PostMapping("/analyze")
    public CareerWorkflowResponse analyze(@Valid @RequestBody CareerWorkflowRequest request) {
        return careerWorkflowApplicationService.analyze(request);
    }

    @PostMapping(value = "/analyze-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CareerWorkflowResponse analyzeUpload(@ModelAttribute CareerWorkflowUploadRequest request) {
        return careerWorkflowApplicationService.analyzeUpload(request);
    }

    @PostMapping("/{workflowId}/refine")
    public CareerWorkflowRefineResponse refineWorkflow(@PathVariable String workflowId,
                                                       @Valid @RequestBody(required = false) CareerWorkflowRefineRequest request) {
        return careerWorkflowApplicationService.refine(workflowId, request);
    }

    @GetMapping("/{workflowId}")
    public CareerWorkflowResponse getWorkflow(@PathVariable String workflowId) {
        return workflowSessionStore.find(workflowId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, localizedMessages.get("errors.workflow.notFound")));
    }

    @GetMapping("/{workflowId}/versions")
    public java.util.List<com.workspace.codeforgeai.career.workflow.WorkflowVersionSummary> getWorkflowVersions(@PathVariable String workflowId) {
        if (workflowSessionStore.find(workflowId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, localizedMessages.get("errors.workflow.notFound"));
        }

        return careerWorkflowApplicationService.listVersions(workflowId);
    }
}
