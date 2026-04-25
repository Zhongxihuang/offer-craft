package com.workspace.codeforgeai.career.workflow;

import com.workspace.codeforgeai.career.api.CareerWorkflowRequest;
import com.workspace.codeforgeai.career.api.CareerWorkflowResponse;
import com.workspace.codeforgeai.career.candidate.CandidateAnalysisAiService;
import com.workspace.codeforgeai.career.candidate.CandidateAnalysisResult;
import com.workspace.codeforgeai.career.gap.GapAnalysisAiService;
import com.workspace.codeforgeai.career.gap.GapAnalysisResult;
import com.workspace.codeforgeai.career.gap.GapItem;
import com.workspace.codeforgeai.career.interview.InterviewPrepAiService;
import com.workspace.codeforgeai.career.interview.InterviewPrepResult;
import com.workspace.codeforgeai.career.jd.JdAnalysisResult;
import com.workspace.codeforgeai.career.jd.JdParsingAiService;
import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareerWorkflowOrchestratorTest {

    @Mock
    private JdParsingAiService jdParsingAiService;

    @Mock
    private CandidateAnalysisAiService candidateAnalysisAiService;

    @Mock
    private GapAnalysisAiService gapAnalysisAiService;

    @Mock
    private InterviewPrepAiService interviewPrepAiService;

    @Mock
    private LocalizedMessages localizedMessages;

    @Mock
    private CareerWorkflowConfidenceBuilder careerWorkflowConfidenceBuilder;

    private CareerWorkflowOrchestrator careerWorkflowOrchestrator;

    @BeforeEach
    void setUp() {
        when(localizedMessages.get(org.mockito.ArgumentMatchers.any(com.workspace.codeforgeai.common.i18n.SupportedLocale.class), org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        careerWorkflowOrchestrator = new CareerWorkflowOrchestrator(
                jdParsingAiService,
                candidateAnalysisAiService,
                gapAnalysisAiService,
                interviewPrepAiService,
                localizedMessages,
                careerWorkflowConfidenceBuilder
        );

        when(careerWorkflowConfidenceBuilder.enrichJdAnalysis(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(careerWorkflowConfidenceBuilder.enrichCandidateAnalysis(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(careerWorkflowConfidenceBuilder.enrichGapAnalysis(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    GapAnalysisResult gapAnalysis = invocation.getArgument(1);
                    List<GapItem> enrichedGaps = gapAnalysis.priorityGaps() == null
                            ? List.of()
                            : gapAnalysis.priorityGaps().stream()
                            .map(gap -> new GapItem(
                                    gap.requirement(),
                                    gap.candidateEvidence(),
                                    gap.gapLevel(),
                                    "HIGH".equalsIgnoreCase(gap.gapLevel()) ? "HIGH" : "MEDIUM",
                                    "HIGH".equalsIgnoreCase(gap.gapLevel()) ? "HIGH" : "MEDIUM",
                                    "LOW",
                                    gap.recommendation(),
                                    gap.rankingReason()
                            ))
                            .toList();
                    return new GapAnalysisResult(
                            gapAnalysis.overallAssessment(),
                            gapAnalysis.matchNarrative(),
                            gapAnalysis.matchingStrengths(),
                            enrichedGaps,
                            gapAnalysis.positioningAdvice(),
                            gapAnalysis.confidence()
                    );
                });
        when(careerWorkflowConfidenceBuilder.enrichInterviewPrep(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(careerWorkflowConfidenceBuilder.buildConfidenceSummary(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new ConfidenceSummary("MEDIUM", List.of("Explicit must-haves"), List.of("Cloud proof"), List.of("Some narrative is inferred"), List.of("AWS production depth"), List.of("AWS production depth"), "The fit call is useful but cloud proof is still limited."));
        when(careerWorkflowConfidenceBuilder.buildDecisionDrivers(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(new EvidenceItem("Explicit must-haves anchor the screen.", EvidenceSourceType.JD_TEXT)));
        when(careerWorkflowConfidenceBuilder.buildClarificationQuestions(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of("What cloud story would most strengthen the case?"));
    }

    @Test
    void runBuildsWorkflowArtifacts() {
        when(jdParsingAiService.analyze(anyString())).thenReturn(
                new JdAnalysisResult(
                        "Senior Backend Engineer",
                        "Senior",
                        "Java backend role focused on APIs, distributed systems, and cloud delivery.",
                        List.of("Java", "Spring Boot", "REST APIs"),
                        List.of("AWS"),
                        List.of("Java", "Spring Boot", "Distributed Systems"),
                        List.of("API design", "scalability", "behavioral ownership"),
                        null
                )
        );

        when(candidateAnalysisAiService.analyze(anyString())).thenReturn(
                new CandidateAnalysisResult(
                        "Backend engineer with strong Java delivery experience",
                        "Candidate has credible backend experience and delivery ownership.",
                        List.of("Java services", "Spring Boot", "API ownership"),
                        List.of("Built internal APIs", "Maintained production services"),
                        List.of("No strong cloud signal", "Limited distributed systems depth"),
                        List.of("Backend API roles", "Platform-adjacent roles"),
                        null
                )
        );

        when(gapAnalysisAiService.analyze(anyString())).thenReturn(
                new GapAnalysisResult(
                        "COMPETITIVE_WITH_GAPS",
                        "The candidate matches the core backend stack but needs stronger cloud and scale stories.",
                        List.of("Strong Java and Spring alignment", "Good ownership signal"),
                        List.of(
                                new GapItem("AWS production depth", "Mentions services but not cloud ownership", "HIGH",
                                        null,
                                        null,
                                        null,
                                        "Prepare two concrete examples that show deployment, observability, and incident handling in cloud environments.",
                                        null),
                                new GapItem("Distributed systems scale", "Some API experience but little scale evidence", "MEDIUM",
                                        null,
                                        null,
                                        null,
                                        "Build and rehearse a STAR story around performance, throughput, or reliability tradeoffs.",
                                        null)
                        ),
                        List.of("Position as a backend operator who can ramp quickly on cloud depth."),
                        null
                )
        );

        when(interviewPrepAiService.analyze(anyString())).thenReturn(
                new InterviewPrepResult(
                        "Focus prep on cloud credibility, scale tradeoffs, and concrete ownership stories.",
                        List.of("Spring Boot internals", "AWS deployment basics", "Caching and queues"),
                        List.of("Tell me about a production incident you handled.", "Describe a system you improved for reliability."),
                        List.of("Explain why your API work maps to this role.", "Defend the scope of your ownership clearly."),
                        List.of("Tailor the resume summary to the JD keywords.", "Rehearse two cloud-adjacent project stories."),
                        List.of("Review the company's engineering blog or architecture interviews.", "Map the JD to the company's public product surface."),
                        null
                )
        );

        CareerWorkflowRequest request = new CareerWorkflowRequest(
                1,
                null,
                "Senior Backend Engineer",
                "Senior",
                "Acme",
                "We need a senior backend engineer with Java, Spring Boot, REST APIs, and AWS exposure.",
                "Backend engineer with Java and Spring Boot experience building APIs.",
                List.of("system design", "behavioral"),
                true,
                "en"
        );

        CareerWorkflowResponse response = careerWorkflowOrchestrator.run(request);

        assertNotNull(response.workflowId());
        assertEquals("en", response.contentLocale());
        assertEquals(1, response.versionInfo().versionNumber());
        assertEquals("MEDIUM", response.confidenceSummary().overallConfidence());
        assertEquals("COMPETITIVE_WITH_GAPS", response.decisionSummary().fitLevel());
        assertEquals("PREP_FIRST", response.decisionSummary().applyVerdict());
        assertEquals("Senior Backend Engineer", response.decisionSummary().recommendedPositioning());
        org.junit.jupiter.api.Assertions.assertFalse(response.actionPlan().isEmpty());
        assertEquals("HIGH", response.gapAnalysis().priorityGaps().getFirst().hiringImpact());
        org.junit.jupiter.api.Assertions.assertFalse(response.nextSteps().isEmpty());

        verify(interviewPrepAiService).analyze(contains("Company name: Acme"));
    }
}
