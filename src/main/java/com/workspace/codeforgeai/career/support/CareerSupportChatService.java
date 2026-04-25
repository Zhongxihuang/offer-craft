package com.workspace.codeforgeai.career.support;

import com.workspace.codeforgeai.ai.CodeForgeAiService;
import com.workspace.codeforgeai.career.api.CareerWorkflowResponse;
import com.workspace.codeforgeai.career.workflow.WorkflowSessionStore;
import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import com.workspace.codeforgeai.common.i18n.SupportedLocale;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CareerSupportChatService {

    private final CodeForgeAiService codeForgeAiService;
    private final WorkflowSessionStore workflowSessionStore;
    private final LocalizedMessages localizedMessages;

    public CareerSupportChatService(CodeForgeAiService codeForgeAiService,
                                    WorkflowSessionStore workflowSessionStore,
                                    LocalizedMessages localizedMessages) {
        this.codeForgeAiService = codeForgeAiService;
        this.workflowSessionStore = workflowSessionStore;
        this.localizedMessages = localizedMessages;
    }

    public Flux<String> chat(int memoryId, String message, String workflowId, String localeValue) {
        SupportedLocale locale = SupportedLocale.from(localeValue);
        if (isBlank(workflowId)) {
            return codeForgeAiService.chatWithStream(memoryId, buildLocaleScopedMessage(message, locale));
        }

        return workflowSessionStore.find(workflowId)
                .map(workflow -> codeForgeAiService.chatWithStream(memoryId, buildWorkflowScopedMessage(message, workflow, locale)))
                .orElseGet(() -> Flux.just(
                        localizedMessages.get(locale, "support.chat.missingWorkflow")
                ));
    }

    private String buildWorkflowScopedMessage(String userMessage, CareerWorkflowResponse workflow, SupportedLocale locale) {
        return """
                Support chat mode: workflow-linked follow-up
                Requested response locale: %s
                Linked workflow ID: %s
                
                Use the saved workflow output below as the primary context for this conversation.
                Answer the user's follow-up directly, explain the reasoning using the workflow evidence,
                and give concrete next steps instead of repeating the entire analysis unless asked.
                
                Saved workflow summary
                Workflow metadata:
                - Content locale: %s
                - Version: %s
                - Version reason: %s
                - Used search: %s
                - Used retrieval: %s
                - Degraded mode: %s
                - Degradation notes: %s
                
                Decision summary:
                - Fit level: %s
                - Positioning: %s
                - Narrative: %s
                - Recommended next move: %s
                - Top priorities: %s
                - Overall confidence: %s
                - Confidence rationale: %s
                - Decision drivers: %s
                - Clarification questions: %s
                - Strongest evidence: %s
                - Missing evidence: %s
                - Inference notes: %s
                - Most influential gaps: %s
                - Blocking uncertainties: %s
                
                JD analysis:
                - Role title: %s
                - Seniority: %s
                - Summary: %s
                - Must-have requirements: %s
                - Interview focus areas: %s
                - Confidence: %s
                - JD observed/inferred/missing signals: %s/%s/%s
                
                Candidate analysis:
                - Headline: %s
                - Summary: %s
                - Strengths: %s
                - Missing signals: %s
                - Evidence: %s
                - Confidence: %s
                - Candidate observed/inferred/missing signals: %s/%s/%s
                
                Gap analysis:
                - Overall assessment: %s
                - Match narrative: %s
                - Matching strengths: %s
                - Priority gaps: %s
                - Positioning advice: %s
                - Confidence: %s
                - Gap observed/inferred/missing signals: %s/%s/%s
                
                Interview prep:
                - Summary: %s
                - Technical focus areas: %s
                - Behavioral prompts: %s
                - Resume defense points: %s
                - Prep plan: %s
                - Company research suggestions: %s
                - Confidence: %s
                - Interview observed/inferred/missing signals: %s/%s/%s
                
                Next steps:
                - %s
                
                User follow-up:
                %s
                """.formatted(
                locale.languageTag(),
                workflow.workflowId(),
                valueOrDefault(workflow.contentLocale()),
                workflow.versionInfo() == null ? "v1" : "v" + workflow.versionInfo().versionNumber(),
                workflow.versionInfo() == null ? "Not available" : valueOrDefault(workflow.versionInfo().versionReason()),
                Boolean.toString(workflow.usedSearch()),
                Boolean.toString(workflow.usedRetrieval()),
                Boolean.toString(workflow.degradedMode()),
                joinItems(workflow.degradationNotes()),
                valueOrDefault(workflow.decisionSummary() == null ? null : workflow.decisionSummary().fitLevel()),
                valueOrDefault(workflow.decisionSummary() == null ? null : workflow.decisionSummary().recommendedPositioning()),
                valueOrDefault(workflow.decisionSummary() == null ? null : workflow.decisionSummary().summary()),
                valueOrDefault(workflow.decisionSummary() == null ? null : workflow.decisionSummary().recommendedNextMove()),
                joinItems(workflow.decisionSummary() == null ? null : workflow.decisionSummary().topPriorities()),
                valueOrDefault(workflow.confidenceSummary() == null ? null : workflow.confidenceSummary().overallConfidence()),
                valueOrDefault(workflow.confidenceSummary() == null ? null : workflow.confidenceSummary().confidenceRationale()),
                joinEvidenceItems(workflow.decisionDrivers()),
                joinItems(workflow.clarificationQuestions()),
                joinItems(workflow.confidenceSummary() == null ? null : workflow.confidenceSummary().strongestEvidence()),
                joinItems(workflow.confidenceSummary() == null ? null : workflow.confidenceSummary().missingEvidence()),
                joinItems(workflow.confidenceSummary() == null ? null : workflow.confidenceSummary().inferenceNotes()),
                joinItems(workflow.confidenceSummary() == null ? null : workflow.confidenceSummary().mostInfluentialGaps()),
                joinItems(workflow.confidenceSummary() == null ? null : workflow.confidenceSummary().blockingUncertainties()),
                valueOrDefault(workflow.jdAnalysis() == null ? null : workflow.jdAnalysis().roleTitle()),
                valueOrDefault(workflow.jdAnalysis() == null ? null : workflow.jdAnalysis().seniority()),
                valueOrDefault(workflow.jdAnalysis() == null ? null : workflow.jdAnalysis().summary()),
                joinItems(workflow.jdAnalysis() == null ? null : workflow.jdAnalysis().mustHaveRequirements()),
                joinItems(workflow.jdAnalysis() == null ? null : workflow.jdAnalysis().interviewFocusAreas()),
                valueOrDefault(workflow.jdAnalysis() == null || workflow.jdAnalysis().confidence() == null ? null : workflow.jdAnalysis().confidence().evidenceStrength()),
                valueOrDefault(workflow.jdAnalysis() == null || workflow.jdAnalysis().confidence() == null ? null : String.valueOf(workflow.jdAnalysis().confidence().observedSignalCount())),
                valueOrDefault(workflow.jdAnalysis() == null || workflow.jdAnalysis().confidence() == null ? null : String.valueOf(workflow.jdAnalysis().confidence().inferredSignalCount())),
                valueOrDefault(workflow.jdAnalysis() == null || workflow.jdAnalysis().confidence() == null ? null : String.valueOf(workflow.jdAnalysis().confidence().missingSignalCount())),
                valueOrDefault(workflow.candidateAnalysis() == null ? null : workflow.candidateAnalysis().candidateHeadline()),
                valueOrDefault(workflow.candidateAnalysis() == null ? null : workflow.candidateAnalysis().summary()),
                joinItems(workflow.candidateAnalysis() == null ? null : workflow.candidateAnalysis().strengths()),
                joinItems(workflow.candidateAnalysis() == null ? null : workflow.candidateAnalysis().missingSignals()),
                joinItems(workflow.candidateAnalysis() == null ? null : workflow.candidateAnalysis().evidence()),
                valueOrDefault(workflow.candidateAnalysis() == null || workflow.candidateAnalysis().confidence() == null ? null : workflow.candidateAnalysis().confidence().evidenceStrength()),
                valueOrDefault(workflow.candidateAnalysis() == null || workflow.candidateAnalysis().confidence() == null ? null : String.valueOf(workflow.candidateAnalysis().confidence().observedSignalCount())),
                valueOrDefault(workflow.candidateAnalysis() == null || workflow.candidateAnalysis().confidence() == null ? null : String.valueOf(workflow.candidateAnalysis().confidence().inferredSignalCount())),
                valueOrDefault(workflow.candidateAnalysis() == null || workflow.candidateAnalysis().confidence() == null ? null : String.valueOf(workflow.candidateAnalysis().confidence().missingSignalCount())),
                valueOrDefault(workflow.gapAnalysis() == null ? null : workflow.gapAnalysis().overallAssessment()),
                valueOrDefault(workflow.gapAnalysis() == null ? null : workflow.gapAnalysis().matchNarrative()),
                joinItems(workflow.gapAnalysis() == null ? null : workflow.gapAnalysis().matchingStrengths()),
                joinGapItems(workflow.gapAnalysis() == null ? null : workflow.gapAnalysis().priorityGaps()),
                joinItems(workflow.gapAnalysis() == null ? null : workflow.gapAnalysis().positioningAdvice()),
                valueOrDefault(workflow.gapAnalysis() == null || workflow.gapAnalysis().confidence() == null ? null : workflow.gapAnalysis().confidence().evidenceStrength()),
                valueOrDefault(workflow.gapAnalysis() == null || workflow.gapAnalysis().confidence() == null ? null : String.valueOf(workflow.gapAnalysis().confidence().observedSignalCount())),
                valueOrDefault(workflow.gapAnalysis() == null || workflow.gapAnalysis().confidence() == null ? null : String.valueOf(workflow.gapAnalysis().confidence().inferredSignalCount())),
                valueOrDefault(workflow.gapAnalysis() == null || workflow.gapAnalysis().confidence() == null ? null : String.valueOf(workflow.gapAnalysis().confidence().missingSignalCount())),
                valueOrDefault(workflow.interviewPrep() == null ? null : workflow.interviewPrep().prepSummary()),
                joinItems(workflow.interviewPrep() == null ? null : workflow.interviewPrep().technicalFocusAreas()),
                joinItems(workflow.interviewPrep() == null ? null : workflow.interviewPrep().behavioralQuestionPrompts()),
                joinItems(workflow.interviewPrep() == null ? null : workflow.interviewPrep().resumeDefensePoints()),
                joinItems(workflow.interviewPrep() == null ? null : workflow.interviewPrep().prepPlan()),
                joinItems(workflow.interviewPrep() == null ? null : workflow.interviewPrep().companyResearchSuggestions()),
                valueOrDefault(workflow.interviewPrep() == null || workflow.interviewPrep().confidence() == null ? null : workflow.interviewPrep().confidence().evidenceStrength()),
                valueOrDefault(workflow.interviewPrep() == null || workflow.interviewPrep().confidence() == null ? null : String.valueOf(workflow.interviewPrep().confidence().observedSignalCount())),
                valueOrDefault(workflow.interviewPrep() == null || workflow.interviewPrep().confidence() == null ? null : String.valueOf(workflow.interviewPrep().confidence().inferredSignalCount())),
                valueOrDefault(workflow.interviewPrep() == null || workflow.interviewPrep().confidence() == null ? null : String.valueOf(workflow.interviewPrep().confidence().missingSignalCount())),
                joinItems(workflow.nextSteps()),
                userMessage
        );
    }

    private String buildLocaleScopedMessage(String userMessage, SupportedLocale locale) {
        return """
                Support chat mode: locale-aware follow-up
                Requested response locale: %s

                User follow-up:
                %s
                """.formatted(locale.languageTag(), userMessage);
    }

    private String joinItems(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "None";
        }

        return items.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(" | "));
    }

    private String joinGapItems(List<com.workspace.codeforgeai.career.gap.GapItem> gapItems) {
        if (gapItems == null || gapItems.isEmpty()) {
            return "None";
        }

        return gapItems.stream()
                .map(gap -> "%s (gap: %s, evidence: %s, recommendation: %s)".formatted(
                        valueOrDefault(gap.requirement()),
                        valueOrDefault(gap.gapLevel()),
                        valueOrDefault(gap.candidateEvidence()),
                        valueOrDefault(gap.recommendation())
                ))
                .collect(Collectors.joining(" | "));
    }

    private String joinEvidenceItems(List<com.workspace.codeforgeai.career.workflow.EvidenceItem> evidenceItems) {
        if (evidenceItems == null || evidenceItems.isEmpty()) {
            return "None";
        }

        return evidenceItems.stream()
                .map(item -> "%s (%s)".formatted(
                        valueOrDefault(item.statement()),
                        item.sourceType() == null ? "UNKNOWN" : item.sourceType().name()
                ))
                .collect(Collectors.joining(" | "));
    }

    private String valueOrDefault(String value) {
        return isBlank(value) ? "Not available" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
