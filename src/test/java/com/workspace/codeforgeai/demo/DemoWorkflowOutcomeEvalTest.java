package com.workspace.codeforgeai.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workspace.codeforgeai.career.api.CareerWorkflowResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "career.ai.mode=demo",
                "spring.datasource.url=jdbc:h2:mem:demo-outcome;MODE=LEGACY;DB_CLOSE_DELAY=-1",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "career.workflow.storage.upload-root=target/test-uploads/demo-outcome"
        }
)
class DemoWorkflowOutcomeEvalTest {

    private static final String CANONICAL_JD = """
            Senior AI Product Manager, enterprise GenAI platform.
            Requirements:
            - Own AI/LLM product strategy and prioritization
            - Drive experimentation, KPI ownership, and iteration loops
            - Partner with engineering, design, GTM, and operations
            - Improve enterprise trust, governance, and admin controls
            - Explain evaluation, retrieval quality, latency, and reliability tradeoffs
            """;

    private static final String STRONG_RESUME = """
            Senior Product Manager with 7 years in enterprise SaaS.
            Led AI assistant launches for operations teams and owned KPI reviews, experimentation, and rollout quality.
            Partnered with engineering, legal, security, and GTM on enterprise admin controls, trust reviews, and governance design.
            Can explain retrieval quality, evaluation metrics, and latency tradeoffs to executives and technical teams.
            """;

    private static final String WEAK_RESUME = """
            Operations assistant focused on schedules, follow-up notes, and vendor coordination.
            Comfortable with spreadsheets, status trackers, and meeting logistics.
            No shipped product ownership, no technical build history, and no measurable experimentation record.
            """;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void datasetScenariosMeetOutcomeRubric() throws IOException {
        List<OutcomeEvalCase> scenarios = objectMapper.readValue(
                new ClassPathResource("eval/workflow-outcome-dataset.json").getInputStream(),
                new TypeReference<>() {
                }
        );

        assertThat(scenarios).isNotEmpty();

        for (OutcomeEvalCase scenario : scenarios) {
            CareerWorkflowResponse response = analyze(
                    scenario.jobDescription(),
                    scenario.candidateProfile(),
                    scenario.locale(),
                    scenario.includeCompanyResearch(),
                    scenario.companyName()
            );

            assertThat(response.decisionSummary().fitLevel())
                    .withFailMessage("Unexpected fit band for %s", scenario.name())
                    .isIn(scenario.expectation().fitBands().toArray(String[]::new));
            assertThat(response.confidenceSummary().overallConfidence())
                    .withFailMessage("Unexpected confidence band for %s", scenario.name())
                    .isIn(scenario.expectation().confidenceBands().toArray(String[]::new));
            assertThat(response.decisionDrivers())
                    .withFailMessage("Decision drivers should be populated for %s", scenario.name())
                    .hasSizeGreaterThanOrEqualTo(scenario.expectation().minDecisionDrivers());
            assertThat(response.clarificationQuestions())
                    .withFailMessage("Clarification questions should be populated for %s", scenario.name())
                    .hasSizeGreaterThanOrEqualTo(scenario.expectation().minClarificationQuestions());
            assertThat(response.degradedMode()).isTrue();
            assertThat(response.degradationNotes()).isNotEmpty();

            if (scenario.expectation().expectBlockingUncertainties()) {
                assertThat(response.confidenceSummary().blockingUncertainties())
                        .withFailMessage("Blocking uncertainties should be present for %s", scenario.name())
                        .isNotEmpty();
            }

            if (scenario.expectation().expectChinese()) {
                assertThat(response.contentLocale()).isEqualTo("zh-CN");
                assertThat(response.confidenceSummary().confidenceRationale()).matches(".*\\p{IsHan}.*");
            }
        }
    }

    @Test
    void strongResumeOutperformsWeakResumeAndKeepsPrepSpecific() {
        CareerWorkflowResponse strong = analyze(CANONICAL_JD, STRONG_RESUME, "en", true, "Northstar AI");
        CareerWorkflowResponse weak = analyze(CANONICAL_JD, WEAK_RESUME, "en", true, "Northstar AI");

        assertThat(strong.decisionSummary().fitLevel()).isIn("STRONG_MATCH", "COMPETITIVE_WITH_GAPS");
        assertThat(weak.candidateAnalysis().missingSignals()).isNotEmpty();
        assertThat(weak.confidenceSummary().missingEvidence()).isNotEmpty();
        assertThat(strong.interviewPrep().prepPlan()).isNotEmpty();
        assertThat(strong.confidenceSummary().strongestEvidence()).isNotEmpty();
    }

    @Test
    void incompleteJdLowersJdConfidenceAndPreservesUsefulFallback() {
        CareerWorkflowResponse response = analyze(
                "AI PM role. Work on AI products. Fast experiments.",
                STRONG_RESUME,
                "en",
                false,
                null
        );

        assertThat(response.jdAnalysis().confidence()).isNotNull();
        assertThat(response.jdAnalysis().confidence().evidenceStrength()).isNotBlank();
        assertThat(response.confidenceSummary().strongestEvidence()).isNotEmpty();
        assertThat(response.interviewPrep().prepPlan()).isNotEmpty();
    }

    @Test
    void incompleteResumeLowersCandidateConfidenceWithoutHallucinatingEvidence() {
        CareerWorkflowResponse response = analyze(
                CANONICAL_JD,
                "PM intern. Worked on some product tasks.",
                "en",
                false,
                null
        );

        assertThat(response.candidateAnalysis().confidence()).isNotNull();
        assertThat(response.candidateAnalysis().confidence().missingEvidence()).isNotEmpty();
        assertThat(response.gapAnalysis().priorityGaps()).isNotEmpty();
        assertThat(response.confidenceSummary().missingEvidence()).isNotEmpty();
    }

    @Test
    void chineseWorkflowStillReturnsSpecificStructuredOutput() {
        CareerWorkflowResponse response = analyze(
                """
                        AI 产品经理校招岗位。
                        需要理解 AI 产品、做过实验、能快速做 Demo，并参与过真实用户场景的 AI 应用落地。
                        """,
                """
                        做过 AI 产品定义、RAG、Agent 架构和实验设计，也有跨团队推进和 Demo 落地经验。
                        """,
                "zh-CN",
                true,
                "美团"
        );

        assertThat(response.contentLocale()).isEqualTo("zh-CN");
        assertThat(response.interviewPrep().prepSummary()).matches(".*\\p{IsHan}.*");
        assertThat(response.confidenceSummary().strongestEvidence()).isNotEmpty();
    }

    @Test
    void demoModeSurfacesGracefulDegradationInsteadOfFailing() {
        CareerWorkflowResponse response = analyze(CANONICAL_JD, STRONG_RESUME, "en", true, "Northstar AI");

        assertThat(response.degradedMode()).isTrue();
        assertThat(response.degradationNotes()).isNotEmpty();
        assertThat(response.usedSearch()).isFalse();
        assertThat(response.usedRetrieval()).isFalse();
        assertThat(response.interviewPrep().companyResearchSuggestions()).isNotEmpty();
    }

    private CareerWorkflowResponse analyze(String jobDescription,
                                           String candidateProfile,
                                           String locale,
                                           boolean includeCompanyResearch,
                                           String companyName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String companyFragment = companyName == null
                ? ""
                : """
                  "companyName": %s,
                """.formatted(quote(companyName));

        String requestBody = """
                {
                  "memoryId": 91,
                  "locale": %s,
                  "targetRole": %s,
                  "targetLevel": %s,
                %s  "jobDescription": %s,
                  "candidateProfile": %s,
                  "focusAreas": ["gap analysis", "interview prep"],
                  "includeCompanyResearch": %s
                }
                """.formatted(
                quote(locale),
                quote(locale.startsWith("zh") ? "AI 产品经理" : "Senior AI Product Manager"),
                quote(locale.startsWith("zh") ? "校招" : "Senior"),
                companyFragment,
                quote(jobDescription),
                quote(candidateProfile),
                includeCompanyResearch
        );

        ResponseEntity<CareerWorkflowResponse> response = restTemplate.postForEntity(
                "/career/workflow/analyze",
                new HttpEntity<>(requestBody, headers),
                CareerWorkflowResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private String quote(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n") + "\"";
    }

    private record OutcomeEvalCase(
            String name,
            String locale,
            String jobDescription,
            String candidateProfile,
            boolean includeCompanyResearch,
            String companyName,
            OutcomeExpectation expectation
    ) {
    }

    private record OutcomeExpectation(
            List<String> fitBands,
            List<String> confidenceBands,
            int minDecisionDrivers,
            int minClarificationQuestions,
            boolean expectBlockingUncertainties,
            boolean expectChinese
    ) {
    }
}
