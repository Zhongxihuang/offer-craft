package com.workspace.codeforgeai.career.demo;

import com.workspace.codeforgeai.career.gap.GapAnalysisAiService;
import com.workspace.codeforgeai.career.gap.GapAnalysisResult;
import com.workspace.codeforgeai.career.gap.GapItem;
import com.workspace.codeforgeai.common.i18n.SupportedLocale;
import com.workspace.codeforgeai.demo.DemoCareerHeuristics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@ConditionalOnProperty(prefix = "career.ai", name = "mode", havingValue = "demo", matchIfMissing = true)
public class DemoGapAnalysisAiService implements GapAnalysisAiService {

    @Override
    public GapAnalysisResult analyze(String workflowInput) {
        SupportedLocale locale = DemoCareerHeuristics.requestedLocale(workflowInput);
        List<String> requirements = DemoCareerHeuristics.extractBullets(workflowInput, "Must-have requirements:");
        List<String> strengths = DemoCareerHeuristics.extractBullets(workflowInput, "Strengths:");
        List<String> missingSignals = DemoCareerHeuristics.extractBullets(workflowInput, "Missing signals:");
        List<String> evidence = DemoCareerHeuristics.extractBullets(workflowInput, "Evidence:");
        List<String> focusAreas = DemoCareerHeuristics.extractBullets(workflowInput, "Focus areas:");

        List<String> matchingStrengths = new ArrayList<>();
        List<GapItem> priorityGaps = new ArrayList<>();

        for (String requirement : requirements) {
            String category = categoryOf(requirement);
            boolean covered = strengths.stream().anyMatch(item -> categoryOf(item).equals(category))
                    || evidence.stream().anyMatch(item -> categoryOf(item).equals(category));

            if (covered) {
                matchingStrengths.add(DemoCareerHeuristics.text(locale,
                        "Visible evidence already supports: " + requirement,
                        "已有可见证据支持该要求：" + requirement));
                continue;
            }

            String evidenceNarrative = missingSignals.stream()
                    .filter(item -> categoryOf(item).equals(category))
                    .findFirst()
                    .orElse(DemoCareerHeuristics.text(locale,
                            "The resume does not yet provide strong visible proof for this requirement.",
                            "简历里还没有为这项要求提供足够强的可见证据。"));

            priorityGaps.add(new GapItem(
                    requirement,
                    evidenceNarrative,
                    gapLevel(category, missingSignals),
                    null,
                    null,
                    null,
                    recommendationFor(category, focusAreas, locale),
                    null
            ));
        }

        if (priorityGaps.isEmpty() && requirements.isEmpty()) {
            priorityGaps.add(new GapItem(
                    DemoCareerHeuristics.text(locale, "Clarify the job's must-have requirements", "先澄清岗位的核心必备要求"),
                    DemoCareerHeuristics.text(locale, "The workflow received limited JD structure.", "当前 JD 结构化信息较少。"),
                    "MEDIUM",
                    null,
                    null,
                    null,
                    DemoCareerHeuristics.text(locale,
                            "Confirm the top three interview priorities before tailoring the application.",
                            "先确认最重要的三项面试考察点，再开始定制申请材料。"),
                    null
            ));
        }

        String assessment = overallAssessment(requirements.size(), matchingStrengths.size(), priorityGaps);
        String narrative = switch (assessment) {
            case "STRONG_MATCH" -> DemoCareerHeuristics.text(locale,
                    "The candidate looks ready to compete now, with only lightweight narrative sharpening needed.",
                    "候选人已经具备较强竞争力，目前主要需要做轻量的表达优化。");
            case "COMPETITIVE_WITH_GAPS" -> DemoCareerHeuristics.text(locale,
                    "The candidate is directionally credible, but a few role-specific gaps need explicit framing and prep.",
                    "候选人的方向整体可信，但仍有少数岗位特定差距，需要提前解释和准备。");
            case "STRETCH" -> DemoCareerHeuristics.text(locale,
                    "The role is plausible as a stretch play, but the current profile leaves meaningful risk on core requirements.",
                    "这个岗位可以作为冲刺项，但当前背景在核心要求上仍有明显风险。");
            default -> DemoCareerHeuristics.text(locale,
                    "The current evidence does not yet support a strong fit, so the candidate should reposition or build proof first.",
                    "现有证据还不足以支撑强匹配，建议先调整定位或补充关键证明。");
        };

        List<String> positioningAdvice = new ArrayList<>();
        if (assessment.equals("STRONG_MATCH") || assessment.equals("COMPETITIVE_WITH_GAPS")) {
            positioningAdvice.add(DemoCareerHeuristics.text(locale,
                    "Lead with the strongest product execution stories and directly map them to the JD's top must-haves.",
                    "先用最强的产品落地案例开场，并直接映射到 JD 的核心要求。"));
        }
        if (!priorityGaps.isEmpty()) {
            positioningAdvice.add(DemoCareerHeuristics.text(locale,
                    "Address the riskiest gaps proactively in resume bullets and interview narratives.",
                    "在简历 bullet 和面试叙事里主动回应最高风险差距。"));
        }
        if (focusAreas.stream().anyMatch(item -> {
            String lower = item.toLowerCase(Locale.ROOT);
            return lower.contains("governance") || lower.contains("治理");
        })) {
            positioningAdvice.add(DemoCareerHeuristics.text(locale,
                    "Prepare a concrete AI governance point of view, even if the underlying experience is adjacent rather than direct.",
                    "即使相关经验更多是邻近型的，也要提前准备一套清晰的 AI 治理观点。"));
        }

        return new GapAnalysisResult(
                assessment,
                narrative,
                DemoCareerHeuristics.distinct(matchingStrengths),
                priorityGaps.stream().limit(3).toList(),
                DemoCareerHeuristics.distinct(positioningAdvice),
                null
        );
    }

    private String overallAssessment(int requirementCount, int matchedCount, List<GapItem> gaps) {
        if (requirementCount == 0) {
            return "COMPETITIVE_WITH_GAPS";
        }
        if (matchedCount >= Math.max(3, requirementCount - 1) && gaps.stream().noneMatch(gap -> "HIGH".equals(gap.gapLevel()))) {
            return "STRONG_MATCH";
        }
        if (matchedCount >= Math.max(2, requirementCount / 2)) {
            return "COMPETITIVE_WITH_GAPS";
        }
        if (matchedCount >= 1) {
            return "STRETCH";
        }
        return "LOW_MATCH";
    }

    private String gapLevel(String category, List<String> missingSignals) {
        if (List.of("governance", "technical", "ai").contains(category)) {
            return "HIGH";
        }
        if (missingSignals.stream().anyMatch(item -> categoryOf(item).equals(category))) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String recommendationFor(String category, List<String> focusAreas, SupportedLocale locale) {
        return switch (category) {
            case "governance" -> DemoCareerHeuristics.text(locale,
                    "Add a concise point of view on enterprise AI governance, admin controls, and rollout risk.",
                    "补一套对企业级 AI 治理、管理控制和发布风险的简明观点。");
            case "technical" -> DemoCareerHeuristics.text(locale,
                    "Prepare a simple technical narrative covering evaluation, retrieval quality, latency, and reliability tradeoffs.",
                    "准备一套能讲清评估、检索质量、延迟和可靠性权衡的技术叙事。");
            case "ai" -> DemoCareerHeuristics.text(locale,
                    "Show how you prioritize AI use cases, guardrails, and measurable business outcomes.",
                    "准备说明你如何判断 AI 用例优先级、设置护栏，并定义可量化业务结果。");
            case "platform" -> DemoCareerHeuristics.text(locale,
                    "Frame your experience in terms of reusable workflows, adoption levers, and platform tradeoffs.",
                    "把你的经历翻译成可复用工作流、采用率杠杆和平台取舍。");
            case "metrics" -> DemoCareerHeuristics.text(locale,
                    "Bring sharper KPI, experimentation, and iteration evidence into both the resume and interview stories.",
                    "在简历和面试案例中补强 KPI、实验和迭代优化证据。");
            default -> focusAreas.isEmpty()
                    ? DemoCareerHeuristics.text(locale,
                    "Translate adjacent experience into direct evidence for this requirement.",
                    "把邻近经验翻译成与该要求直接相关的证据。")
                    : DemoCareerHeuristics.text(locale,
                    "Turn the relevant focus areas into one resume line and one interview story for this gap.",
                    "把相关关注点转化成一条简历 bullet 和一个面试故事来覆盖这项差距。");
        };
    }

    private String categoryOf(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (DemoCareerHeuristics.hasAny(lower, "governance", "trust", "security", "compliance", "enterprise", "治理", "安全", "合规", "企业")) {
            return "governance";
        }
        if (DemoCareerHeuristics.hasAny(lower, "technical", "retrieval", "reliability", "latency", "api", "evaluation", "技术", "检索", "可靠性", "延迟", "评估")) {
            return "technical";
        }
        if (DemoCareerHeuristics.hasAny(lower, "ai", "llm", "genai", "model", "人工智能", "大模型")) {
            return "ai";
        }
        if (DemoCareerHeuristics.hasAny(lower, "platform", "workflow", "orchestration", "平台", "工作流", "编排")) {
            return "platform";
        }
        if (DemoCareerHeuristics.hasAny(lower, "metric", "kpi", "experiment", "analytics", "指标", "实验", "数据")) {
            return "metrics";
        }
        if (DemoCareerHeuristics.hasAny(lower, "cross-functional", "stakeholder", "engineering", "design", "gtm", "跨部门", "协同", "工程", "设计")) {
            return "execution";
        }
        return "general";
    }
}
