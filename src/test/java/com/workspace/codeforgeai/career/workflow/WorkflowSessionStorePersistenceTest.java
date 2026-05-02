package com.workspace.codeforgeai.career.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.workspace.codeforgeai.career.api.CareerWorkflowResponse;
import com.workspace.codeforgeai.career.candidate.CandidateAnalysisResult;
import com.workspace.codeforgeai.career.gap.GapAnalysisResult;
import com.workspace.codeforgeai.career.gap.GapItem;
import com.workspace.codeforgeai.career.interview.InterviewPrepResult;
import com.workspace.codeforgeai.career.jd.JdAnalysisResult;
import com.workspace.codeforgeai.career.workflow.ConfidenceSummary;
import com.workspace.codeforgeai.career.workflow.ActionPlanStep;
import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class WorkflowSessionStorePersistenceTest {

    @Autowired
    private WorkflowSessionRepository workflowSessionRepository;

    @Test
    void savePersistsWorkflowMetadataAndReloadsFromRepositoryBackedStore() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        LocalizedMessages localizedMessages = org.mockito.Mockito.mock(LocalizedMessages.class);
        org.mockito.Mockito.when(localizedMessages.get(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        WorkflowSessionStore writer = new WorkflowSessionStore(workflowSessionRepository, objectMapper, localizedMessages);
        CareerWorkflowResponse response = sampleResponse();

        writer.save(new WorkflowPersistenceRecord(
                response,
                "workflow-123",
                null,
                1,
                "Initial analysis",
                77,
                "Senior AI Product Manager",
                "Senior",
                "Nimbus AI",
                true,
                List.of("resume framing", "interview prep"),
                WorkflowDocumentInput.file("job-description.pdf", "./data/uploads/workflow-123/job-description.pdf",
                        "Enterprise AI PM role with governance responsibility."),
                WorkflowDocumentInput.text("Candidate profile focused on experimentation and cross-functional delivery.")
        ));

        WorkflowSessionStore reader = new WorkflowSessionStore(workflowSessionRepository, objectMapper, localizedMessages);
        CareerWorkflowResponse reloaded = reader.find("workflow-123").orElseThrow();
        WorkflowSessionEntity entity = workflowSessionRepository.findById("workflow-123").orElseThrow();

        assertEquals("workflow-123", reloaded.workflowId());
        assertEquals("en", reloaded.contentLocale());
        assertEquals("COMPETITIVE_WITH_GAPS", reloaded.decisionSummary().fitLevel());
        assertEquals(1, reloaded.versionInfo().versionNumber());
        assertEquals("workflow-123", reloaded.versionInfo().rootWorkflowId());
        assertEquals("FILE", entity.getJobDescriptionSourceType());
        assertEquals("TEXT", entity.getCandidateProfileSourceType());
        assertEquals("en", entity.getContentLocale());
        assertEquals("workflow-123", entity.getRootWorkflowId());
        assertEquals(1, entity.getWorkflowVersion());
        assertTrue(entity.getWorkflowResponseJson().contains("\"workflowId\":\"workflow-123\""));
        assertTrue(entity.getJobDescriptionText().contains("governance responsibility"));
    }

    @Test
    void findDefaultsLegacyRecordsWithoutContentLocaleToEnglish() {
        WorkflowSessionEntity legacyEntity = new WorkflowSessionEntity();
        legacyEntity.setWorkflowId("legacy-workflow");
        legacyEntity.setWorkflowResponseJson("""
                {
                  "workflowId":"legacy-workflow",
                  "generatedAt":"2026-04-11T12:00:00Z",
                  "usedSearch":false,
                  "usedRetrieval":false,
                  "degradedMode":false,
                  "degradationNotes":[],
                  "decisionSummary":{
                    "fitLevel":"COMPETITIVE_WITH_GAPS",
                    "recommendedPositioning":"AI Product Manager",
                    "summary":"Legacy response without locale metadata.",
                    "recommendedNextMove":"Re-run in the preferred language.",
                    "topPriorities":["Narrative clarity"]
                  },
                  "jdAnalysis":{
                    "roleTitle":"AI Product Manager",
                    "seniority":"Mid",
                    "summary":"Legacy JD analysis.",
                    "mustHaveRequirements":["AI strategy"],
                    "niceToHaveRequirements":["Prompt iteration"],
                    "keywords":["AI"],
                    "interviewFocusAreas":["Prioritization"]
                  },
                  "candidateAnalysis":{
                    "candidateHeadline":"Candidate headline",
                    "summary":"Legacy candidate analysis.",
                    "strengths":["Analytics"],
                    "evidence":["Owned experiments"],
                    "missingSignals":["Governance"],
                    "likelyFitAreas":["AI PM"]
                  },
                  "gapAnalysis":{
                    "overallAssessment":"COMPETITIVE_WITH_GAPS",
                    "matchNarrative":"Legacy gap narrative.",
                    "matchingStrengths":["Execution"],
                    "priorityGaps":[],
                    "positioningAdvice":["Lead with execution"]
                  },
                  "interviewPrep":{
                    "prepSummary":"Legacy prep summary.",
                    "technicalFocusAreas":["Metrics"],
                    "behavioralQuestionPrompts":["Tell me about a launch."],
                    "resumeDefensePoints":["Use quantified impact."],
                    "prepPlan":["Rehearse one story."],
                    "companyResearchSuggestions":["Review product messaging."]
                  },
                  "nextSteps":["Tailor resume."],
                  "supportCapabilities":["Support chat"]
                }
                """);
        workflowSessionRepository.save(legacyEntity);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        LocalizedMessages localizedMessages = org.mockito.Mockito.mock(LocalizedMessages.class);
        org.mockito.Mockito.when(localizedMessages.get(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        WorkflowSessionStore store = new WorkflowSessionStore(workflowSessionRepository, objectMapper, localizedMessages);

        CareerWorkflowResponse reloaded = store.find("legacy-workflow").orElseThrow();

        assertEquals("en", reloaded.contentLocale());
        assertEquals("legacy-workflow", reloaded.workflowId());
        assertEquals(1, reloaded.versionInfo().versionNumber());
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
                        "The PM foundation is strong, but governance proof is still the biggest hiring question.",
                        "Senior AI Product Manager",
                        "Strong PM foundation with specific enterprise AI gaps to close.",
                        "Apply with tailored positioning and a focused prep sprint.",
                        List.of("Governance depth", "Technical storytelling")
                ),
                new ConfidenceSummary(
                        "MEDIUM",
                        List.of("Strong PM fundamentals"),
                        List.of("Governance depth"),
                        List.of("Some technical signals are inferred"),
                        List.of("Enterprise AI governance"),
                        List.of("Enterprise AI governance"),
                        "The fit call is useful, but stronger governance proof could still change the outcome."
                ),
                List.of(new EvidenceItem("Strong PM fundamentals are directly supported by the resume.", EvidenceSourceType.CANDIDATE_TEXT)),
                List.of("What enterprise governance example best proves readiness?"),
                new JdAnalysisResult(
                        "Senior AI Product Manager",
                        "Senior",
                        "Enterprise GenAI PM role.",
                        List.of("AI strategy", "Cross-functional leadership"),
                        List.of("B2B SaaS"),
                        List.of("AI/LLM product strategy"),
                        List.of("Metrics ownership", "Governance"),
                        stageConfidence("HIGH")
                ),
                new CandidateAnalysisResult(
                        "PM with analytics and experimentation depth",
                        "Strong PM fundamentals with partial AI exposure.",
                        List.of("Experimentation", "Cross-functional execution"),
                        List.of("Launched AI-assisted features"),
                        List.of("Limited governance depth"),
                        List.of("AI PM roles"),
                        stageConfidence("MEDIUM")
                ),
                new GapAnalysisResult(
                        "COMPETITIVE_WITH_GAPS",
                        "Good fit on core PM skills, but enterprise AI specifics need stronger proof.",
                        List.of("Strong PM execution"),
                        List.of(new GapItem(
                                "Enterprise AI governance",
                                "Mentions AI features but not governance frameworks",
                                "HIGH",
                                "HIGH",
                                "HIGH",
                                "LOW",
                                "Prepare a concrete governance and risk-management story.",
                                "This is the highest-risk gap for enterprise-readiness confidence."
                        )),
                        List.of("Lead with PM fundamentals and AI-adjacent wins."),
                        stageConfidence("MEDIUM")
                ),
                new InterviewPrepResult(
                        "Focus on governance, metrics, and technical credibility.",
                        List.of("Evaluation metrics", "AI system tradeoffs"),
                        List.of("How would you scope an enterprise AI copilot rollout?"),
                        List.of("Explain how you influenced engineering and GTM."),
                        List.of("Prepare governance and metrics stories."),
                        List.of("Review company AI positioning."),
                        stageConfidence("HIGH")
                ),
                false,
                true,
                false,
                List.of(),
                List.of(new ActionPlanStep(
                        "Day 1-2",
                        "Tighten the enterprise AI story",
                        List.of("Add a governance example.", "Rewrite the opening positioning."),
                        "The application story now addresses enterprise-readiness concerns directly."
                )),
                List.of("Tailor resume.", "Prepare governance stories."),
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
