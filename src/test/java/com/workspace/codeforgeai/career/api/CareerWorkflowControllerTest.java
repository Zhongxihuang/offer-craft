package com.workspace.codeforgeai.career.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workspace.codeforgeai.career.candidate.CandidateAnalysisResult;
import com.workspace.codeforgeai.career.gap.GapAnalysisResult;
import com.workspace.codeforgeai.career.gap.GapItem;
import com.workspace.codeforgeai.career.interview.InterviewPrepResult;
import com.workspace.codeforgeai.career.jd.JdAnalysisResult;
import com.workspace.codeforgeai.career.workflow.ConfidenceSummary;
import com.workspace.codeforgeai.career.workflow.CareerWorkflowApplicationService;
import com.workspace.codeforgeai.career.workflow.DecisionSummary;
import com.workspace.codeforgeai.career.workflow.EvidenceItem;
import com.workspace.codeforgeai.career.workflow.EvidenceSourceType;
import com.workspace.codeforgeai.career.workflow.ActionPlanStep;
import com.workspace.codeforgeai.career.workflow.StageConfidence;
import com.workspace.codeforgeai.career.workflow.WorkflowSessionStore;
import com.workspace.codeforgeai.career.workflow.WorkflowAccessGuard;
import com.workspace.codeforgeai.career.workflow.WorkflowVersionInfo;
import com.workspace.codeforgeai.career.workflow.WorkflowVersionSummary;
import com.workspace.codeforgeai.common.api.ApiErrorDetail;
import com.workspace.codeforgeai.common.api.ApiValidationException;
import com.workspace.codeforgeai.common.api.GlobalExceptionHandler;
import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CareerWorkflowController.class)
@Import(GlobalExceptionHandler.class)
class CareerWorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CareerWorkflowApplicationService careerWorkflowApplicationService;

    @MockBean
    private WorkflowSessionStore workflowSessionStore;

    @MockBean
    private WorkflowAccessGuard workflowAccessGuard;

    @MockBean
    private LocalizedMessages localizedMessages;

    @org.junit.jupiter.api.BeforeEach
    void setUpMessages() {
        when(localizedMessages.get(anyString())).thenAnswer(invocation -> switch ((String) invocation.getArgument(0)) {
            case "errors.request.validation" -> "Request validation failed.";
            case "errors.request.body.invalid" -> "Request body is invalid or malformed JSON.";
            case "errors.request.unexpected" -> "An unexpected error occurred.";
            case "errors.request.invalidValue" -> "Invalid value.";
            case "errors.request.forbidden" -> "A valid workflow access token is required in this environment.";
            case "errors.workflow.notFound" -> "Workflow not found.";
            default -> (String) invocation.getArgument(0);
        });
    }

    @Test
    void analyzeReturnsWorkflowArtifactsForValidRequest() throws Exception {
        when(careerWorkflowApplicationService.analyze(any(CareerWorkflowRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/career/workflow/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowId").value("workflow-123"))
                .andExpect(jsonPath("$.decisionSummary.fitLevel").value("COMPETITIVE_WITH_GAPS"));
    }

    @Test
    void analyzeReturnsBadRequestWhenJobDescriptionIsMissing() throws Exception {
        String requestBody = """
                {
                  "candidateProfile": "Backend engineer with Spring Boot experience.",
                  "targetRole": "Backend Engineer"
                }
                """;

        mockMvc.perform(post("/career/workflow/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.details[*].field", hasItem("jobDescription")));
    }

    @Test
    void analyzeReturnsBadRequestWhenCandidateProfileIsMissing() throws Exception {
        String requestBody = """
                {
                  "jobDescription": "Need a Java backend engineer with Spring Boot.",
                  "targetRole": "Backend Engineer"
                }
                """;

        mockMvc.perform(post("/career/workflow/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.details[*].field", hasItem("candidateProfile")));
    }

    @Test
    void analyzeReturnsBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/career/workflow/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobDescription\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request body is invalid or malformed JSON."));
    }

    @Test
    void getWorkflowReturnsNotFoundWithUnifiedErrorEnvelope() throws Exception {
        when(workflowSessionStore.find("missing-workflow")).thenReturn(Optional.empty());

        mockMvc.perform(get("/career/workflow/missing-workflow"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Workflow not found."));
    }

    @Test
    void getWorkflowReturnsForbiddenWhenAccessGuardRejectsRequest() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN,
                "A valid workflow access token is required in this environment."
        )).when(workflowAccessGuard).verifyReadAccess(null);

        mockMvc.perform(get("/career/workflow/workflow-123"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("A valid workflow access token is required in this environment."));
    }

    @Test
    void refineReturnsForbiddenWhenAccessGuardRejectsRequest() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN,
                "A valid workflow access token is required in this environment."
        )).when(workflowAccessGuard).verifyReadAccess(null);

        mockMvc.perform(post("/career/workflow/workflow-123/refine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "locale": "en",
                                  "action": "APPEND_CANDIDATE_EVIDENCE",
                                  "message": "Add AI governance evidence."
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("A valid workflow access token is required in this environment."));
    }

    @Test
    void analyzeUploadReturnsWorkflowArtifactsForValidMultipartRequest() throws Exception {
        when(careerWorkflowApplicationService.analyzeUpload(any(CareerWorkflowUploadRequest.class))).thenReturn(sampleResponse());

        MockMultipartFile jobDescriptionFile = new MockMultipartFile(
                "jobDescriptionFile",
                "jd.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Senior AI PM role".getBytes()
        );

        mockMvc.perform(multipart("/career/workflow/analyze-upload")
                        .file(jobDescriptionFile)
                        .param("candidateProfileText", "Candidate has PM and AI workflow experience.")
                        .param("targetRole", "Senior AI Product Manager")
                        .param("focusAreas", "resume framing, interview prep"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowId").value("workflow-123"))
                .andExpect(jsonPath("$.decisionSummary.fitLevel").value("COMPETITIVE_WITH_GAPS"));
    }

    @Test
    void compareReturnsRankedRoleResults() throws Exception {
        when(careerWorkflowApplicationService.compare(any(CareerWorkflowComparisonRequest.class)))
                .thenReturn(new CareerWorkflowComparisonResponse(
                        Instant.parse("2026-04-11T12:10:00Z"),
                        "en",
                        "workflow-123",
                        "Prioritize Backend Engineer first.",
                        List.of(new CareerWorkflowComparisonItem(
                                "workflow-123",
                                "Backend Engineer",
                                "Acme",
                                "COMPETITIVE_WITH_GAPS",
                                "APPLY_WITH_REFRAMING",
                                "MEDIUM",
                                "Distributed systems scale",
                                "Medium: needs one positioning pass",
                                62,
                                "Apply with tailored positioning."
                        ))
                ));

        String requestBody = """
                {
                  "candidateProfile": "Candidate has Java and Spring Boot experience shipping APIs.",
                  "targets": [
                    {
                      "targetRole": "Backend Engineer",
                      "companyName": "Acme",
                      "jobDescription": "Need a Java backend engineer with Spring Boot."
                    },
                    {
                      "targetRole": "Platform Engineer",
                      "companyName": "Beta",
                      "jobDescription": "Need platform ownership and distributed systems depth."
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/career/workflow/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedWorkflowId").value("workflow-123"))
                .andExpect(jsonPath("$.items[0].priorityScore").value(62));
    }

    @Test
    void compareReturnsBadRequestWhenTooFewTargetsAreProvided() throws Exception {
        String requestBody = """
                {
                  "candidateProfile": "Candidate has Java and Spring Boot experience shipping APIs.",
                  "targets": [
                    {
                      "targetRole": "Backend Engineer",
                      "jobDescription": "Need a Java backend engineer with Spring Boot."
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/career/workflow/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[*].field", hasItem("targets")));
    }

    @Test
    void analyzeUploadReturnsBadRequestWhenDocumentSourcesAreMissing() throws Exception {
        when(careerWorkflowApplicationService.analyzeUpload(any(CareerWorkflowUploadRequest.class)))
                .thenThrow(new ApiValidationException(
                        "Upload request validation failed.",
                        List.of(new ApiErrorDetail("jobDescription", "Provide pasted text or upload a .pdf, .txt, or .md file."))
                ));

        mockMvc.perform(multipart("/career/workflow/analyze-upload")
                        .param("candidateProfileText", "Candidate has PM and AI workflow experience."))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Upload request validation failed."))
                .andExpect(jsonPath("$.details[*].field", hasItem("jobDescription")));
    }

    @Test
    void analyzeUploadReturnsBadRequestForUnsupportedFileType() throws Exception {
        when(careerWorkflowApplicationService.analyzeUpload(any(CareerWorkflowUploadRequest.class)))
                .thenThrow(new ApiValidationException(
                        "Upload request validation failed.",
                        List.of(new ApiErrorDetail("candidateProfileFile", "Upload a .pdf, .txt, or .md file."))
                ));

        MockMultipartFile candidateProfileFile = new MockMultipartFile(
                "candidateProfileFile",
                "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "binary".getBytes()
        );

        mockMvc.perform(multipart("/career/workflow/analyze-upload")
                        .file(candidateProfileFile)
                        .param("jobDescriptionText", "Need an AI PM with enterprise product experience."))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Upload request validation failed."))
                .andExpect(jsonPath("$.details[*].field", hasItem("candidateProfileFile")));
    }

    @Test
    void refineReturnsUpdatedWorkflowAndVersions() throws Exception {
        when(careerWorkflowApplicationService.refine(anyString(), any(CareerWorkflowRefineRequest.class)))
                .thenReturn(new CareerWorkflowRefineResponse(
                        "APPEND_CANDIDATE_EVIDENCE",
                        "Created v2 with your added evidence.",
                        sampleResponseV2(),
                        List.of(
                                new WorkflowVersionSummary("workflow-123", "workflow-123", null, 1, Instant.parse("2026-04-11T12:00:00Z"), "COMPETITIVE_WITH_GAPS", "en", "Initial analysis"),
                                new WorkflowVersionSummary("workflow-456", "workflow-123", "workflow-123", 2, Instant.parse("2026-04-11T12:05:00Z"), "STRONG_MATCH", "en", "Added candidate evidence")
                        )
                ));

        mockMvc.perform(post("/career/workflow/workflow-123/refine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "locale": "en",
                                  "action": "APPEND_CANDIDATE_EVIDENCE",
                                  "message": "I led a high-scale launch with SRE and platform teams."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionTaken").value("APPEND_CANDIDATE_EVIDENCE"))
                .andExpect(jsonPath("$.workflow.versionInfo.versionNumber").value(2))
                .andExpect(jsonPath("$.versions[1].versionNumber").value(2));
    }

    @Test
    void versionsEndpointReturnsSavedHistory() throws Exception {
        when(workflowSessionStore.find("workflow-123")).thenReturn(Optional.of(sampleResponse()));
        when(careerWorkflowApplicationService.listVersions("workflow-123")).thenReturn(List.of(
                new WorkflowVersionSummary("workflow-123", "workflow-123", null, 1, Instant.parse("2026-04-11T12:00:00Z"), "COMPETITIVE_WITH_GAPS", "en", "Initial analysis"),
                new WorkflowVersionSummary("workflow-456", "workflow-123", "workflow-123", 2, Instant.parse("2026-04-11T12:05:00Z"), "STRONG_MATCH", "en", "Added candidate evidence")
        ));

        mockMvc.perform(get("/career/workflow/workflow-123/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].versionNumber").value(1))
                .andExpect(jsonPath("$[1].workflowId").value("workflow-456"));
    }

    private CareerWorkflowRequest validRequest() {
        return new CareerWorkflowRequest(
                1,
                null,
                "Backend Engineer",
                "Mid",
                "Acme",
                "Need a Java backend engineer with Spring Boot and API design experience.",
                "Candidate has Java and Spring Boot experience shipping APIs.",
                List.of("system design", "behavioral"),
                true,
                "en"
        );
    }

    private CareerWorkflowResponse sampleResponse() {
        return new CareerWorkflowResponse(
                "workflow-123",
                Instant.parse("2026-04-11T12:00:00Z"),
                "en",
                new WorkflowVersionInfo("workflow-123", null, 1, "Initial analysis"),
                new DecisionSummary(
                        "COMPETITIVE_WITH_GAPS",
                        "APPLY_WITH_REFRAMING",
                        "The core backend stack is solid, but stronger scale stories would improve interview confidence.",
                        "Backend Engineer",
                        "The candidate is credible for the role but needs stronger distributed-systems stories.",
                        "Apply with tailored positioning and rehearse scale stories.",
                        List.of("Scale story", "Cloud credibility")
                ),
                new ConfidenceSummary(
                        "MEDIUM",
                        List.of("Explicit role requirements"),
                        List.of("Limited scale evidence"),
                        List.of("Some conclusions rely on inferred scope"),
                        List.of("Distributed systems scale"),
                        List.of("Distributed systems scale"),
                        "The fit call is directionally useful, but stronger scale proof could still change the decision."
                ),
                List.of(new EvidenceItem("The JD explicitly asks for Java and Spring Boot.", EvidenceSourceType.JD_TEXT)),
                List.of("What scale or reliability story best proves your impact?"),
                new JdAnalysisResult(
                        "Backend Engineer",
                        "Mid",
                        "API-focused backend role.",
                        List.of("Java", "Spring Boot"),
                        List.of("AWS"),
                        List.of("Java", "APIs"),
                        List.of("API design"),
                        stageConfidence("HIGH")
                ),
                new CandidateAnalysisResult(
                        "Backend engineer with Java delivery experience",
                        "Candidate has relevant backend experience.",
                        List.of("Java", "Spring Boot"),
                        List.of("Built APIs"),
                        List.of("Limited scale signal"),
                        List.of("Backend platform roles"),
                        stageConfidence("MEDIUM")
                ),
                new GapAnalysisResult(
                        "COMPETITIVE_WITH_GAPS",
                        "The candidate aligns on stack but needs stronger scale proof.",
                        List.of("Strong Java alignment"),
                        List.of(new GapItem(
                                "Distributed systems scale",
                                "Built APIs but not clearly high scale",
                                "MEDIUM",
                                "HIGH",
                                "HIGH",
                                "MEDIUM",
                                "Prepare a performance or reliability story.",
                                "This gap is one of the few blockers that can change hiring confidence."
                        )),
                        List.of("Position as a backend engineer with strong execution fundamentals."),
                        stageConfidence("MEDIUM")
                ),
                new InterviewPrepResult(
                        "Focus on API design and scale tradeoffs.",
                        List.of("API design"),
                        List.of("Tell me about an architecture tradeoff you made."),
                        List.of("Explain ownership clearly."),
                        List.of("Rehearse two technical stories."),
                        List.of("Review the company engineering blog."),
                        stageConfidence("HIGH")
                ),
                false,
                true,
                false,
                List.of(),
                List.of(new ActionPlanStep(
                        "Day 1-2",
                        "Reframe the application story",
                        List.of("Tailor the resume.", "Add one scale-facing example."),
                        "The resume and intro pitch now lead with role-relevant evidence."
                )),
                List.of("Tailor the resume.", "Rehearse scale stories."),
                List.of("Support chat", "RAG")
        );
    }

    private CareerWorkflowResponse sampleResponseV2() {
        return new CareerWorkflowResponse(
                "workflow-456",
                Instant.parse("2026-04-11T12:05:00Z"),
                "en",
                new WorkflowVersionInfo("workflow-123", "workflow-123", 2, "Added candidate evidence"),
                new DecisionSummary(
                        "STRONG_MATCH",
                        "APPLY_NOW",
                        "The new platform evidence removes the main hiring risk.",
                        "Backend Engineer",
                        "The added platform story significantly improves match confidence.",
                        "Apply now and emphasize the new scale story.",
                        List.of("Use scale launch story")
                ),
                new ConfidenceSummary(
                        "HIGH",
                        List.of("Strong backend evidence", "Added scale launch story"),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        "The core hiring risks are now mostly covered by direct evidence."
                ),
                List.of(new EvidenceItem("The added scale launch story materially improves confidence.", EvidenceSourceType.MODEL_INFERENCE)),
                List.of(),
                new JdAnalysisResult("Backend Engineer", "Mid", "API-focused backend role.", List.of("Java", "Spring Boot"), List.of("AWS"), List.of("Java", "APIs"), List.of("API design"), stageConfidence("HIGH")),
                new CandidateAnalysisResult("Backend engineer with stronger scale evidence", "Candidate now shows stronger scale proof.", List.of("Java", "Spring Boot", "Scale launch"), List.of("Built APIs", "Led high-scale launch"), List.of(), List.of("Backend platform roles"), stageConfidence("HIGH")),
                new GapAnalysisResult("STRONG_MATCH", "The main scale risk is now addressed.", List.of("Strong Java alignment", "Scale story added"), List.of(), List.of("Lead with the scale launch story."), stageConfidence("HIGH")),
                new InterviewPrepResult("Focus on emphasizing scale and platform tradeoffs.", List.of("API design"), List.of("Describe the scale launch."), List.of("Anchor scope with metrics."), List.of("Rehearse the scale story."), List.of("Review the company engineering blog."), stageConfidence("HIGH")),
                false,
                true,
                false,
                List.of(),
                List.of(new ActionPlanStep(
                        "Day 1",
                        "Apply now with the updated story",
                        List.of("Lead with the scale launch story."),
                        "The application narrative clearly explains why you fit now."
                )),
                List.of("Lead with the scale launch story."),
                List.of("Support chat", "RAG")
        );
    }

    private StageConfidence stageConfidence(String level) {
        return new StageConfidence(
                level,
                List.of("Strongest evidence"),
                List.of("Missing evidence"),
                List.of("Inference note"),
                3,
                1,
                1,
                false
        );
    }
}
