package com.workspace.codeforgeai.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workspace.codeforgeai.ai.CodeForgeAiService;
import com.workspace.codeforgeai.career.api.CareerWorkflowRefineResponse;
import com.workspace.codeforgeai.career.api.CareerWorkflowResponse;
import com.workspace.codeforgeai.career.workflow.WorkflowVersionSummary;
import com.workspace.codeforgeai.career.workflow.WorkflowSessionStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "career.ai.mode=demo",
                "spring.datasource.url=jdbc:h2:mem:demo-it;MODE=LEGACY;DB_CLOSE_DELAY=-1",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "career.workflow.storage.upload-root=target/test-uploads/demo-it"
        }
)
class DemoModeIntegrationTest {

    private static final String SAMPLE_JD = """
            Senior AI Product Manager, GenAI Platform
            Build enterprise AI workflow capabilities for a B2B copilot product.
            Requirements:
            - Own AI/LLM product strategy and prioritization
            - Partner cross-functionally with engineering, design, and GTM
            - Drive metrics, experimentation, and rollout quality
            - Improve enterprise trust, governance, and admin controls
            - Communicate technical tradeoffs across evaluation, retrieval, and reliability
            """;

    private static final String SAMPLE_RESUME = """
            Senior Product Manager with 7 years in B2B SaaS.
            Led experimentation and KPI reviews for workflow automation products.
            Partnered with engineering, design, sales, and customer success on launches.
            Worked on an AI assistant pilot and defined customer use cases and success metrics.
            """;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WorkflowSessionStore workflowSessionStore;

    @Autowired
    private CodeForgeAiService codeForgeAiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void applicationStartsInDemoModeWithoutExternalKeys() {
        assertThat(codeForgeAiService).isNotNull();
    }

    @Test
    void jsonAnalyzeRestoresPersistedWorkflowAndSupportsWorkflowLinkedChat() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = """
                {
                  "memoryId": 11,
                  "locale": "en",
                  "targetRole": "Senior AI Product Manager",
                  "targetLevel": "Senior",
                  "companyName": "Northstar AI",
                  "jobDescription": %s,
                  "candidateProfile": %s,
                  "focusAreas": ["AI strategy", "governance"],
                  "includeCompanyResearch": true
                }
                """.formatted(
                objectMapper.writeValueAsString(SAMPLE_JD),
                objectMapper.writeValueAsString(SAMPLE_RESUME)
        );

        ResponseEntity<CareerWorkflowResponse> analyzeResponse = restTemplate.postForEntity(
                "/career/workflow/analyze",
                new HttpEntity<>(requestBody, headers),
                CareerWorkflowResponse.class
        );

        assertThat(analyzeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CareerWorkflowResponse response = analyzeResponse.getBody();
        assertThat(response).isNotNull();
        assertThat(response.workflowId()).isNotBlank();
        assertThat(response.contentLocale()).isEqualTo("en");
        assertThat(response.decisionSummary()).isNotNull();
        assertThat(response.jdAnalysis()).isNotNull();
        assertThat(response.candidateAnalysis()).isNotNull();
        assertThat(response.gapAnalysis()).isNotNull();
        assertThat(response.interviewPrep()).isNotNull();

        clearWorkflowCache();

        ResponseEntity<CareerWorkflowResponse> restored = restTemplate.getForEntity(
                "/career/workflow/" + response.workflowId(),
                CareerWorkflowResponse.class
        );
        assertThat(restored.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(restored.getBody()).isNotNull();
        assertThat(restored.getBody().workflowId()).isEqualTo(response.workflowId());

        String followUpUri = UriComponentsBuilder.fromPath("/ai/chat")
                .queryParam("memoryId", 77)
                .queryParam("workflowId", response.workflowId())
                .queryParam("message", "Turn my top gaps into a 7-day prep plan")
                .queryParam("locale", "en")
                .build()
                .toUriString();

        ResponseEntity<String> chatResponse = restTemplate.getForEntity(followUpUri, String.class);
        assertThat(chatResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(chatResponse.getBody()).contains("data:");
        assertThat(chatResponse.getBody()).contains("7-day");
    }

    @Test
    void uploadAnalyzePersistsResultAndRestoresFromStorage() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("memoryId", "12");
        formData.add("locale", "en");
        formData.add("targetRole", "Senior AI Product Manager");
        formData.add("targetLevel", "Senior");
        formData.add("companyName", "Northstar AI");
        formData.add("focusAreas", "AI strategy, governance");
        formData.add("includeCompanyResearch", "true");
        formData.add("jobDescriptionFile", textFile("job-description.txt", SAMPLE_JD));
        formData.add("candidateProfileText", SAMPLE_RESUME);

        ResponseEntity<CareerWorkflowResponse> uploadResponse = restTemplate.postForEntity(
                "/career/workflow/analyze-upload",
                new HttpEntity<>(formData, headers),
                CareerWorkflowResponse.class
        );

        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CareerWorkflowResponse response = uploadResponse.getBody();
        assertThat(response).isNotNull();
        assertThat(response.workflowId()).isNotBlank();
        assertThat(response.contentLocale()).isEqualTo("en");

        clearWorkflowCache();

        ResponseEntity<CareerWorkflowResponse> restored = restTemplate.getForEntity(
                "/career/workflow/" + response.workflowId(),
                CareerWorkflowResponse.class
        );
        assertThat(restored.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(restored.getBody()).isNotNull();
        assertThat(restored.getBody().workflowId()).isEqualTo(response.workflowId());

        Path storedUpload = Path.of("target/test-uploads/demo-it", response.workflowId(), "job-description.txt");
        assertThat(Files.exists(storedUpload)).isTrue();
    }

    @Test
    void jsonAnalyzeAndWorkflowLinkedChatSupportChineseLocale() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAcceptLanguageAsLocales(java.util.List.of(java.util.Locale.forLanguageTag("zh-CN")));

        String requestBody = """
                {
                  "memoryId": 31,
                  "locale": "zh-CN",
                  "targetRole": "AI 产品经理",
                  "targetLevel": "校招",
                  "companyName": "美团",
                  "jobDescription": %s,
                  "candidateProfile": %s,
                  "focusAreas": ["面试策略", "简历表达"],
                  "includeCompanyResearch": true
                }
                """.formatted(
                objectMapper.writeValueAsString("""
                        AI产品经理提前批，要求同时具备 AI 理解、动手能力、AI 编程能力，以及参与过有实际用户的 AI 产品设计或开发。
                        需要强学习能力、逻辑思考能力，并能快速做实验。
                        优先考虑有 AI Agent、MCP Tools、Skill 等应用落地经验的候选人。
                        """),
                objectMapper.writeValueAsString("""
                        产品经理方向候选人，具备 AI 产品定义、RAG、Agent 架构、Prompt Engineering 经验。
                        做过 AI 编程学习与求职辅导助手，也参与过真实 AI 模块设计与实验。
                        有数据分析、A/B 测试、跨团队协作和 Demo 落地经验。
                        """)
        );

        ResponseEntity<CareerWorkflowResponse> analyzeResponse = restTemplate.postForEntity(
                "/career/workflow/analyze",
                new HttpEntity<>(requestBody, headers),
                CareerWorkflowResponse.class
        );

        assertThat(analyzeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CareerWorkflowResponse response = analyzeResponse.getBody();
        assertThat(response).isNotNull();
        assertThat(response.contentLocale()).isEqualTo("zh-CN");
        assertThat(response.decisionSummary().summary()).matches(".*\\p{IsHan}.*");
        assertThat(response.interviewPrep().prepSummary()).matches(".*\\p{IsHan}.*");

        String followUpUri = UriComponentsBuilder.fromPath("/ai/chat")
                .queryParam("memoryId", 88)
                .queryParam("workflowId", response.workflowId())
                .queryParam("message", "把我最重要的差距转成一个 7 天准备计划")
                .queryParam("locale", "zh-CN")
                .build()
                .toUriString();

        ResponseEntity<String> chatResponse = restTemplate.getForEntity(followUpUri, String.class);
        assertThat(chatResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(chatResponse.getBody()).contains("data:");
        assertThat(chatResponse.getBody()).matches("(?s).*\\p{IsHan}.*");
    }

    @Test
    void validationErrorsRespectChineseAcceptLanguage() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAcceptLanguageAsLocales(java.util.List.of(java.util.Locale.forLanguageTag("zh-CN")));

        ResponseEntity<String> response = restTemplate.exchange(
                "/career/workflow/analyze",
                HttpMethod.POST,
                new HttpEntity<>("""
                        {
                          "jobDescription": "需要 AI 产品经理"
                        }
                        """, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("请求校验失败");
        assertThat(response.getBody()).contains("candidateProfile");
    }

    @Test
    void refineCreatesNewWorkflowVersionAndReturnsVersionHistory() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = """
                {
                  "memoryId": 41,
                  "locale": "en",
                  "targetRole": "Senior AI Product Manager",
                  "jobDescription": %s,
                  "candidateProfile": %s,
                  "focusAreas": ["gap analysis", "prep plan"],
                  "includeCompanyResearch": true
                }
                """.formatted(
                objectMapper.writeValueAsString(SAMPLE_JD),
                objectMapper.writeValueAsString(SAMPLE_RESUME)
        );

        ResponseEntity<CareerWorkflowResponse> analyzeResponse = restTemplate.postForEntity(
                "/career/workflow/analyze",
                new HttpEntity<>(requestBody, headers),
                CareerWorkflowResponse.class
        );

        assertThat(analyzeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CareerWorkflowResponse initialWorkflow = analyzeResponse.getBody();
        assertThat(initialWorkflow).isNotNull();
        assertThat(initialWorkflow.versionInfo().versionNumber()).isEqualTo(1);
        assertThat(initialWorkflow.confidenceSummary()).isNotNull();

        String refineBody = """
                {
                  "memoryId": 41,
                  "locale": "en",
                  "action": "APPEND_CANDIDATE_EVIDENCE",
                  "message": "Additional evidence: led a governance review for an enterprise AI rollout and partnered with legal, security, and admin platform teams."
                }
                """;

        ResponseEntity<CareerWorkflowRefineResponse> refineResponse = restTemplate.postForEntity(
                "/career/workflow/" + initialWorkflow.workflowId() + "/refine",
                new HttpEntity<>(refineBody, headers),
                CareerWorkflowRefineResponse.class
        );

        assertThat(refineResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CareerWorkflowRefineResponse refineBodyResponse = refineResponse.getBody();
        assertThat(refineBodyResponse).isNotNull();
        assertThat(refineBodyResponse.workflow()).isNotNull();
        assertThat(refineBodyResponse.workflow().versionInfo().versionNumber()).isEqualTo(2);
        assertThat(refineBodyResponse.workflow().versionInfo().rootWorkflowId()).isEqualTo(initialWorkflow.workflowId());
        assertThat(refineBodyResponse.versions()).hasSizeGreaterThanOrEqualTo(2);

        ResponseEntity<WorkflowVersionSummary[]> versionsResponse = restTemplate.getForEntity(
                "/career/workflow/" + refineBodyResponse.workflow().workflowId() + "/versions",
                WorkflowVersionSummary[].class
        );

        assertThat(versionsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(versionsResponse.getBody()).isNotNull();
        assertThat(versionsResponse.getBody()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void memoryIsolationWorksAcrossSessions() {
        String rememberUri = UriComponentsBuilder.fromPath("/ai/chat")
                .queryParam("memoryId", 201)
                .queryParam("message", "Remember that my focus gap is governance")
                .queryParam("locale", "en")
                .build()
                .toUriString();
        restTemplate.getForEntity(rememberUri, String.class);

        String sameSessionUri = UriComponentsBuilder.fromPath("/ai/chat")
                .queryParam("memoryId", 201)
                .queryParam("message", "What focus gap did I mention?")
                .queryParam("locale", "en")
                .build()
                .toUriString();
        ResponseEntity<String> sameSessionResponse = restTemplate.getForEntity(sameSessionUri, String.class);
        assertThat(sameSessionResponse.getBody()).contains("governance");

        String differentSessionUri = UriComponentsBuilder.fromPath("/ai/chat")
                .queryParam("memoryId", 202)
                .queryParam("message", "What focus gap did I mention?")
                .queryParam("locale", "en")
                .build()
                .toUriString();
        ResponseEntity<String> differentSessionResponse = restTemplate.getForEntity(differentSessionUri, String.class);
        assertThat(differentSessionResponse.getBody()).contains("not shared a focus gap");
    }

    @SuppressWarnings("unchecked")
    private void clearWorkflowCache() {
        Map<String, CareerWorkflowResponse> cache =
                (Map<String, CareerWorkflowResponse>) ReflectionTestUtils.getField(workflowSessionStore, "responseCache");
        assertThat(cache).isNotNull();
        cache.clear();
    }

    private ByteArrayResource textFile(String filename, String contents) {
        return new ByteArrayResource(contents.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
