package com.workspace.codeforgeai.career.demo;

import com.workspace.codeforgeai.career.interview.InterviewPrepAiService;
import com.workspace.codeforgeai.career.interview.InterviewPrepResult;
import com.workspace.codeforgeai.common.i18n.SupportedLocale;
import com.workspace.codeforgeai.demo.DemoCareerHeuristics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@ConditionalOnProperty(prefix = "career.ai", name = "mode", havingValue = "demo", matchIfMissing = true)
public class DemoInterviewPrepAiService implements InterviewPrepAiService {

    @Override
    public InterviewPrepResult analyze(String workflowInput) {
        SupportedLocale locale = DemoCareerHeuristics.requestedLocale(workflowInput);
        String companyName = DemoCareerHeuristics.extractLineValue(workflowInput, "Company name:");
        boolean includeCompanyResearch = DemoCareerHeuristics.extractLineValue(workflowInput, "Include company research:")
                .equalsIgnoreCase("true");
        List<String> interviewFocus = DemoCareerHeuristics.extractBullets(workflowInput, "Interview focus areas:");
        List<String> strengths = DemoCareerHeuristics.extractBullets(workflowInput, "Strengths:");
        List<String> missingSignals = DemoCareerHeuristics.extractBullets(workflowInput, "Missing signals:");
        List<String> priorityGaps = DemoCareerHeuristics.extractBullets(workflowInput, "Priority gaps:");

        List<String> technicalFocusAreas = new ArrayList<>(interviewFocus);
        if (technicalFocusAreas.isEmpty()) {
            technicalFocusAreas.add(DemoCareerHeuristics.text(locale,
                    "Clarify the target architecture, evaluation loop, and KPI tradeoffs for the role.",
                    "先梳理这个岗位对应的目标架构、评估闭环和 KPI 取舍。"));
        }

        List<String> behavioralPrompts = new ArrayList<>();
        behavioralPrompts.add(DemoCareerHeuristics.text(locale,
                "Tell me about a time you aligned engineering, design, and GTM around an ambiguous product priority.",
                "请讲一个你在目标不明确的情况下，推动工程、设计和业务团队达成一致的经历。"));
        if (priorityGaps.stream().anyMatch(item -> containsAny(item, "governance", "治理"))) {
            behavioralPrompts.add(DemoCareerHeuristics.text(locale,
                    "Describe how you would introduce governance or admin controls for an enterprise AI rollout.",
                    "如果要推动企业级 AI 功能上线，你会如何设计治理机制或管理控制？"));
        }
        if (priorityGaps.stream().anyMatch(item -> containsAny(item, "technical", "技术", "评估", "检索", "可靠性"))) {
            behavioralPrompts.add(DemoCareerHeuristics.text(locale,
                    "Walk me through how you explain evaluation quality, latency, and reliability tradeoffs to executives.",
                    "如果面对管理层，你会怎样解释评估质量、延迟和可靠性的权衡？"));
        }

        List<String> resumeDefensePoints = new ArrayList<>();
        resumeDefensePoints.add(DemoCareerHeuristics.text(locale,
                "Open with the strongest product execution and metrics stories before addressing adjacent AI depth.",
                "先用最强的产品推进和数据结果案例开场，再解释 AI 深度方面的邻近经验。"));
        if (!strengths.isEmpty()) {
            resumeDefensePoints.add(DemoCareerHeuristics.text(locale,
                    "Use the clearest evidence line to prove why your background transfers to this role: " + strengths.get(0),
                    "用最明确的一条证据说明你的背景为什么能迁移到这个岗位：" + strengths.get(0)));
        }
        if (!missingSignals.isEmpty()) {
            resumeDefensePoints.add(DemoCareerHeuristics.text(locale,
                    "Acknowledge the biggest missing signal directly, then bridge it with adjacent experience and a learning plan.",
                    "直接承认最明显的缺口，再用邻近经验和补齐计划把它接上。"));
        }

        List<String> prepPlan = new ArrayList<>();
        prepPlan.add(DemoCareerHeuristics.text(locale,
                "Day 1-2: tailor the resume and intro pitch to the top must-haves and strongest evidence.",
                "第 1-2 天：按核心要求和最强证据改写简历与自我介绍。"));
        prepPlan.add(DemoCareerHeuristics.text(locale,
                "Day 3: build one STAR story for cross-functional execution and one for KPI ownership.",
                "第 3 天：各准备一个跨团队推进案例和 KPI 负责案例。"));
        prepPlan.add(DemoCareerHeuristics.text(locale,
                "Day 4: rehearse answers on AI use-case prioritization, guardrails, and measurable value.",
                "第 4 天：练习 AI 用例优先级、护栏设计和价值衡量相关回答。"));
        prepPlan.add(DemoCareerHeuristics.text(locale,
                "Day 5: practice the riskiest technical or governance gap using plain-language explanations.",
                "第 5 天：用业务语言讲清最高风险的技术或治理差距。"));
        prepPlan.add(DemoCareerHeuristics.text(locale,
                "Day 6-7: run mock interviews focused on the top two gaps and update your talking points.",
                "第 6-7 天：围绕前两项差距做模拟面试，并收敛表达。"));

        List<String> companyResearchSuggestions = new ArrayList<>();
        if (includeCompanyResearch) {
            String companyLabel = DemoCareerHeuristics.isMeaningful(companyName)
                    ? companyName
                    : DemoCareerHeuristics.text(locale, "the target company", "目标公司");
            companyResearchSuggestions.add(DemoCareerHeuristics.text(locale,
                    "Review %s's public AI product launches, admin controls, and enterprise positioning.".formatted(companyLabel),
                    "回看 %s 公开的 AI 产品发布、管理控制和企业化定位。".formatted(companyLabel)));
            companyResearchSuggestions.add(DemoCareerHeuristics.text(locale,
                    "Map the role's must-haves to %s's current product maturity, evaluation loop, and trust posture.".formatted(companyLabel),
                    "把岗位核心要求映射到 %s 当前的产品成熟度、评估闭环和信任机制。".formatted(companyLabel)));
            companyResearchSuggestions.add(DemoCareerHeuristics.text(locale,
                    "Prepare one hypothesis on how %s could improve adoption, governance, or ROI communication.".formatted(companyLabel),
                    "准备一个你对 %s 如何提升采用率、治理机制或 ROI 表达的判断。".formatted(companyLabel)));
        } else {
            companyResearchSuggestions.add(DemoCareerHeuristics.text(locale,
                    "If company research matters later, review recent launches, trust features, and GTM messaging before interviews.",
                    "如果后续需要补公司研究，可在面试前补看近期发布、信任能力和 GTM 表达。"));
        }

        String prepSummary = DemoCareerHeuristics.text(locale,
                "The prep plan should focus on converting adjacent experience into role-specific proof, especially around %s."
                        .formatted(priorityGaps.isEmpty() ? "the highest-risk must-haves" : priorityGaps.get(0)),
                "这轮准备的重点，是把邻近经验转化成岗位所需的直接证据，尤其要围绕 %s。"
                        .formatted(priorityGaps.isEmpty() ? "最高风险的核心要求" : priorityGaps.get(0)));

        return new InterviewPrepResult(
                prepSummary,
                DemoCareerHeuristics.distinct(technicalFocusAreas).stream().limit(5).toList(),
                DemoCareerHeuristics.distinct(behavioralPrompts).stream().limit(4).toList(),
                DemoCareerHeuristics.distinct(resumeDefensePoints).stream().limit(4).toList(),
                prepPlan,
                DemoCareerHeuristics.distinct(companyResearchSuggestions).stream().limit(3).toList(),
                null
        );
    }

    private boolean containsAny(String value, String... keywords) {
        String lower = value == null ? "" : value.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
