package com.workspace.codeforgeai.career.api;

import com.workspace.codeforgeai.career.candidate.CandidateAnalysisResult;
import com.workspace.codeforgeai.career.gap.GapAnalysisResult;
import com.workspace.codeforgeai.career.interview.InterviewPrepResult;
import com.workspace.codeforgeai.career.jd.JdAnalysisResult;
import com.workspace.codeforgeai.career.workflow.ConfidenceSummary;
import com.workspace.codeforgeai.career.workflow.DecisionSummary;
import com.workspace.codeforgeai.career.workflow.EvidenceItem;
import com.workspace.codeforgeai.career.workflow.ActionPlanStep;
import com.workspace.codeforgeai.career.workflow.WorkflowVersionInfo;

import java.time.Instant;
import java.util.List;

public record CareerWorkflowResponse(
        String workflowId,
        Instant generatedAt,
        String contentLocale,
        WorkflowVersionInfo versionInfo,
        DecisionSummary decisionSummary,
        ConfidenceSummary confidenceSummary,
        List<EvidenceItem> decisionDrivers,
        List<String> clarificationQuestions,
        JdAnalysisResult jdAnalysis,
        CandidateAnalysisResult candidateAnalysis,
        GapAnalysisResult gapAnalysis,
        InterviewPrepResult interviewPrep,
        boolean usedSearch,
        boolean usedRetrieval,
        boolean degradedMode,
        List<String> degradationNotes,
        List<ActionPlanStep> actionPlan,
        List<String> nextSteps,
        List<String> supportCapabilities
) {
}
