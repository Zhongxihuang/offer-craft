package com.workspace.codeforgeai.career.support;

import com.workspace.codeforgeai.ai.CodeForgeAiService;
import com.workspace.codeforgeai.career.api.CareerWorkflowResponse;
import com.workspace.codeforgeai.career.candidate.CandidateAnalysisResult;
import com.workspace.codeforgeai.career.gap.GapAnalysisResult;
import com.workspace.codeforgeai.career.gap.GapItem;
import com.workspace.codeforgeai.career.interview.InterviewPrepResult;
import com.workspace.codeforgeai.career.jd.JdAnalysisResult;
import com.workspace.codeforgeai.career.workflow.ConfidenceSummary;
import com.workspace.codeforgeai.career.workflow.DecisionSummary;
import com.workspace.codeforgeai.career.workflow.EvidenceItem;
import com.workspace.codeforgeai.career.workflow.EvidenceSourceType;
import com.workspace.codeforgeai.career.workflow.ActionPlanStep;
import com.workspace.codeforgeai.career.workflow.StageConfidence;
import com.workspace.codeforgeai.career.workflow.WorkflowSessionStore;
import com.workspace.codeforgeai.career.workflow.WorkflowVersionInfo;
import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareerSupportChatServiceTest {

    @Mock
    private CodeForgeAiService codeForgeAiService;

    @Mock
    private WorkflowSessionStore workflowSessionStore;

    @Mock
    private LocalizedMessages localizedMessages;

    private CareerSupportChatService careerSupportChatService;

    @BeforeEach
    void setUp() {
        careerSupportChatService = new CareerSupportChatService(codeForgeAiService, workflowSessionStore, localizedMessages);
    }

    @Test
    void chatDelegatesRawMessageWhenWorkflowIdIsMissing() {
        when(codeForgeAiService.chatWithStream(eq(7), anyString()))
                .thenReturn(Flux.just("ok"));

        List<String> chunks = careerSupportChatService
                .chat(7, "Explain the riskiest interview area.", null, "en")
                .collectList()
                .block();

        assertEquals(List.of("ok"), chunks);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(codeForgeAiService).chatWithStream(eq(7), messageCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("Requested response locale: en"));
        assertTrue(messageCaptor.getValue().contains("Explain the riskiest interview area."));
    }

    @Test
    void chatBuildsWorkflowScopedPromptWhenWorkflowIdExists() {
        when(workflowSessionStore.find("workflow-123")).thenReturn(Optional.of(sampleResponse()));
        when(codeForgeAiService.chatWithStream(eq(7), anyString())).thenReturn(Flux.just("ok"));

        careerSupportChatService
                .chat(7, "How should I explain the distributed systems gap?", "workflow-123", "en")
                .blockLast();

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(codeForgeAiService).chatWithStream(eq(7), messageCaptor.capture());

        String contextualizedMessage = messageCaptor.getValue();
        assertTrue(contextualizedMessage.contains("Support chat mode: workflow-linked follow-up"));
        assertTrue(contextualizedMessage.contains("Requested response locale: en"));
        assertTrue(contextualizedMessage.contains("Linked workflow ID: workflow-123"));
        assertTrue(contextualizedMessage.contains("Version: v1"));
        assertTrue(contextualizedMessage.contains("Overall confidence: MEDIUM"));
        assertTrue(contextualizedMessage.contains("Fit level: COMPETITIVE_WITH_GAPS"));
        assertTrue(contextualizedMessage.contains("Distributed systems scale"));
        assertTrue(contextualizedMessage.contains("How should I explain the distributed systems gap?"));
    }

    @Test
    void chatReturnsFriendlyFallbackWhenWorkflowContextIsMissing() {
        when(workflowSessionStore.find("missing-workflow")).thenReturn(Optional.empty());
        when(localizedMessages.get(org.mockito.ArgumentMatchers.any(com.workspace.codeforgeai.common.i18n.SupportedLocale.class), org.mockito.ArgumentMatchers.eq("support.chat.missingWorkflow")))
                .thenReturn("Please rerun the career workflow analysis");

        List<String> chunks = careerSupportChatService
                .chat(7, "Help me frame the resume.", "missing-workflow", "en")
                .collectList()
                .block();

        assertEquals(1, chunks.size());
        assertTrue(chunks.getFirst().contains("Please rerun the career workflow analysis"));
        verifyNoInteractions(codeForgeAiService);
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
                        "The backend fundamentals are strong, but the scale story still needs sharper framing.",
                        "Senior Backend Engineer",
                        "The candidate is credible for the role but needs stronger scale stories.",
                        "Apply with tailored positioning and rehearse high-scale ownership stories.",
                        List.of("Scale narrative", "Cloud credibility")
                ),
                new ConfidenceSummary(
                        "MEDIUM",
                        List.of("Strong Java alignment"),
                        List.of("Limited cloud proof"),
                        List.of("Some fit areas are inferred"),
                        List.of("Distributed systems scale"),
                        List.of("Distributed systems scale"),
                        "The fit call is useful, but stronger cloud and scale proof would tighten it."
                ),
                List.of(new EvidenceItem("The candidate already shows Java API delivery evidence.", EvidenceSourceType.CANDIDATE_TEXT)),
                List.of("What is your strongest scale story?"),
                new JdAnalysisResult(
                        "Senior Backend Engineer",
                        "Senior",
                        "Distributed systems role focused on APIs and reliability.",
                        List.of("Java", "Spring Boot", "Distributed systems"),
                        List.of("AWS"),
                        List.of("Java", "Distributed systems"),
                        List.of("System design", "Reliability"),
                        stageConfidence("HIGH")
                ),
                new CandidateAnalysisResult(
                        "Backend engineer with Java API delivery experience",
                        "Strong backend fundamentals with weaker scale proof.",
                        List.of("Java delivery", "API ownership"),
                        List.of("Built internal APIs", "Owned production fixes"),
                        List.of("No clear cloud depth", "Limited scale examples"),
                        List.of("Backend platform roles"),
                        stageConfidence("MEDIUM")
                ),
                new GapAnalysisResult(
                        "COMPETITIVE_WITH_GAPS",
                        "The candidate aligns on stack but needs stronger distributed-systems stories.",
                        List.of("Strong Java alignment"),
                        List.of(new GapItem(
                                "Distributed systems scale",
                                "Built APIs but not clearly at large scale",
                                "MEDIUM",
                                "HIGH",
                                "HIGH",
                                "MEDIUM",
                                "Prepare a performance or reliability story with concrete metrics.",
                                "This gap can materially change hiring confidence."
                        )),
                        List.of("Position as a strong backend executor who can ramp on scale complexity."),
                        stageConfidence("MEDIUM")
                ),
                new InterviewPrepResult(
                        "Focus on reliability, tradeoffs, and concrete ownership.",
                        List.of("Reliability tradeoffs", "API design"),
                        List.of("Describe a reliability issue you resolved."),
                        List.of("Explain scope and ownership clearly."),
                        List.of("Rehearse two scale stories."),
                        List.of("Review the company engineering blog."),
                        stageConfidence("HIGH")
                ),
                false,
                true,
                false,
                List.of(),
                List.of(new ActionPlanStep(
                        "Day 1-2",
                        "Reframe the scale story",
                        List.of("Update the intro pitch.", "Anchor one reliability story with metrics."),
                        "The resume and interview intro now explain scale ownership clearly."
                )),
                List.of("Tailor the resume.", "Rehearse scale stories."),
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
