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
import com.workspace.codeforgeai.common.i18n.SupportedLocale;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CareerWorkflowOrchestrator {

    private final JdParsingAiService jdParsingAiService;
    private final CandidateAnalysisAiService candidateAnalysisAiService;
    private final GapAnalysisAiService gapAnalysisAiService;
    private final InterviewPrepAiService interviewPrepAiService;
    private final LocalizedMessages localizedMessages;
    private final CareerWorkflowConfidenceBuilder confidenceBuilder;

    public CareerWorkflowOrchestrator(JdParsingAiService jdParsingAiService,
                                      CandidateAnalysisAiService candidateAnalysisAiService,
                                      GapAnalysisAiService gapAnalysisAiService,
                                      InterviewPrepAiService interviewPrepAiService,
                                      LocalizedMessages localizedMessages,
                                      CareerWorkflowConfidenceBuilder confidenceBuilder) {
        this.jdParsingAiService = jdParsingAiService;
        this.candidateAnalysisAiService = candidateAnalysisAiService;
        this.gapAnalysisAiService = gapAnalysisAiService;
        this.interviewPrepAiService = interviewPrepAiService;
        this.localizedMessages = localizedMessages;
        this.confidenceBuilder = confidenceBuilder;
    }

    public CareerWorkflowResponse run(CareerWorkflowRequest request) {
        String workflowId = isBlank(request.workflowId()) ? UUID.randomUUID().toString() : request.workflowId();
        SupportedLocale locale = SupportedLocale.from(request.locale());

        JdAnalysisResult jdAnalysis = confidenceBuilder.enrichJdAnalysis(
                request,
                jdParsingAiService.analyze(buildJdInput(request))
        );
        CandidateAnalysisResult candidateAnalysis = confidenceBuilder.enrichCandidateAnalysis(
                request,
                candidateAnalysisAiService.analyze(buildCandidateInput(request))
        );
        GapAnalysisResult gapAnalysis = confidenceBuilder.enrichGapAnalysis(
                request,
                gapAnalysisAiService.analyze(buildGapInput(request, jdAnalysis, candidateAnalysis)),
                jdAnalysis,
                candidateAnalysis
        );
        InterviewPrepResult interviewPrep = confidenceBuilder.enrichInterviewPrep(
                request,
                interviewPrepAiService.analyze(
                        buildInterviewPrepInput(request, jdAnalysis, candidateAnalysis, gapAnalysis)
                ),
                gapAnalysis
        );

        DecisionSummary decisionSummary = buildDecisionSummary(request, jdAnalysis, gapAnalysis);
        ConfidenceSummary confidenceSummary = confidenceBuilder.buildConfidenceSummary(
                request,
                jdAnalysis,
                candidateAnalysis,
                gapAnalysis,
                interviewPrep
        );
        List<EvidenceItem> decisionDrivers = confidenceBuilder.buildDecisionDrivers(
                request,
                jdAnalysis,
                candidateAnalysis,
                gapAnalysis,
                interviewPrep
        );
        List<String> clarificationQuestions = confidenceBuilder.buildClarificationQuestions(
                request,
                jdAnalysis,
                candidateAnalysis,
                gapAnalysis
        );
        CareerWorkflowResponse response = new CareerWorkflowResponse(
                workflowId,
                Instant.now(),
                locale.languageTag(),
                new WorkflowVersionInfo(workflowId, null, 1, localizedMessages.get(locale, "workflow.version.initial")),
                decisionSummary,
                confidenceSummary,
                decisionDrivers,
                clarificationQuestions,
                jdAnalysis,
                candidateAnalysis,
                gapAnalysis,
                interviewPrep,
                false,
                false,
                false,
                List.of(),
                buildActionPlan(decisionSummary, gapAnalysis, interviewPrep, locale),
                buildNextSteps(decisionSummary, gapAnalysis, interviewPrep, locale),
                supportCapabilities(locale)
        );

        return response;
    }

    private DecisionSummary buildDecisionSummary(CareerWorkflowRequest request,
                                                 JdAnalysisResult jdAnalysis,
                                                 GapAnalysisResult gapAnalysis) {
        List<String> topPriorities = gapAnalysis.priorityGaps() == null
                ? List.of()
                : gapAnalysis.priorityGaps().stream()
                .limit(3)
                .map(gap -> gap.requirement() + ": " + gap.recommendation())
                .collect(Collectors.toList());

        String targetRole = firstNonBlank(request.targetRole(), jdAnalysis.roleTitle(), "Target role");
        SupportedLocale locale = SupportedLocale.from(request.locale());

        return new DecisionSummary(
                firstNonBlank(gapAnalysis.overallAssessment(), "UNKNOWN"),
                applyVerdict(gapAnalysis),
                applyVerdictReason(gapAnalysis, locale),
                targetRole,
                firstNonBlank(gapAnalysis.matchNarrative(), defaultMatchNarrative(locale)),
                recommendedNextMove(gapAnalysis.overallAssessment(), locale),
                topPriorities
        );
    }

    private String applyVerdict(GapAnalysisResult gapAnalysis) {
        String assessment = gapAnalysis == null ? null : gapAnalysis.overallAssessment();
        List<GapItem> gaps = gapAnalysis == null || gapAnalysis.priorityGaps() == null ? List.of() : gapAnalysis.priorityGaps();
        boolean hasHighGap = gaps.stream().anyMatch(gap -> "HIGH".equalsIgnoreCase(gap.gapLevel()));

        if (assessment == null) {
            return "PREP_FIRST";
        }

        return switch (assessment.toUpperCase()) {
            case "STRONG_MATCH" -> hasHighGap ? "APPLY_WITH_REFRAMING" : "APPLY_NOW";
            case "COMPETITIVE_WITH_GAPS" -> hasHighGap ? "PREP_FIRST" : "APPLY_WITH_REFRAMING";
            case "STRETCH" -> "PREP_FIRST";
            case "LOW_MATCH", "NOT_RECOMMENDED" -> "REDIRECT";
            default -> "PREP_FIRST";
        };
    }

    private String applyVerdictReason(GapAnalysisResult gapAnalysis, SupportedLocale locale) {
        String verdict = applyVerdict(gapAnalysis);
        GapItem topGap = gapAnalysis == null || gapAnalysis.priorityGaps() == null || gapAnalysis.priorityGaps().isEmpty()
                ? null
                : gapAnalysis.priorityGaps().get(0);
        String topGapRequirement = topGap == null ? null : topGap.requirement();

        return switch (verdict) {
            case "APPLY_NOW" -> locale.isChinese()
                    ? "当前优势已经足以支撑投递，剩余问题更多是表达优化，而不是硬性阻塞。"
                    : "The current evidence is already strong enough to support an application, and the remaining work is mostly narrative polish rather than a hard blocker.";
            case "APPLY_WITH_REFRAMING" -> locale.isChinese()
                    ? "可以投递，但建议先把最关键的经历和成果重新组织好，避免优势没有被看见。"
                    : "You can apply now, but the strongest evidence should be reframed first so the role-fit signal is obvious.";
            case "PREP_FIRST" -> locale.isChinese()
                    ? "现在最影响结果的是%s，建议先补这类证据或准备表达，再投入这条申请线。".formatted(
                    firstNonBlank(topGapRequirement, "最高风险差距"))
                    : "The biggest blocker right now is %s, so it is better to strengthen the proof or interview narrative before investing in this application."
                    .formatted(firstNonBlank(topGapRequirement, "the highest-risk gap"));
            case "REDIRECT" -> locale.isChinese()
                    ? "当前背景和岗位核心要求之间仍有明显距离，继续投入这条岗位的回报可能不高。"
                    : "The current profile still sits too far from the role's core requirements, so continued investment is unlikely to pay off yet.";
            default -> locale.isChinese()
                    ? "当前结论更适合作为方向性参考，建议结合最高风险差距决定下一步。"
                    : "Treat the current result as directional and use the highest-risk gaps to decide the next move.";
        };
    }

    private String recommendedNextMove(String overallAssessment, SupportedLocale locale) {
        if (overallAssessment == null) {
            return localizedMessages.get(locale, "workflow.nextMove.default");
        }

        return switch (overallAssessment.toUpperCase()) {
            case "STRONG_MATCH" -> localizedMessages.get(locale, "workflow.nextMove.strong");
            case "COMPETITIVE_WITH_GAPS" -> localizedMessages.get(locale, "workflow.nextMove.competitive");
            case "STRETCH" -> localizedMessages.get(locale, "workflow.nextMove.stretch");
            case "LOW_MATCH" -> localizedMessages.get(locale, "workflow.nextMove.low");
            default -> localizedMessages.get(locale, "workflow.nextMove.default");
        };
    }

    private List<String> buildNextSteps(DecisionSummary decisionSummary,
                                        GapAnalysisResult gapAnalysis,
                                        InterviewPrepResult interviewPrep,
                                        SupportedLocale locale) {
        List<String> nextSteps = new ArrayList<>();
        String applyVerdict = decisionSummary == null ? null : decisionSummary.applyVerdict();

        if (applyVerdict != null) {
            nextSteps.add(primaryDecisionAction(applyVerdict, gapAnalysis, locale));
        } else if (decisionSummary != null && decisionSummary.recommendedNextMove() != null && !decisionSummary.recommendedNextMove().isBlank()) {
            nextSteps.add(decisionSummary.recommendedNextMove().trim());
        }

        if (gapAnalysis.priorityGaps() != null) {
            gapAnalysis.priorityGaps().stream()
                    .map(GapItem::recommendation)
                    .filter(step -> step != null && !step.isBlank())
                    .limit(2)
                    .forEach(nextSteps::add);
        }

        if (interviewPrep.prepPlan() != null) {
            interviewPrep.prepPlan().stream()
                    .filter(step -> step != null && !step.isBlank())
                    .limit(2)
                    .forEach(nextSteps::add);
        }

        if (nextSteps.isEmpty()) {
            nextSteps.add(localizedMessages.get(locale, "workflow.nextSteps.default.resume"));
            nextSteps.add(localizedMessages.get(locale, "workflow.nextSteps.default.prep"));
        }

        return nextSteps.stream().distinct().limit(5).collect(Collectors.toList());
    }

    private String primaryDecisionAction(String applyVerdict,
                                         GapAnalysisResult gapAnalysis,
                                         SupportedLocale locale) {
        GapItem topGap = gapAnalysis == null || gapAnalysis.priorityGaps() == null || gapAnalysis.priorityGaps().isEmpty()
                ? null
                : gapAnalysis.priorityGaps().get(0);
        String topGapRequirement = firstNonBlank(topGap == null ? null : topGap.requirement(),
                locale.isChinese() ? "最高优先级差距" : "the highest-priority gap");

        return switch (applyVerdict) {
            case "APPLY_NOW" -> locale.isChinese()
                    ? "建议现在投递，并在投递前先把最能支撑岗位匹配的经历前置。"
                    : "Apply now, and move the strongest role-relevant evidence to the top of the resume before submitting.";
            case "APPLY_WITH_REFRAMING" -> locale.isChinese()
                    ? "建议先完成一轮简历重写与表达优化，再尽快投递。"
                    : "Reframe the strongest evidence first, then apply without waiting too long.";
            case "PREP_FIRST" -> locale.isChinese()
                    ? "建议先围绕%s做一轮 3-7 天补强，再决定是否投递。".formatted(topGapRequirement)
                    : "Spend 3-7 days strengthening %s before deciding whether to apply.".formatted(topGapRequirement);
            case "REDIRECT" -> locale.isChinese()
                    ? "建议先转向更接近当前背景的岗位，或补齐关键证据后再回来评估。"
                    : "Reallocate effort toward closer-fit roles, or return after building stronger evidence.";
            default -> locale.isChinese()
                    ? "先围绕最高风险差距做一轮聚焦准备，再决定下一步。"
                    : "Run a focused prep sprint against the top gap before choosing the next move.";
        };
    }

    private List<ActionPlanStep> buildActionPlan(DecisionSummary decisionSummary,
                                                 GapAnalysisResult gapAnalysis,
                                                 InterviewPrepResult interviewPrep,
                                                 SupportedLocale locale) {
        List<GapItem> gaps = gapAnalysis == null || gapAnalysis.priorityGaps() == null ? List.of() : gapAnalysis.priorityGaps();
        String topGap = gaps.isEmpty() ? (locale.isChinese() ? "岗位匹配表达" : "role-fit framing") : firstNonBlank(gaps.get(0).requirement(), locale.isChinese() ? "最高优先级差距" : "top gap");
        String secondGap = gaps.size() > 1 ? firstNonBlank(gaps.get(1).requirement(), topGap) : topGap;
        String thirdGap = gaps.size() > 2 ? firstNonBlank(gaps.get(2).requirement(), secondGap) : secondGap;
        String verdict = decisionSummary == null ? "PREP_FIRST" : firstNonBlank(decisionSummary.applyVerdict(), "PREP_FIRST");

        List<ActionPlanStep> actionPlan = new ArrayList<>();
        actionPlan.add(new ActionPlanStep(
                locale.isChinese() ? "第 1 天" : "Day 1",
                locale.isChinese() ? "收敛投递判断与简历主线" : "Lock the application call and resume narrative",
                List.of(
                        locale.isChinese()
                                ? "用一句话写清楚：为什么你适合这个岗位，为什么是现在。"
                                : "Write a one-line case for why you fit this role and why now.",
                        locale.isChinese()
                                ? "把与 %s 最相关的经历和结果放到简历前半页。".formatted(topGap)
                                : "Move the strongest evidence tied to %s into the top half of the resume.".formatted(topGap)
                ),
                locale.isChinese()
                        ? "你能在 30 秒内讲清楚是否投递，以及最强匹配点。"
                        : "You can explain the application decision and strongest fit point in 30 seconds."
        ));
        actionPlan.add(new ActionPlanStep(
                locale.isChinese() ? "第 2-3 天" : "Day 2-3",
                locale.isChinese() ? "补强最影响结果的前两项差距" : "Strengthen the two gaps most likely to change the outcome",
                List.of(
                        locale.isChinese()
                                ? "分别为 %s 和 %s 准备一段具体案例、数据结果或职责范围说明。".formatted(topGap, secondGap)
                                : "Prepare one concrete story, metric, or ownership explanation for %s and %s.".formatted(topGap, secondGap),
                        locale.isChinese()
                                ? "把这两项差距各改成一条简历 bullet 和一段面试表达。"
                                : "Turn each gap into one resume bullet and one interview-ready narrative."
                ),
                locale.isChinese()
                        ? "前两项 gap 都能用具体项目或结果解释，而不是只能抽象描述。"
                        : "Both top gaps can be explained with concrete project evidence instead of abstractions."
        ));
        actionPlan.add(new ActionPlanStep(
                locale.isChinese() ? "第 4-5 天" : "Day 4-5",
                locale.isChinese() ? "围绕最高风险问题进行面试化训练" : "Rehearse the highest-risk interview questions",
                List.of(
                        locale.isChinese()
                                ? "围绕 %s 准备 3 个高概率追问，并各自写出回答提纲。".formatted(topGap)
                                : "Draft three likely follow-up questions around %s and outline answers for each.".formatted(topGap),
                        locale.isChinese()
                                ? "把 %s 的回答练到可以用业务语言讲清楚。".formatted(secondGap)
                                : "Practice %s until you can explain it in plain business language.".formatted(secondGap)
                ),
                locale.isChinese()
                        ? "你能连续回答 5 分钟追问，且不再依赖模糊表述。"
                        : "You can handle five minutes of follow-ups without falling back on vague language."
        ));

        if (!"APPLY_NOW".equalsIgnoreCase(verdict)) {
            actionPlan.add(new ActionPlanStep(
                    locale.isChinese() ? "第 6-7 天" : "Day 6-7",
                    locale.isChinese() ? "根据结果决定投递、延后或转向" : "Decide whether to apply, delay, or redirect",
                    List.of(
                            locale.isChinese()
                                    ? "重新跑一次分析，确认 %s 和 %s 是否已经被明显补强。".formatted(topGap, thirdGap)
                                    : "Rerun the workflow and verify whether %s and %s are now materially stronger.".formatted(topGap, thirdGap),
                            locale.isChinese()
                                    ? "如果结论仍然偏弱，就把时间转向更接近当前背景的岗位。"
                                    : "If the verdict remains weak, redirect effort toward better-fit roles."
                    ),
                    locale.isChinese()
                            ? "你能基于更新后的结果明确决定：现在投、稍后投，还是暂不投入。"
                            : "You can make a clear apply-now, apply-later, or do-not-invest decision from the refreshed result."
            ));
        }

        return actionPlan;
    }

    private String buildJdInput(CareerWorkflowRequest request) {
        return """
                Workflow stage: JD parsing
                Requested output locale: %s
                Target role hint: %s
                Target level hint: %s
                Company hint: %s
                
                Raw job description:
                %s
                """.formatted(
                SupportedLocale.from(request.locale()).languageTag(),
                firstNonBlank(request.targetRole(), "Not provided"),
                firstNonBlank(request.targetLevel(), "Not provided"),
                firstNonBlank(request.companyName(), "Not provided"),
                request.jobDescription()
        );
    }

    private String buildCandidateInput(CareerWorkflowRequest request) {
        return """
                Workflow stage: candidate analysis
                Requested output locale: %s
                Target role: %s
                Target level: %s
                Candidate profile or resume:
                %s
                """.formatted(
                SupportedLocale.from(request.locale()).languageTag(),
                firstNonBlank(request.targetRole(), "Not provided"),
                firstNonBlank(request.targetLevel(), "Not provided"),
                request.candidateProfile()
        );
    }

    private String buildGapInput(CareerWorkflowRequest request,
                                 JdAnalysisResult jdAnalysis,
                                 CandidateAnalysisResult candidateAnalysis) {
        return """
                Workflow stage: gap analysis
                Requested output locale: %s
                Requested role: %s
                Requested level: %s
                Focus areas: %s
                
                JD analysis
                Role title: %s
                Seniority: %s
                Summary: %s
                Must-have requirements:
                - %s
                Nice-to-have requirements:
                - %s
                
                Candidate analysis
                Headline: %s
                Summary: %s
                Strengths:
                - %s
                Missing signals:
                - %s
                Evidence:
                - %s
                """.formatted(
                SupportedLocale.from(request.locale()).languageTag(),
                firstNonBlank(request.targetRole(), "Not provided"),
                firstNonBlank(request.targetLevel(), "Not provided"),
                joinItems(request.normalizedFocusAreas()),
                firstNonBlank(jdAnalysis.roleTitle(), "Unknown"),
                firstNonBlank(jdAnalysis.seniority(), "Unknown"),
                firstNonBlank(jdAnalysis.summary(), "No summary returned"),
                joinItems(jdAnalysis.mustHaveRequirements()),
                joinItems(jdAnalysis.niceToHaveRequirements()),
                firstNonBlank(candidateAnalysis.candidateHeadline(), "Unknown"),
                firstNonBlank(candidateAnalysis.summary(), "No summary returned"),
                joinItems(candidateAnalysis.strengths()),
                joinItems(candidateAnalysis.missingSignals()),
                joinItems(candidateAnalysis.evidence())
        );
    }

    private String buildInterviewPrepInput(CareerWorkflowRequest request,
                                           JdAnalysisResult jdAnalysis,
                                           CandidateAnalysisResult candidateAnalysis,
                                           GapAnalysisResult gapAnalysis) {
        return """
                Workflow stage: interview prep generation
                Requested output locale: %s
                Company name: %s
                Include company research: %s
                Focus areas: %s
                
                JD analysis
                Role title: %s
                Seniority: %s
                Must-have requirements:
                - %s
                Interview focus areas:
                - %s
                
                Candidate analysis
                Headline: %s
                Strengths:
                - %s
                Missing signals:
                - %s
                
                Gap analysis
                Overall assessment: %s
                Match narrative: %s
                Positioning advice:
                - %s
                Priority gaps:
                - %s
                """.formatted(
                SupportedLocale.from(request.locale()).languageTag(),
                firstNonBlank(request.companyName(), "Not provided"),
                request.includeCompanyResearch(),
                joinItems(request.normalizedFocusAreas()),
                firstNonBlank(jdAnalysis.roleTitle(), "Unknown"),
                firstNonBlank(jdAnalysis.seniority(), "Unknown"),
                joinItems(jdAnalysis.mustHaveRequirements()),
                joinItems(jdAnalysis.interviewFocusAreas()),
                firstNonBlank(candidateAnalysis.candidateHeadline(), "Unknown"),
                joinItems(candidateAnalysis.strengths()),
                joinItems(candidateAnalysis.missingSignals()),
                firstNonBlank(gapAnalysis.overallAssessment(), "Unknown"),
                firstNonBlank(gapAnalysis.matchNarrative(), "No narrative returned"),
                joinItems(gapAnalysis.positioningAdvice()),
                joinGapItems(gapAnalysis.priorityGaps())
        );
    }

    private String joinItems(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "None";
        }
        return items.stream()
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.joining("\n- "));
    }

    private String joinGapItems(List<GapItem> gapItems) {
        if (gapItems == null || gapItems.isEmpty()) {
            return "None";
        }

        return gapItems.stream()
                .map(gap -> "%s | evidence: %s | gap: %s | action: %s".formatted(
                        firstNonBlank(gap.requirement(), "Unknown requirement"),
                        firstNonBlank(gap.candidateEvidence(), "No evidence"),
                        firstNonBlank(gap.gapLevel(), "Unknown"),
                        firstNonBlank(gap.recommendation(), "No recommendation")
                ))
                .collect(Collectors.joining("\n- "));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private List<String> supportCapabilities(SupportedLocale locale) {
        return List.of(
                localizedMessages.get(locale, "workflow.supportCapabilities.orchestration"),
                localizedMessages.get(locale, "workflow.supportCapabilities.rag"),
                localizedMessages.get(locale, "workflow.supportCapabilities.search"),
                localizedMessages.get(locale, "workflow.supportCapabilities.tools"),
                localizedMessages.get(locale, "workflow.supportCapabilities.chat")
        );
    }

    private String defaultMatchNarrative(SupportedLocale locale) {
        return locale.isChinese()
                ? "工作流已完成，但没有返回明确的匹配结论。"
                : "The workflow completed, but a fit narrative was not returned.";
    }
}
