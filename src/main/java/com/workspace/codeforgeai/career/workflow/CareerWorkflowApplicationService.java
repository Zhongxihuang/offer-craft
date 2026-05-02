package com.workspace.codeforgeai.career.workflow;

import com.workspace.codeforgeai.career.api.CareerWorkflowRefineRequest;
import com.workspace.codeforgeai.career.api.CareerWorkflowRefineResponse;
import com.workspace.codeforgeai.career.api.CareerWorkflowComparisonItem;
import com.workspace.codeforgeai.career.api.CareerWorkflowComparisonRequest;
import com.workspace.codeforgeai.career.api.CareerWorkflowComparisonResponse;
import com.workspace.codeforgeai.career.api.CareerWorkflowComparisonTarget;
import com.workspace.codeforgeai.career.api.CareerWorkflowRequest;
import com.workspace.codeforgeai.career.api.CareerWorkflowResponse;
import com.workspace.codeforgeai.career.api.CareerWorkflowUploadRequest;
import com.workspace.codeforgeai.career.gap.GapItem;
import com.workspace.codeforgeai.common.api.ApiErrorDetail;
import com.workspace.codeforgeai.common.api.ApiValidationException;
import com.workspace.codeforgeai.common.i18n.CareerLocaleResolver;
import com.workspace.codeforgeai.common.i18n.LocalizedMessages;
import com.workspace.codeforgeai.common.i18n.SupportedLocale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CareerWorkflowApplicationService {

    private final CareerWorkflowOrchestrator careerWorkflowOrchestrator;
    private final CareerDocumentUploadService careerDocumentUploadService;
    private final WorkflowSessionStore workflowSessionStore;
    private final CareerLocaleResolver careerLocaleResolver;
    private final WorkflowCapabilityStatus workflowCapabilityStatus;
    private final WorkflowRefineRouter workflowRefineRouter;
    private final LocalizedMessages localizedMessages;

    public CareerWorkflowApplicationService(CareerWorkflowOrchestrator careerWorkflowOrchestrator,
                                            CareerDocumentUploadService careerDocumentUploadService,
                                            WorkflowSessionStore workflowSessionStore,
                                            CareerLocaleResolver careerLocaleResolver,
                                            WorkflowCapabilityStatus workflowCapabilityStatus,
                                            WorkflowRefineRouter workflowRefineRouter,
                                            LocalizedMessages localizedMessages) {
        this.careerWorkflowOrchestrator = careerWorkflowOrchestrator;
        this.careerDocumentUploadService = careerDocumentUploadService;
        this.workflowSessionStore = workflowSessionStore;
        this.careerLocaleResolver = careerLocaleResolver;
        this.workflowCapabilityStatus = workflowCapabilityStatus;
        this.workflowRefineRouter = workflowRefineRouter;
        this.localizedMessages = localizedMessages;
    }

    public CareerWorkflowResponse analyze(CareerWorkflowRequest request) {
        String workflowId = resolveWorkflowId(request.workflowId());
        String locale = careerLocaleResolver.resolveLanguageTag(request.locale());
        CareerWorkflowRequest normalizedRequest = new CareerWorkflowRequest(
                request.memoryId(),
                workflowId,
                normalizeOptional(request.targetRole()),
                normalizeOptional(request.targetLevel()),
                normalizeOptional(request.companyName()),
                normalizeRequired(request.jobDescription()),
                normalizeRequired(request.candidateProfile()),
                request.normalizedFocusAreas(),
                request.includeCompanyResearch(),
                locale
        );

        WorkflowVersionInfo versionInfo = new WorkflowVersionInfo(
                workflowId,
                null,
                1,
                localizedMessages.get(SupportedLocale.from(locale), "workflow.version.initial")
        );

        return runAndPersist(
                normalizedRequest,
                WorkflowDocumentInput.text(normalizedRequest.jobDescription()),
                WorkflowDocumentInput.text(normalizedRequest.candidateProfile()),
                versionInfo
        );
    }

    public CareerWorkflowResponse analyzeUpload(CareerWorkflowUploadRequest request) {
        String workflowId = resolveWorkflowId(request.getWorkflowId());
        String locale = careerLocaleResolver.resolveLanguageTag(request.getLocale());
        WorkflowDocumentInput jobDescription = careerDocumentUploadService.resolveDocument(
                workflowId,
                "jobDescription",
                request.getJobDescriptionFile(),
                request.getJobDescriptionText(),
                locale
        );
        WorkflowDocumentInput candidateProfile = careerDocumentUploadService.resolveDocument(
                workflowId,
                "candidateProfile",
                request.getCandidateProfileFile(),
                request.getCandidateProfileText(),
                locale
        );

        CareerWorkflowRequest normalizedRequest = new CareerWorkflowRequest(
                request.getMemoryId(),
                workflowId,
                normalizeOptional(request.getTargetRole()),
                normalizeOptional(request.getTargetLevel()),
                normalizeOptional(request.getCompanyName()),
                jobDescription.text(),
                candidateProfile.text(),
                normalizeFocusAreas(request.normalizedFocusAreas()),
                request.includeCompanyResearchEnabled(),
                locale
        );

        WorkflowVersionInfo versionInfo = new WorkflowVersionInfo(
                workflowId,
                null,
                1,
                localizedMessages.get(SupportedLocale.from(locale), "workflow.version.initial")
        );

        return runAndPersist(normalizedRequest, jobDescription, candidateProfile, versionInfo);
    }

    public CareerWorkflowComparisonResponse compare(CareerWorkflowComparisonRequest request) {
        String locale = careerLocaleResolver.resolveLanguageTag(request.locale());
        SupportedLocale supportedLocale = SupportedLocale.from(locale);
        List<CareerWorkflowComparisonTarget> targets = request.targets() == null ? List.of() : request.targets();

        if (targets.size() < 2 || targets.size() > 5) {
            throw new ApiValidationException(
                    localizedMessages.get(supportedLocale, "errors.request.validation"),
                    List.of(new ApiErrorDetail("targets", localizedMessages.get(supportedLocale, "errors.workflow.comparison.targets")))
            );
        }

        List<CareerWorkflowComparisonItem> items = targets.stream()
                .map(target -> {
                    CareerWorkflowResponse response = analyze(new CareerWorkflowRequest(
                        request.memoryId(),
                        null,
                        normalizeRequired(target.targetRole()),
                        normalizeOptional(target.targetLevel()),
                        normalizeOptional(target.companyName()),
                        normalizeRequired(target.jobDescription()),
                        normalizeRequired(request.candidateProfile()),
                        request.normalizedFocusAreas(),
                        request.includeCompanyResearch(),
                        locale
                    ));
                    return comparisonItem(response, target, supportedLocale);
                })
                .sorted(Comparator.comparingInt(CareerWorkflowComparisonItem::priorityScore).reversed())
                .toList();
        String recommendedWorkflowId = items.isEmpty() ? null : items.getFirst().workflowId();

        return new CareerWorkflowComparisonResponse(
                Instant.now(),
                supportedLocale.languageTag(),
                recommendedWorkflowId,
                comparisonSummary(items, supportedLocale),
                items
        );
    }

    public CareerWorkflowRefineResponse refine(String workflowId, CareerWorkflowRefineRequest request) {
        StoredWorkflowContext storedWorkflowContext = workflowSessionStore.findStoredContext(workflowId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, localizedMessages.get("errors.workflow.notFound")));

        String locale = careerLocaleResolver.resolveLanguageTag(
                request != null && request.locale() != null ? request.locale() : storedWorkflowContext.contentLocale()
        );
        SupportedLocale supportedLocale = SupportedLocale.from(locale);
        String message = request == null ? null : normalizeOptional(request.message());
        WorkflowRefineAction action = workflowRefineRouter.resolve(
                request == null ? null : request.action(),
                message,
                supportedLocale
        );

        if ((action == WorkflowRefineAction.APPEND_CANDIDATE_EVIDENCE || action == WorkflowRefineAction.CLARIFY_JD)
                && message == null) {
            throw new ApiValidationException(
                    localizedMessages.get(supportedLocale, "errors.request.validation"),
                    List.of(new ApiErrorDetail("message", localizedMessages.get(supportedLocale, "errors.workflow.refine.messageRequired")))
            );
        }

        if (action == WorkflowRefineAction.EXPLAIN_RESULT) {
            return new CareerWorkflowRefineResponse(
                    action.name(),
                    explainCurrentResult(storedWorkflowContext.response(), supportedLocale),
                    null,
                    workflowSessionStore.listVersions(workflowId)
            );
        }

        WorkflowVersionInfo nextVersionInfo = new WorkflowVersionInfo(
                storedWorkflowContext.versionInfo().rootWorkflowId(),
                storedWorkflowContext.response().workflowId(),
                storedWorkflowContext.versionInfo().versionNumber() + 1,
                versionReason(action, supportedLocale)
        );

        CareerWorkflowRequest refinedRequest = buildRefinedRequest(storedWorkflowContext, request, action, locale);
        CareerWorkflowResponse refinedResponse = runAndPersist(
                refinedRequest,
                WorkflowDocumentInput.text(refinedRequest.jobDescription()),
                WorkflowDocumentInput.text(refinedRequest.candidateProfile()),
                nextVersionInfo
        );

        return new CareerWorkflowRefineResponse(
                action.name(),
                buildRefineMessage(refinedResponse, action, supportedLocale),
                refinedResponse,
                workflowSessionStore.listVersions(refinedResponse.workflowId())
        );
    }

    private CareerWorkflowComparisonItem comparisonItem(CareerWorkflowResponse response,
                                                        CareerWorkflowComparisonTarget target,
                                                        SupportedLocale locale) {
        GapItem topGap = response.gapAnalysis() == null
                || response.gapAnalysis().priorityGaps() == null
                || response.gapAnalysis().priorityGaps().isEmpty()
                ? null
                : response.gapAnalysis().priorityGaps().getFirst();
        String fitLevel = response.decisionSummary() == null ? null : response.decisionSummary().fitLevel();
        String applyVerdict = response.decisionSummary() == null ? null : response.decisionSummary().applyVerdict();
        String confidence = response.confidenceSummary() == null ? null : response.confidenceSummary().overallConfidence();
        int score = priorityScore(fitLevel, applyVerdict, confidence, topGap);
        String prepCost = prepCost(topGap, applyVerdict, locale);

        return new CareerWorkflowComparisonItem(
                response.workflowId(),
                firstNonBlank(target.targetRole(), response.decisionSummary() == null ? null : response.decisionSummary().recommendedPositioning()),
                normalizeOptional(target.companyName()),
                fitLevel,
                applyVerdict,
                confidence,
                topGap == null ? null : topGap.requirement(),
                prepCost,
                score,
                response.decisionSummary() == null ? null : response.decisionSummary().applyVerdictReason()
        );
    }

    private int priorityScore(String fitLevel, String applyVerdict, String confidence, GapItem topGap) {
        int score = switch (normalizeKey(fitLevel)) {
            case "STRONG_MATCH" -> 45;
            case "COMPETITIVE_WITH_GAPS" -> 35;
            case "STRETCH" -> 22;
            case "LOW_MATCH", "NOT_RECOMMENDED" -> 8;
            default -> 15;
        };
        score += switch (normalizeKey(applyVerdict)) {
            case "APPLY_NOW" -> 30;
            case "APPLY_WITH_REFRAMING" -> 22;
            case "PREP_FIRST" -> 12;
            case "REDIRECT" -> -10;
            default -> 0;
        };
        score += switch (normalizeKey(confidence)) {
            case "HIGH" -> 10;
            case "MEDIUM" -> 5;
            default -> 0;
        };
        if (topGap != null && "HIGH".equalsIgnoreCase(topGap.hiringImpact())) {
            score -= 8;
        }
        return Math.max(0, score);
    }

    private String prepCost(GapItem topGap, String applyVerdict, SupportedLocale locale) {
        if ("APPLY_NOW".equalsIgnoreCase(applyVerdict)) {
            return locale.isChinese() ? "低：主要是表达打磨" : "Low: mostly narrative polish";
        }
        if (topGap == null || "LOW".equalsIgnoreCase(topGap.hiringImpact())) {
            return locale.isChinese() ? "中：需要补一轮岗位化表达" : "Medium: needs one positioning pass";
        }
        if ("HIGH".equalsIgnoreCase(topGap.hiringImpact())) {
            return locale.isChinese() ? "高：先补关键证据或集中准备" : "High: strengthen proof before investing";
        }
        return locale.isChinese() ? "中：建议先处理最高风险差距" : "Medium: address the highest-risk gap first";
    }

    private String comparisonSummary(List<CareerWorkflowComparisonItem> items, SupportedLocale locale) {
        if (items.isEmpty()) {
            return locale.isChinese() ? "当前没有可对比的岗位结果。" : "No comparable role results were returned.";
        }

        CareerWorkflowComparisonItem top = items.getFirst();
        return locale.isChinese()
                ? "优先考虑 %s；它当前的投递建议是 %s，准备成本为%s。".formatted(
                firstNonBlank(top.targetRole(), "排名第一的岗位"),
                firstNonBlank(top.applyVerdict(), "暂无"),
                firstNonBlank(top.prepCost(), "暂无"))
                : "Prioritize %s first; its current apply verdict is %s with %s prep cost.".formatted(
                firstNonBlank(top.targetRole(), "the top-ranked role"),
                firstNonBlank(top.applyVerdict(), "unknown"),
                firstNonBlank(top.prepCost(), "unknown"));
    }

    private String normalizeKey(String value) {
        return value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String firstNonBlank(String primary, String fallback) {
        String normalizedPrimary = normalizeOptional(primary);
        return normalizedPrimary == null ? normalizeOptional(fallback) : normalizedPrimary;
    }

    public List<WorkflowVersionSummary> listVersions(String workflowId) {
        return workflowSessionStore.listVersions(workflowId);
    }

    private CareerWorkflowRequest buildRefinedRequest(StoredWorkflowContext storedWorkflowContext,
                                                      CareerWorkflowRefineRequest request,
                                                      WorkflowRefineAction action,
                                                      String locale) {
        String nextWorkflowId = UUID.randomUUID().toString();
        String message = request == null ? null : normalizeOptional(request.message());
        List<String> focusAreas = new ArrayList<>(storedWorkflowContext.focusAreas());
        String jobDescription = storedWorkflowContext.jobDescriptionText();
        String candidateProfile = storedWorkflowContext.candidateProfileText();

        switch (action) {
            case APPEND_CANDIDATE_EVIDENCE -> {
                candidateProfile = joinWithContext(candidateProfile, localizedMessages.get(SupportedLocale.from(locale), "workflow.refine.appendCandidateEvidence"), message);
                focusAreas.add(localizedMessages.get(SupportedLocale.from(locale), "workflow.focus.resumeEvidence"));
            }
            case CLARIFY_JD -> {
                jobDescription = joinWithContext(jobDescription, localizedMessages.get(SupportedLocale.from(locale), "workflow.refine.appendJdClarification"), message);
                focusAreas.add(localizedMessages.get(SupportedLocale.from(locale), "workflow.focus.roleClarification"));
            }
            case RERUN_GAP_ANALYSIS -> focusAreas.add(localizedMessages.get(SupportedLocale.from(locale), "workflow.focus.gapRefresh"));
            case RERUN_INTERVIEW_PREP -> focusAreas.add(localizedMessages.get(SupportedLocale.from(locale), "workflow.focus.interviewRefresh"));
            case CREATE_PREP_PLAN -> focusAreas.add(localizedMessages.get(SupportedLocale.from(locale), "workflow.focus.prepPlan"));
            default -> {
            }
        }

        if (message != null && (action == WorkflowRefineAction.RERUN_GAP_ANALYSIS
                || action == WorkflowRefineAction.RERUN_INTERVIEW_PREP
                || action == WorkflowRefineAction.CREATE_PREP_PLAN)) {
            focusAreas.add(message);
        }

        Integer memoryId = request != null && request.memoryId() != null ? request.memoryId() : storedWorkflowContext.memoryId();

        return new CareerWorkflowRequest(
                memoryId,
                nextWorkflowId,
                storedWorkflowContext.targetRole(),
                storedWorkflowContext.targetLevel(),
                storedWorkflowContext.companyName(),
                jobDescription,
                candidateProfile,
                normalizeFocusAreas(focusAreas),
                storedWorkflowContext.includeCompanyResearch(),
                locale
        );
    }

    private CareerWorkflowResponse runAndPersist(CareerWorkflowRequest request,
                                                 WorkflowDocumentInput jobDescription,
                                                 WorkflowDocumentInput candidateProfile,
                                                 WorkflowVersionInfo versionInfo) {
        CareerWorkflowResponse rawResponse = careerWorkflowOrchestrator.run(request);
        CareerWorkflowResponse response = annotateResponse(rawResponse, request, versionInfo);

        return workflowSessionStore.save(
                new WorkflowPersistenceRecord(
                        response,
                        versionInfo.rootWorkflowId(),
                        versionInfo.parentWorkflowId(),
                        versionInfo.versionNumber(),
                        versionInfo.versionReason(),
                        request.memoryId(),
                        request.targetRole(),
                        request.targetLevel(),
                        request.companyName(),
                        request.includeCompanyResearch(),
                        request.normalizedFocusAreas(),
                        jobDescription,
                        candidateProfile
                )
        );
    }

    private CareerWorkflowResponse annotateResponse(CareerWorkflowResponse rawResponse,
                                                    CareerWorkflowRequest request,
                                                    WorkflowVersionInfo versionInfo) {
        SupportedLocale locale = SupportedLocale.from(request.locale());
        boolean usedSearch = workflowCapabilityStatus.searchAvailable()
                && request.includeCompanyResearch()
                && normalizeOptional(request.companyName()) != null;
        boolean usedRetrieval = workflowCapabilityStatus.retrievalAvailable();
        List<String> degradationNotes = buildDegradationNotes(request, locale, usedSearch, usedRetrieval);
        boolean degradedMode = !degradationNotes.isEmpty();
        ConfidenceSummary confidenceSummary = annotateConfidenceSummary(rawResponse, degradationNotes, locale);
        List<EvidenceItem> decisionDrivers = annotateDecisionDrivers(rawResponse, usedSearch, usedRetrieval, degradationNotes, locale);
        List<String> clarificationQuestions = annotateClarificationQuestions(rawResponse, degradationNotes, locale);

        return new CareerWorkflowResponse(
                rawResponse.workflowId(),
                rawResponse.generatedAt(),
                rawResponse.contentLocale(),
                versionInfo,
                rawResponse.decisionSummary(),
                confidenceSummary,
                decisionDrivers,
                clarificationQuestions,
                rawResponse.jdAnalysis(),
                rawResponse.candidateAnalysis(),
                rawResponse.gapAnalysis(),
                rawResponse.interviewPrep(),
                usedSearch,
                usedRetrieval,
                degradedMode,
                degradationNotes,
                rawResponse.actionPlan(),
                rawResponse.nextSteps(),
                rawResponse.supportCapabilities()
        );
    }

    private ConfidenceSummary annotateConfidenceSummary(CareerWorkflowResponse response,
                                                        List<String> degradationNotes,
                                                        SupportedLocale locale) {
        ConfidenceSummary summary = response.confidenceSummary();
        if (summary == null) {
            return new ConfidenceSummary(
                    "LOW",
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    degradationNotes,
                    localizedMessages.get(locale, "workflow.confidence.rationale.noSummary")
            );
        }

        List<String> blockingUncertainties = new ArrayList<>();
        if (summary.blockingUncertainties() != null) {
            blockingUncertainties.addAll(summary.blockingUncertainties());
        }
        blockingUncertainties.addAll(degradationNotes);

        String confidenceRationale = summary.confidenceRationale();
        if (degradationNotes.isEmpty()) {
            confidenceRationale = normalizeOptional(confidenceRationale);
        } else if (normalizeOptional(confidenceRationale) == null) {
            confidenceRationale = localizedMessages.get(locale, "workflow.confidence.rationale.degraded");
        } else {
            confidenceRationale = confidenceRationale + " " + localizedMessages.get(locale, "workflow.confidence.rationale.degraded");
        }

        return new ConfidenceSummary(
                summary.overallConfidence(),
                summary.strongestEvidence() == null ? List.of() : summary.strongestEvidence(),
                summary.missingEvidence() == null ? List.of() : summary.missingEvidence(),
                summary.inferenceNotes() == null ? List.of() : summary.inferenceNotes(),
                summary.mostInfluentialGaps() == null ? List.of() : summary.mostInfluentialGaps(),
                blockingUncertainties.stream().distinct().toList(),
                confidenceRationale
        );
    }

    private List<EvidenceItem> annotateDecisionDrivers(CareerWorkflowResponse response,
                                                       boolean usedSearch,
                                                       boolean usedRetrieval,
                                                       List<String> degradationNotes,
                                                       SupportedLocale locale) {
        List<EvidenceItem> decisionDrivers = new ArrayList<>();
        if (response.decisionDrivers() != null) {
            decisionDrivers.addAll(response.decisionDrivers());
        }
        if (usedRetrieval) {
            decisionDrivers.add(new EvidenceItem(
                    localizedMessages.get(locale, "workflow.decisionDriver.retrieval"),
                    EvidenceSourceType.RETRIEVAL
            ));
        }
        if (usedSearch) {
            decisionDrivers.add(new EvidenceItem(
                    localizedMessages.get(locale, "workflow.decisionDriver.search"),
                    EvidenceSourceType.SEARCH
            ));
        }
        degradationNotes.stream()
                .map(note -> new EvidenceItem(note, EvidenceSourceType.FALLBACK))
                .forEach(decisionDrivers::add);

        return decisionDrivers.stream()
                .filter(item -> item != null && normalizeOptional(item.statement()) != null)
                .distinct()
                .limit(6)
                .toList();
    }

    private List<String> annotateClarificationQuestions(CareerWorkflowResponse response,
                                                        List<String> degradationNotes,
                                                        SupportedLocale locale) {
        List<String> questions = new ArrayList<>();
        if (response.clarificationQuestions() != null) {
            questions.addAll(response.clarificationQuestions());
        }

        if (!degradationNotes.isEmpty()) {
            questions.add(localizedMessages.get(locale, "workflow.clarification.degraded"));
        }

        return questions.stream()
                .map(this::normalizeOptional)
                .filter(value -> value != null)
                .distinct()
                .limit(3)
                .toList();
    }

    private List<String> buildDegradationNotes(CareerWorkflowRequest request,
                                               SupportedLocale locale,
                                               boolean usedSearch,
                                               boolean usedRetrieval) {
        List<String> notes = new ArrayList<>();

        if (workflowCapabilityStatus.isDemoMode()) {
            notes.add(localizedMessages.get(locale, "workflow.degraded.demoMode"));
        }
        if (!usedRetrieval) {
            notes.add(localizedMessages.get(locale, "workflow.degraded.noRetrieval"));
        }
        if (request.includeCompanyResearch() && normalizeOptional(request.companyName()) != null && !usedSearch) {
            notes.add(localizedMessages.get(locale, "workflow.degraded.noSearch"));
        }

        return notes.stream().distinct().toList();
    }

    private String explainCurrentResult(CareerWorkflowResponse response, SupportedLocale locale) {
        String fitLevel = response.decisionSummary() == null ? null : response.decisionSummary().fitLevel();
        String confidence = response.confidenceSummary() == null ? null : response.confidenceSummary().overallConfidence();
        String nextMove = response.decisionSummary() == null ? null : response.decisionSummary().recommendedNextMove();

        return locale.isChinese()
                ? "当前判断是 %s，整体结论强度为 %s。最值得优先执行的动作是：%s".formatted(
                fitLevel == null ? "未明确" : fitLevel,
                confidence == null ? "未明确" : confidence,
                nextMove == null ? "先回到结果页检查最高优先级差距" : nextMove
        )
                : "The current fit call is %s with %s decision confidence. The most useful next move is: %s".formatted(
                fitLevel == null ? "not specified" : fitLevel,
                confidence == null ? "unspecified" : confidence,
                nextMove == null ? "review the highest-priority gaps in the workflow result" : nextMove
        );
    }

    private String buildRefineMessage(CareerWorkflowResponse response,
                                      WorkflowRefineAction action,
                                      SupportedLocale locale) {
        String versionLabel = "v" + response.versionInfo().versionNumber();
        return switch (action) {
            case APPEND_CANDIDATE_EVIDENCE -> locale.isChinese()
                    ? "已基于你补充的候选人经历生成 %s，并刷新差距与准备建议。".formatted(versionLabel)
                    : "Generated %s after adding your new candidate evidence and refreshed the gaps and prep guidance.".formatted(versionLabel);
            case CLARIFY_JD -> locale.isChinese()
                    ? "已基于你补充的 JD 信息生成 %s，并更新岗位要求与差距判断。".formatted(versionLabel)
                    : "Generated %s after clarifying the JD and refreshed the role requirements and gap call.".formatted(versionLabel);
            case RERUN_GAP_ANALYSIS -> locale.isChinese()
                    ? "已重新运行差距分析，并生成新的 %s。".formatted(versionLabel)
                    : "Re-ran the gap analysis and created a new %s.".formatted(versionLabel);
            case RERUN_INTERVIEW_PREP -> locale.isChinese()
                    ? "已重新生成面试准备建议，并创建新的 %s。".formatted(versionLabel)
                    : "Regenerated interview prep guidance and created a new %s.".formatted(versionLabel);
            case CREATE_PREP_PLAN -> locale.isChinese()
                    ? "已围绕新的 follow-up 生成更聚焦的准备计划，并创建 %s。".formatted(versionLabel)
                    : "Created a more focused prep plan from your follow-up and saved it as %s.".formatted(versionLabel);
            default -> locale.isChinese()
                    ? "已根据你的 follow-up 生成新的 workflow 版本 %s。".formatted(versionLabel)
                    : "Created a new workflow version %s from your follow-up.".formatted(versionLabel);
        };
    }

    private String versionReason(WorkflowRefineAction action, SupportedLocale locale) {
        return switch (action) {
            case APPEND_CANDIDATE_EVIDENCE -> localizedMessages.get(locale, "workflow.version.appendCandidateEvidence");
            case CLARIFY_JD -> localizedMessages.get(locale, "workflow.version.clarifyJd");
            case RERUN_GAP_ANALYSIS -> localizedMessages.get(locale, "workflow.version.rerunGap");
            case RERUN_INTERVIEW_PREP -> localizedMessages.get(locale, "workflow.version.rerunInterviewPrep");
            case CREATE_PREP_PLAN -> localizedMessages.get(locale, "workflow.version.createPrepPlan");
            default -> localizedMessages.get(locale, "workflow.version.initial");
        };
    }

    private String joinWithContext(String original, String label, String message) {
        String base = normalizeRequired(original);
        if (message == null) {
            return base;
        }
        return """
                %s

                %s:
                %s
                """.formatted(base, label, message).trim();
    }

    private List<String> normalizeFocusAreas(List<String> focusAreas) {
        return focusAreas == null
                ? List.of()
                : focusAreas.stream()
                .map(this::normalizeOptional)
                .filter(value -> value != null)
                .distinct()
                .toList();
    }

    private String resolveWorkflowId(String workflowId) {
        String normalizedWorkflowId = normalizeOptional(workflowId);
        return normalizedWorkflowId == null ? UUID.randomUUID().toString() : normalizedWorkflowId;
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
