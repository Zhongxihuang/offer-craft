package com.workspace.codeforgeai.career.demo;

import com.workspace.codeforgeai.career.candidate.CandidateAnalysisAiService;
import com.workspace.codeforgeai.career.candidate.CandidateAnalysisResult;
import com.workspace.codeforgeai.common.i18n.SupportedLocale;
import com.workspace.codeforgeai.demo.DemoCareerHeuristics;
import com.workspace.codeforgeai.demo.DemoCareerHeuristics.SignalProfile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(prefix = "career.ai", name = "mode", havingValue = "demo", matchIfMissing = true)
public class DemoCandidateAnalysisAiService implements CandidateAnalysisAiService {

    @Override
    public CandidateAnalysisResult analyze(String workflowInput) {
        SupportedLocale locale = DemoCareerHeuristics.requestedLocale(workflowInput);
        String targetRole = DemoCareerHeuristics.extractLineValue(workflowInput, "Target role:");
        String resumeText = DemoCareerHeuristics.extractSection(workflowInput, "Candidate profile or resume:");
        SignalProfile signals = DemoCareerHeuristics.analyzeSignals(resumeText);

        List<String> strengths = new ArrayList<>();
        if (signals.metrics()) {
            strengths.add(DemoCareerHeuristics.text(locale,
                    "Strong analytics, experimentation, and KPI ownership signal",
                    "具备较强的数据分析、实验设计和 KPI 负责意识"));
        }
        if (signals.crossFunctional()) {
            strengths.add(DemoCareerHeuristics.text(locale,
                    "Cross-functional execution across engineering, design, and business partners",
                    "有跨工程、设计和业务团队协同推进的执行经验"));
        }
        if (signals.aiStrategy()) {
            strengths.add(DemoCareerHeuristics.text(locale,
                    "Visible AI or GenAI product exposure",
                    "具备可见的 AI / GenAI 产品实践经历"));
        }
        if (signals.platform()) {
            strengths.add(DemoCareerHeuristics.text(locale,
                    "Evidence of workflow, platform, or API-oriented product thinking",
                    "展现出工作流、平台化或 API 导向的产品思维"));
        }
        if (signals.enterprise()) {
            strengths.add(DemoCareerHeuristics.text(locale,
                    "Experience with enterprise or B2B rollout considerations",
                    "有企业级或 B 端场景落地的相关经验"));
        }
        if (strengths.isEmpty()) {
            strengths.add(DemoCareerHeuristics.text(locale,
                    "General product delivery signal is present, but domain depth is light",
                    "具备一定的产品落地基础，但领域深度信号仍偏弱"));
        }

        List<String> missingSignals = new ArrayList<>();
        if (!signals.aiStrategy()) {
            missingSignals.add(DemoCareerHeuristics.text(locale,
                    "Direct AI/LLM product ownership is not clearly visible",
                    "缺少直接负责 AI/LLM 产品的明确信号"));
        }
        if (!signals.platform()) {
            missingSignals.add(DemoCareerHeuristics.text(locale,
                    "Platform thinking, workflow architecture, or API depth is underspecified",
                    "平台化思维、工作流架构或 API 深度仍不够明确"));
        }
        if (!signals.governance()) {
            missingSignals.add(DemoCareerHeuristics.text(locale,
                    "Enterprise AI governance, admin controls, or trust posture is not explicit",
                    "企业级 AI 治理、管理控制或信任机制表达不足"));
        }
        if (!signals.technical()) {
            missingSignals.add(DemoCareerHeuristics.text(locale,
                    "Technical storytelling on evaluation, retrieval, or reliability is limited",
                    "围绕评估、检索或可靠性的技术表达仍偏弱"));
        }
        if (resumeText.length() < 220) {
            missingSignals.add(DemoCareerHeuristics.text(locale,
                    "Impact scope, metrics, and ownership depth need stronger evidence",
                    "影响范围、结果指标和 ownership 深度还需要更强证据"));
        }

        List<String> evidence = signals.evidenceLines().isEmpty()
                ? List.of(DemoCareerHeuristics.text(locale,
                "The resume provides limited concrete evidence beyond high-level role descriptions.",
                "简历目前除了高层描述之外，缺少更具体的事实证据。"))
                : signals.evidenceLines();

        List<String> likelyFitAreas = new ArrayList<>();
        likelyFitAreas.add(targetRole.isBlank()
                ? DemoCareerHeuristics.text(locale, "Product management roles with cross-functional scope", "强调跨团队推进的产品岗位")
                : targetRole);
        if (signals.metrics()) {
            likelyFitAreas.add(DemoCareerHeuristics.text(locale,
                    "Data-informed product strategy and experimentation-heavy roles",
                    "偏数据驱动、强调实验优化的产品岗位"));
        }
        if (signals.aiStrategy()) {
            likelyFitAreas.add(DemoCareerHeuristics.text(locale,
                    "AI-adjacent product roles where GenAI exposure can be expanded",
                    "可继续放大 GenAI 实战经历的 AI 邻近型产品岗位"));
        }

        String headline = DemoCareerHeuristics.text(locale,
                "Candidate profile for %s".formatted(
                        DemoCareerHeuristics.isMeaningful(targetRole) ? targetRole : "the target role"
                ),
                "%s 候选人画像".formatted(
                        DemoCareerHeuristics.isMeaningful(targetRole) ? targetRole : "目标岗位"
                )
        );
        String summary = signals.evidenceLines().isEmpty()
                ? DemoCareerHeuristics.text(locale,
                "The profile suggests relevant PM fundamentals, but the visible evidence is sparse and should be positioned carefully.",
                "这份背景显示出一定的产品基础，但可见证据仍偏少，需要更谨慎地做定位表达。")
                : DemoCareerHeuristics.text(locale,
                "The profile shows PM execution strength with the clearest evidence around %s."
                        .formatted(String.join(", ", strengths.stream().limit(2).toList()).toLowerCase()),
                "这份背景的主要优势体现在：%s。"
                        .formatted(String.join("、", strengths.stream().limit(2).toList())));

        return new CandidateAnalysisResult(
                headline,
                summary,
                DemoCareerHeuristics.distinct(strengths),
                DemoCareerHeuristics.distinct(evidence),
                DemoCareerHeuristics.distinct(missingSignals),
                DemoCareerHeuristics.distinct(likelyFitAreas),
                null
        );
    }
}
