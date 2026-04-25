package com.workspace.codeforgeai.career.workflow;

import com.workspace.codeforgeai.career.api.CareerWorkflowRequest;
import com.workspace.codeforgeai.career.candidate.CandidateAnalysisResult;
import com.workspace.codeforgeai.career.gap.GapAnalysisResult;
import com.workspace.codeforgeai.career.gap.GapItem;
import com.workspace.codeforgeai.career.interview.InterviewPrepResult;
import com.workspace.codeforgeai.career.jd.JdAnalysisResult;
import com.workspace.codeforgeai.common.i18n.SupportedLocale;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class CareerWorkflowConfidenceBuilder {

    public JdAnalysisResult enrichJdAnalysis(CareerWorkflowRequest request, JdAnalysisResult jdAnalysis) {
        SupportedLocale locale = SupportedLocale.from(request.locale());
        List<String> strongestEvidence = new ArrayList<>();
        List<String> missingEvidence = new ArrayList<>();
        List<String> inferenceNotes = new ArrayList<>();

        int observedSignals = 0;
        int inferredSignals = 0;

        if (hasItems(jdAnalysis.mustHaveRequirements())) {
            observedSignals += Math.min(jdAnalysis.mustHaveRequirements().size(), 3);
            strongestEvidence.add(text(
                    locale,
                    "The JD exposes %s explicit must-have requirements.".formatted(jdAnalysis.mustHaveRequirements().size()),
                    "JD 中明确给出了 %s 条核心必备要求。".formatted(jdAnalysis.mustHaveRequirements().size())
            ));
        }
        if (hasItems(jdAnalysis.keywords())) {
            observedSignals += Math.min(jdAnalysis.keywords().size(), 2);
            strongestEvidence.add(text(
                    locale,
                    "Role-specific keywords are visible and usable for screening.",
                    "结果中已经提炼出可用于筛选的岗位关键词。"
            ));
        }
        if (isMeaningful(jdAnalysis.seniority())) {
            observedSignals++;
        } else {
            missingEvidence.add(text(
                    locale,
                    "The JD does not make seniority explicit.",
                    "JD 没有清楚说明岗位级别。"
            ));
        }
        if (!hasItems(jdAnalysis.mustHaveRequirements()) || jdAnalysis.mustHaveRequirements().size() < 2) {
            missingEvidence.add(text(
                    locale,
                    "The JD does not provide enough explicit must-haves for a high-confidence screen.",
                    "JD 中明确列出的核心要求偏少，暂时不支持高置信度判断。"
            ));
        }
        if (!isMeaningful(request.targetRole()) && !isMeaningful(jdAnalysis.roleTitle())) {
            inferredSignals++;
            inferenceNotes.add(text(
                    locale,
                    "The role title had to be inferred from weak JD signals.",
                    "岗位名称主要来自较弱的 JD 信号推断。"
            ));
        }

        boolean fallbackUsed = inferredSignals > observedSignals && observedSignals < 2;
        return new JdAnalysisResult(
                jdAnalysis.roleTitle(),
                jdAnalysis.seniority(),
                jdAnalysis.summary(),
                jdAnalysis.mustHaveRequirements(),
                jdAnalysis.niceToHaveRequirements(),
                jdAnalysis.keywords(),
                jdAnalysis.interviewFocusAreas(),
                buildStageConfidence(observedSignals, inferredSignals, missingEvidence.size(), fallbackUsed,
                        strongestEvidence, missingEvidence, inferenceNotes)
        );
    }

    public CandidateAnalysisResult enrichCandidateAnalysis(CareerWorkflowRequest request,
                                                           CandidateAnalysisResult candidateAnalysis) {
        SupportedLocale locale = SupportedLocale.from(request.locale());
        List<String> strongestEvidence = new ArrayList<>();
        List<String> missingEvidence = new ArrayList<>();
        List<String> inferenceNotes = new ArrayList<>();

        int observedSignals = 0;
        int inferredSignals = 0;

        if (hasItems(candidateAnalysis.evidence())) {
            observedSignals += Math.min(candidateAnalysis.evidence().size(), 3);
            strongestEvidence.add(text(
                    locale,
                    "The profile contains %s concrete evidence lines.".formatted(candidateAnalysis.evidence().size()),
                    "候选人材料中包含 %s 条可引用的具体证据。".formatted(candidateAnalysis.evidence().size())
            ));
        }
        if (hasItems(candidateAnalysis.strengths())) {
            observedSignals += Math.min(candidateAnalysis.strengths().size(), 2);
            strongestEvidence.add(text(
                    locale,
                    "Role-relevant strengths are explicitly visible in the source profile.",
                    "候选人材料里已经能看到与岗位相关的优势信号。"
            ));
        }
        if (request.candidateProfile() == null || request.candidateProfile().trim().length() < 280) {
            missingEvidence.add(text(
                    locale,
                    "The candidate profile is short, so ownership depth and impact scope remain partially uncertain.",
                    "候选人信息偏短，职责深度和影响范围仍有不确定性。"
            ));
        }
        if (hasItems(candidateAnalysis.missingSignals())) {
            missingEvidence.add(text(
                    locale,
                    "Several hiring signals still require stronger direct proof.",
                    "仍有几项招聘信号缺少更直接的证据。"
            ));
        }
        if (!hasItems(candidateAnalysis.evidence()) && hasItems(candidateAnalysis.likelyFitAreas())) {
            inferredSignals++;
            inferenceNotes.add(text(
                    locale,
                    "Likely fit areas are partly inferred from role adjacency rather than direct impact evidence.",
                    "“可能适合的方向”有一部分来自岗位相邻性推断，而不是直接成果证据。"
            ));
        }

        boolean fallbackUsed = request.candidateProfile() == null
                || request.candidateProfile().trim().length() < 180
                || (!hasItems(candidateAnalysis.evidence()) && inferredSignals > 0);

        return new CandidateAnalysisResult(
                candidateAnalysis.candidateHeadline(),
                candidateAnalysis.summary(),
                candidateAnalysis.strengths(),
                candidateAnalysis.evidence(),
                candidateAnalysis.missingSignals(),
                candidateAnalysis.likelyFitAreas(),
                buildStageConfidence(observedSignals, inferredSignals, missingEvidence.size(), fallbackUsed,
                        strongestEvidence, missingEvidence, inferenceNotes)
        );
    }

    public GapAnalysisResult enrichGapAnalysis(CareerWorkflowRequest request,
                                               GapAnalysisResult gapAnalysis,
                                               JdAnalysisResult jdAnalysis,
                                               CandidateAnalysisResult candidateAnalysis) {
        SupportedLocale locale = SupportedLocale.from(request.locale());
        List<GapItem> sortedGaps = enrichAndSortGaps(gapAnalysis.priorityGaps(), request.normalizedFocusAreas(), locale);
        List<String> strongestEvidence = new ArrayList<>();
        List<String> missingEvidence = new ArrayList<>();
        List<String> inferenceNotes = new ArrayList<>();

        int observedSignals = 0;
        int inferredSignals = 0;

        if (hasItems(gapAnalysis.matchingStrengths())) {
            observedSignals += Math.min(gapAnalysis.matchingStrengths().size(), 2);
            strongestEvidence.add(text(
                    locale,
                    "The match narrative is supported by explicit aligned strengths.",
                    "匹配判断背后有明确的对齐优势作为支持。"
            ));
        }
        if (hasItems(sortedGaps)) {
            observedSignals += Math.min(sortedGaps.size(), 2);
            strongestEvidence.add(text(
                    locale,
                    "The highest-risk gaps were ranked down to the few issues most likely to change hiring confidence.",
                    "最高风险差距已经被收敛到最可能影响录用信心的少数几项。"
            ));
        }
        boolean upstreamWeak = isLow(jdAnalysis.confidence()) || isLow(candidateAnalysis.confidence());
        if (upstreamWeak) {
            missingEvidence.add(text(
                    locale,
                    "Upstream JD or candidate evidence is weak, so the gap ranking should be treated as directional rather than final.",
                    "上游 JD 或候选人证据偏弱，因此差距排序更适合被当作方向性判断，而不是最终结论。"
            ));
        }
        if (!hasItems(sortedGaps) && !hasItems(gapAnalysis.matchingStrengths())) {
            inferredSignals++;
            inferenceNotes.add(text(
                    locale,
                    "The gap output is relying on limited structured evidence.",
                    "当前差距分析主要依赖有限的结构化证据。"
            ));
        }

        boolean fallbackUsed = upstreamWeak || (!hasItems(sortedGaps) && inferredSignals > 0);
        return new GapAnalysisResult(
                gapAnalysis.overallAssessment(),
                gapAnalysis.matchNarrative(),
                gapAnalysis.matchingStrengths(),
                sortedGaps,
                gapAnalysis.positioningAdvice(),
                buildStageConfidence(observedSignals, inferredSignals, missingEvidence.size(), fallbackUsed,
                        strongestEvidence, missingEvidence, inferenceNotes)
        );
    }

    public InterviewPrepResult enrichInterviewPrep(CareerWorkflowRequest request,
                                                   InterviewPrepResult interviewPrep,
                                                   GapAnalysisResult gapAnalysis) {
        SupportedLocale locale = SupportedLocale.from(request.locale());
        List<String> strongestEvidence = new ArrayList<>();
        List<String> missingEvidence = new ArrayList<>();
        List<String> inferenceNotes = new ArrayList<>();

        int observedSignals = 0;
        int inferredSignals = 0;

        if (hasItems(interviewPrep.prepPlan())) {
            observedSignals += Math.min(interviewPrep.prepPlan().size(), 2);
            strongestEvidence.add(text(
                    locale,
                    "The prep output contains a concrete, time-bounded plan.",
                    "准备输出里包含明确且有时间边界的计划。"
            ));
        }
        if (hasItems(interviewPrep.technicalFocusAreas()) || hasItems(interviewPrep.behavioralQuestionPrompts())) {
            observedSignals += 2;
            strongestEvidence.add(text(
                    locale,
                    "The prep is tied to specific interview topics instead of generic encouragement.",
                    "准备建议已经锚定到具体面试主题，而不是泛泛鼓励。"
            ));
        }
        if (hasItems(gapAnalysis.priorityGaps()) && !hasItems(interviewPrep.prepPlan())) {
            missingEvidence.add(text(
                    locale,
                    "The workflow found gaps, but the prep plan is still too light for a high-confidence interview sprint.",
                    "系统已经识别出差距，但准备计划仍偏轻，不足以支撑高置信度冲刺。"
            ));
        }
        if (!hasItems(interviewPrep.companyResearchSuggestions()) && request.includeCompanyResearch()) {
            inferredSignals++;
            inferenceNotes.add(text(
                    locale,
                    "Company research was requested, but the returned guidance remained general.",
                    "用户要求了公司研究，但当前返回仍偏通用建议。"
            ));
        }

        boolean fallbackUsed = request.includeCompanyResearch() && !hasItems(interviewPrep.companyResearchSuggestions());
        return new InterviewPrepResult(
                interviewPrep.prepSummary(),
                interviewPrep.technicalFocusAreas(),
                interviewPrep.behavioralQuestionPrompts(),
                interviewPrep.resumeDefensePoints(),
                interviewPrep.prepPlan(),
                interviewPrep.companyResearchSuggestions(),
                buildStageConfidence(observedSignals, inferredSignals, missingEvidence.size(), fallbackUsed,
                        strongestEvidence, missingEvidence, inferenceNotes)
        );
    }

    public ConfidenceSummary buildConfidenceSummary(CareerWorkflowRequest request,
                                                    JdAnalysisResult jdAnalysis,
                                                    CandidateAnalysisResult candidateAnalysis,
                                                    GapAnalysisResult gapAnalysis,
                                                    InterviewPrepResult interviewPrep) {
        SupportedLocale locale = SupportedLocale.from(request.locale());
        List<String> strongestEvidence = new ArrayList<>();
        List<String> missingEvidence = new ArrayList<>();
        List<String> inferenceNotes = new ArrayList<>();
        List<String> blockingUncertainties = new ArrayList<>();
        List<String> influentialGaps = gapAnalysis.priorityGaps() == null
                ? List.of()
                : gapAnalysis.priorityGaps().stream()
                .map(GapItem::requirement)
                .filter(this::isMeaningful)
                .limit(3)
                .toList();

        strongestEvidence.addAll(valuesOrEmpty(jdAnalysis.confidence() == null ? null : jdAnalysis.confidence().strongestEvidence()));
        strongestEvidence.addAll(valuesOrEmpty(candidateAnalysis.confidence() == null ? null : candidateAnalysis.confidence().strongestEvidence()));
        strongestEvidence.addAll(valuesOrEmpty(gapAnalysis.confidence() == null ? null : gapAnalysis.confidence().strongestEvidence()));
        strongestEvidence.addAll(valuesOrEmpty(interviewPrep.confidence() == null ? null : interviewPrep.confidence().strongestEvidence()));

        missingEvidence.addAll(valuesOrEmpty(jdAnalysis.confidence() == null ? null : jdAnalysis.confidence().missingEvidence()));
        missingEvidence.addAll(valuesOrEmpty(candidateAnalysis.confidence() == null ? null : candidateAnalysis.confidence().missingEvidence()));
        missingEvidence.addAll(valuesOrEmpty(gapAnalysis.confidence() == null ? null : gapAnalysis.confidence().missingEvidence()));
        missingEvidence.addAll(valuesOrEmpty(interviewPrep.confidence() == null ? null : interviewPrep.confidence().missingEvidence()));

        inferenceNotes.addAll(valuesOrEmpty(jdAnalysis.confidence() == null ? null : jdAnalysis.confidence().inferenceNotes()));
        inferenceNotes.addAll(valuesOrEmpty(candidateAnalysis.confidence() == null ? null : candidateAnalysis.confidence().inferenceNotes()));
        inferenceNotes.addAll(valuesOrEmpty(gapAnalysis.confidence() == null ? null : gapAnalysis.confidence().inferenceNotes()));
        inferenceNotes.addAll(valuesOrEmpty(interviewPrep.confidence() == null ? null : interviewPrep.confidence().inferenceNotes()));

        if (isLow(jdAnalysis.confidence())) {
            blockingUncertainties.add(text(
                    locale,
                    "The JD signal is too weak to lock the screen with high confidence.",
                    "JD 信号偏弱，还不足以支撑高置信度筛选。"
            ));
        }
        if (isLow(candidateAnalysis.confidence())) {
            blockingUncertainties.add(text(
                    locale,
                    "The candidate evidence does not yet prove scope, ownership, or shipped impact strongly enough.",
                    "候选人证据还不足以强有力证明 scope、ownership 和已落地成果。"
            ));
        }
        if (!hasItems(influentialGaps)) {
            inferenceNotes.add(text(
                    locale,
                    "No single blocking gap dominated the current analysis.",
                    "当前分析里没有单一的阻塞性差距主导结论。"
            ));
        } else {
            blockingUncertainties.add(text(
                    locale,
                    "The final hiring call is still most sensitive to: %s".formatted(String.join(", ", influentialGaps)),
                    "最终录用判断最容易被这些差距改变：%s".formatted(String.join("、", influentialGaps))
            ));
        }

        int lowCount = lowConfidenceCount(jdAnalysis, candidateAnalysis, gapAnalysis, interviewPrep);
        String overallConfidence = lowCount >= 2 ? "LOW" : lowCount == 1 ? "MEDIUM" : "HIGH";
        String confidenceRationale = buildConfidenceRationale(locale, overallConfidence, blockingUncertainties);

        return new ConfidenceSummary(
                overallConfidence,
                distinct(strongestEvidence).stream().limit(4).toList(),
                distinct(missingEvidence).stream().limit(4).toList(),
                distinct(inferenceNotes).stream().limit(4).toList(),
                influentialGaps,
                distinct(blockingUncertainties).stream().limit(3).toList(),
                confidenceRationale
        );
    }

    public List<EvidenceItem> buildDecisionDrivers(CareerWorkflowRequest request,
                                                   JdAnalysisResult jdAnalysis,
                                                   CandidateAnalysisResult candidateAnalysis,
                                                   GapAnalysisResult gapAnalysis,
                                                   InterviewPrepResult interviewPrep) {
        SupportedLocale locale = SupportedLocale.from(request.locale());
        List<EvidenceItem> drivers = new ArrayList<>();

        if (hasItems(jdAnalysis.mustHaveRequirements())) {
            drivers.add(new EvidenceItem(
                    text(
                            locale,
                            "The JD explicitly anchors the screen around: %s.".formatted(joinInline(jdAnalysis.mustHaveRequirements(), 2)),
                            "JD 明确把筛选重点锚定在：%s。".formatted(joinInline(jdAnalysis.mustHaveRequirements(), 2))
                    ),
                    EvidenceSourceType.JD_TEXT
            ));
        }

        if (hasItems(candidateAnalysis.evidence())) {
            drivers.add(new EvidenceItem(
                    text(
                            locale,
                            "The strongest candidate proof currently comes from: %s.".formatted(firstMeaningful(candidateAnalysis.evidence())),
                            "当前最强的候选人证据来自：%s。".formatted(firstMeaningful(candidateAnalysis.evidence()))
                    ),
                    EvidenceSourceType.CANDIDATE_TEXT
            ));
        } else if (hasItems(candidateAnalysis.strengths())) {
            drivers.add(new EvidenceItem(
                    text(
                            locale,
                            "The candidate fit still leans on adjacent strengths such as: %s.".formatted(firstMeaningful(candidateAnalysis.strengths())),
                            "候选人的匹配判断目前仍较多依赖相邻优势，例如：%s。".formatted(firstMeaningful(candidateAnalysis.strengths()))
                    ),
                    EvidenceSourceType.MODEL_INFERENCE
            ));
        }

        if (hasItems(gapAnalysis.priorityGaps())) {
            GapItem topGap = gapAnalysis.priorityGaps().get(0);
            drivers.add(new EvidenceItem(
                    text(
                            locale,
                            "The top hiring-risk gap is %s, which is why the final fit call is not higher.".formatted(firstMeaningful(topGap.requirement())),
                            "当前最影响录用判断的差距是 %s，因此最终 fit 结论没有更高。".formatted(firstMeaningful(topGap.requirement()))
                    ),
                    EvidenceSourceType.MODEL_INFERENCE
            ));
        }

        if (hasItems(interviewPrep.prepPlan())) {
            drivers.add(new EvidenceItem(
                    text(
                            locale,
                            "The preparation output is actionable because it already includes a time-bounded prep plan.",
                            "这份准备结果是可执行的，因为它已经包含带时间边界的准备计划。"
                    ),
                    EvidenceSourceType.MODEL_INFERENCE
            ));
        }

        return drivers.stream()
                .filter(item -> item != null && isMeaningful(item.statement()))
                .limit(4)
                .toList();
    }

    public List<String> buildClarificationQuestions(CareerWorkflowRequest request,
                                                    JdAnalysisResult jdAnalysis,
                                                    CandidateAnalysisResult candidateAnalysis,
                                                    GapAnalysisResult gapAnalysis) {
        SupportedLocale locale = SupportedLocale.from(request.locale());
        List<String> questions = new ArrayList<>();

        if (request.candidateProfile() == null || request.candidateProfile().trim().length() < 280) {
            questions.add(text(
                    locale,
                    "What was the largest scope you personally owned, and how many users, teams, or revenue lines did it affect?",
                    "你亲自负责过的最大 scope 是什么，影响了多少用户、团队或业务指标？"
            ));
        }

        if (hasItems(candidateAnalysis.missingSignals())) {
            String missingSignal = firstMeaningful(candidateAnalysis.missingSignals());
            questions.add(text(
                    locale,
                    "What concrete project, launch, or metric best proves this missing signal: %s?".formatted(missingSignal),
                    "哪段具体项目、上线结果或指标最能补强这个缺失信号：%s？".formatted(missingSignal)
            ));
        }

        if (hasItems(gapAnalysis.priorityGaps())) {
            String topGap = firstMeaningful(gapAnalysis.priorityGaps().get(0).requirement());
            questions.add(text(
                    locale,
                    "What is your strongest real example that could reduce the hiring risk around %s?".formatted(topGap),
                    "你最能降低 %s 这项录用风险的真实经历是什么？".formatted(topGap)
            ));
        }

        if (questions.size() < 3 && (!isMeaningful(jdAnalysis.seniority()) || sizeOf(jdAnalysis.mustHaveRequirements()) < 2)) {
            questions.add(text(
                    locale,
                    "Which responsibilities or must-haves are truly non-negotiable in this role, and which are only preferred?",
                    "这个岗位里哪些职责或要求是真正不能缺的，哪些只是加分项？"
            ));
        }

        return distinct(questions).stream().limit(3).toList();
    }

    private StageConfidence buildStageConfidence(int observedSignals,
                                                 int inferredSignals,
                                                 int missingSignals,
                                                 boolean fallbackUsed,
                                                 List<String> strongestEvidence,
                                                 List<String> missingEvidence,
                                                 List<String> inferenceNotes) {
        return new StageConfidence(
                confidenceLevel(observedSignals, inferredSignals, missingSignals, fallbackUsed),
                distinct(strongestEvidence),
                distinct(missingEvidence),
                distinct(inferenceNotes),
                observedSignals,
                inferredSignals,
                missingSignals,
                fallbackUsed
        );
    }

    private List<GapItem> enrichAndSortGaps(List<GapItem> gapItems,
                                            List<String> focusAreas,
                                            SupportedLocale locale) {
        if (!hasItems(gapItems)) {
            return List.of();
        }

        return gapItems.stream()
                .map(gap -> new GapItem(
                        gap.requirement(),
                        gap.candidateEvidence(),
                        gap.gapLevel(),
                        firstNonBlank(gap.hiringImpact(), normalizedImpact(gap.gapLevel())),
                        firstNonBlank(gap.interviewRisk(), interviewRisk(gap, focusAreas)),
                        firstNonBlank(gap.evidenceStrength(), evidenceStrength(gap)),
                        gap.recommendation(),
                        rankingReason(gap, focusAreas, locale)
                ))
                .sorted(Comparator
                        .comparingInt((GapItem gap) -> gapPriorityScore(gap, focusAreas))
                        .reversed()
                        .thenComparing(gap -> gap.requirement() == null ? "" : gap.requirement()))
                .limit(3)
                .toList();
    }

    private String rankingReason(GapItem gap, List<String> focusAreas, SupportedLocale locale) {
        boolean focusAligned = focusAreas.stream().anyMatch(focus -> overlap(gap.requirement(), focus));
        String impactLabel = gapImpactLabel(gap.gapLevel());
        boolean weakEvidence = !isMeaningful(gap.candidateEvidence()) || containsWeaknessHint(gap.candidateEvidence());

        if (focusAligned && weakEvidence) {
            return text(
                    locale,
                    "Ranked high because the hiring impact is %s, the current proof is weak, and it overlaps with the interview focus.".formatted(impactLabel),
                    "排在前面是因为它的录用影响度为 %s、当前证据偏弱，而且和当前面试重点直接重合。".formatted(impactLabel)
            );
        }
        if (weakEvidence) {
            return text(
                    locale,
                    "Ranked high because the hiring impact is %s and the candidate evidence is still weak or indirect.".formatted(impactLabel),
                    "排在前面是因为它的录用影响度为 %s，且候选人证据仍然偏弱或不够直接。".formatted(impactLabel)
            );
        }
        if (focusAligned) {
            return text(
                    locale,
                    "Ranked up because it matters in interviews now, even if the evidence gap is not the worst blocker.",
                    "它被提前是因为当前面试很可能会考到，即使它未必是最严重的证据缺口。"
            );
        }

        return text(
                locale,
                "Still important, but secondary to the highest hiring-impact and weakest-evidence blockers.",
                "它仍然重要，但优先级低于那些录用影响更大、证据更弱的阻塞项。"
        );
    }

    private int gapPriorityScore(GapItem gap, List<String> focusAreas) {
        int hiringImpact = switch ((gap.gapLevel() == null ? "" : gap.gapLevel()).toUpperCase(Locale.ROOT)) {
            case "HIGH" -> 30;
            case "MEDIUM" -> 20;
            default -> 10;
        };

        int evidenceWeakness = !isMeaningful(gap.candidateEvidence())
                ? 8
                : containsWeaknessHint(gap.candidateEvidence()) ? 5 : 1;

        int interviewRisk = focusAreas.stream().anyMatch(focus -> overlap(gap.requirement(), focus)) ? 5 : 2;

        return hiringImpact + evidenceWeakness + interviewRisk;
    }

    private String firstNonBlank(String primary, String fallback) {
        return isMeaningful(primary) ? primary.trim() : fallback;
    }

    private boolean containsWeaknessHint(String value) {
        if (!isMeaningful(value)) {
            return true;
        }

        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.contains("limited")
                || normalized.contains("little")
                || normalized.contains("not")
                || normalized.contains("missing")
                || normalized.contains("lack")
                || normalized.contains("缺")
                || normalized.contains("弱")
                || normalized.contains("没有");
    }

    private String buildConfidenceRationale(SupportedLocale locale,
                                            String overallConfidence,
                                            List<String> blockingUncertainties) {
        if ("HIGH".equalsIgnoreCase(overallConfidence)) {
            return text(
                    locale,
                    "Most critical judgments are supported by direct evidence, so the fit call is stable unless new contradictory information appears.",
                    "大部分关键判断都有直接证据支撑，因此除非出现新的反向信息，否则当前 fit 结论相对稳定。"
            );
        }
        if ("MEDIUM".equalsIgnoreCase(overallConfidence)) {
            return text(
                    locale,
                    "The fit call is directionally useful, but a few missing signals could still move the final decision.",
                    "当前 fit 结论具有方向性参考价值，但仍有少数缺失信号可能改变最终判断。"
            );
        }
        return text(
                locale,
                blockingUncertainties.isEmpty()
                        ? "The analysis relies on partial evidence, so treat this result as a hypothesis and collect more proof before acting on it."
                        : "The result should be treated as provisional because the current evidence leaves key hiring questions unresolved.",
                blockingUncertainties.isEmpty()
                        ? "当前分析更多依赖部分证据，因此更适合被当作假设，需要先补更多证明材料再用于决策。"
                        : "这份结果更适合作为暂时性判断，因为当前证据还无法解决几个关键的录用问题。"
        );
    }

    private String gapImpactLabel(String gapLevel) {
        return switch ((gapLevel == null ? "" : gapLevel).toUpperCase(Locale.ROOT)) {
            case "HIGH" -> "high";
            case "MEDIUM" -> "medium";
            default -> "moderate";
        };
    }

    private String normalizedImpact(String gapLevel) {
        return switch ((gapLevel == null ? "" : gapLevel).toUpperCase(Locale.ROOT)) {
            case "HIGH" -> "HIGH";
            case "MEDIUM" -> "MEDIUM";
            default -> "LOW";
        };
    }

    private String interviewRisk(GapItem gap, List<String> focusAreas) {
        return focusAreas.stream().anyMatch(focus -> overlap(gap.requirement(), focus))
                ? "HIGH"
                : "HIGH".equalsIgnoreCase(gap.gapLevel()) ? "MEDIUM" : "LOW";
    }

    private String evidenceStrength(GapItem gap) {
        if (!isMeaningful(gap.candidateEvidence()) || containsWeaknessHint(gap.candidateEvidence())) {
            return "LOW";
        }
        return "HIGH".equalsIgnoreCase(gap.gapLevel()) ? "MEDIUM" : "HIGH";
    }

    private String confidenceLevel(int observedSignals,
                                   int inferredSignals,
                                   int missingSignals,
                                   boolean fallbackUsed) {
        int score = (observedSignals * 2) + inferredSignals - (missingSignals * 2) - (fallbackUsed ? 2 : 0);
        if (observedSignals >= 3 && missingSignals == 0 && !fallbackUsed && score >= 5) {
            return "HIGH";
        }
        if (observedSignals >= 1 && score >= 1) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private boolean overlap(String left, String right) {
        if (!isMeaningful(left) || !isMeaningful(right)) {
            return false;
        }

        String normalizedLeft = left.toLowerCase(Locale.ROOT);
        String normalizedRight = right.toLowerCase(Locale.ROOT);
        return normalizedLeft.contains(normalizedRight) || normalizedRight.contains(normalizedLeft);
    }

    private int lowConfidenceCount(JdAnalysisResult jdAnalysis,
                                   CandidateAnalysisResult candidateAnalysis,
                                   GapAnalysisResult gapAnalysis,
                                   InterviewPrepResult interviewPrep) {
        int count = 0;
        if (isLow(jdAnalysis.confidence())) {
            count++;
        }
        if (isLow(candidateAnalysis.confidence())) {
            count++;
        }
        if (isLow(gapAnalysis.confidence())) {
            count++;
        }
        if (isLow(interviewPrep.confidence())) {
            count++;
        }
        return count;
    }

    private boolean isLow(StageConfidence confidence) {
        return confidence != null && "LOW".equalsIgnoreCase(confidence.evidenceStrength());
    }

    private int sizeOf(List<?> values) {
        return values == null ? 0 : values.size();
    }

    private boolean hasItems(List<?> values) {
        return values != null && !values.isEmpty();
    }

    private List<String> valuesOrEmpty(List<String> values) {
        return values == null ? List.of() : values;
    }

    private boolean isMeaningful(String value) {
        return value != null
                && !value.isBlank()
                && !Set.of("none", "not available", "unknown", "无", "暂无", "未知").contains(value.trim().toLowerCase(Locale.ROOT));
    }

    private List<String> distinct(List<String> values) {
        LinkedHashSet<String> items = new LinkedHashSet<>();
        for (String value : values) {
            if (isMeaningful(value)) {
                items.add(value.trim());
            }
        }
        return List.copyOf(items);
    }

    private String firstMeaningful(List<String> values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (isMeaningful(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String firstMeaningful(String value) {
        return isMeaningful(value) ? value.trim() : "";
    }

    private String joinInline(List<String> values, int limit) {
        if (!hasItems(values)) {
            return "";
        }

        return values.stream()
                .filter(this::isMeaningful)
                .limit(limit)
                .map(String::trim)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String text(SupportedLocale locale, String english, String chinese) {
        return locale.isChinese() ? chinese : english;
    }
}
