package com.workspace.codeforgeai.demo;

import com.workspace.codeforgeai.common.i18n.SupportedLocale;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DemoCareerHeuristics {

    private DemoCareerHeuristics() {
    }

    public static SupportedLocale requestedLocale(String input) {
        return SupportedLocale.from(extractLineValue(input, "Requested output locale:"));
    }

    public static String text(SupportedLocale locale, String english, String chinese) {
        return locale.isChinese() ? chinese : english;
    }

    public static String extractLineValue(String input, String label) {
        if (isBlank(input)) {
            return "";
        }
        Pattern pattern = Pattern.compile("(?m)^" + Pattern.quote(label) + "\\s*(.*)$");
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    public static String extractSection(String input, String label) {
        if (isBlank(input)) {
            return "";
        }
        int start = input.indexOf(label);
        if (start < 0) {
            return "";
        }
        return input.substring(start + label.length()).trim();
    }

    public static List<String> extractBullets(String input, String label) {
        if (isBlank(input)) {
            return List.of();
        }
        int start = input.indexOf(label);
        if (start < 0) {
            return List.of();
        }

        String remaining = input.substring(start + label.length());
        List<String> bullets = new ArrayList<>();
        for (String line : remaining.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("- ")) {
                bullets.add(trimmed.substring(2).trim());
                continue;
            }
            if (!bullets.isEmpty() && (trimmed.isEmpty() || trimmed.endsWith(":"))) {
                break;
            }
        }
        return distinct(bullets);
    }

    public static SignalProfile analyzeSignals(String text) {
        String safeText = normalize(text);
        String lower = safeText.toLowerCase(Locale.ROOT);

        List<String> evidenceLines = safeText.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> line.length() > 12)
                .filter(line -> hasAny(line.toLowerCase(Locale.ROOT),
                        "ai", "llm", "genai", "copilot", "metric", "kpi", "experiment",
                        "platform", "workflow", "retrieval", "latency", "reliability",
                        "governance", "security", "compliance", "stakeholder", "engineering",
                        "customer", "enterprise", "b2b",
                        "人工智能", "大模型", "智能体", "指标", "实验", "平台", "工作流", "检索", "延迟", "可靠性",
                        "治理", "安全", "合规", "跨部门", "工程", "客户", "企业", "b端", "用户"))
                .map(DemoCareerHeuristics::clipLine)
                .limit(5)
                .toList();

        return new SignalProfile(
                hasAny(lower, "ai ", " ai", "genai", "llm", "machine learning", "copilot", "model-powered", "人工智能", "大模型", "智能体", "ai产品"),
                hasAny(lower, "platform", "workflow", "orchestration", "api", "infrastructure", "system", "平台", "工作流", "编排", "接口", "架构"),
                hasAny(lower, "metric", "kpi", "experiment", "analytics", "analysis", "ab test", "a/b", "指标", "实验", "数据分析", "转化率", "roi"),
                hasAny(lower, "governance", "risk", "compliance", "security", "trust", "privacy", "admin control", "治理", "风险", "合规", "安全", "信任", "权限"),
                hasAny(lower, "cross-functional", "stakeholder", "engineering", "design", "sales", "gtm", "go-to-market", "跨部门", "协同", "工程", "设计", "运营", "商业化"),
                hasAny(lower, "retrieval", "evaluation", "latency", "reliability", "prompt", "architecture", "technical", "integration", "检索", "评估", "延迟", "可靠性", "提示词", "技术", "集成"),
                hasAny(lower, "customer", "user research", "discovery", "voice of customer", "客户", "用户", "调研", "洞察"),
                hasAny(lower, "enterprise", "b2b", "admin", "security", "governance", "rollout", "企业", "b端", "权限", "治理", "落地"),
                evidenceLines
        );
    }

    public static String inferRoleTitle(String hint, String text, SupportedLocale locale) {
        if (isMeaningful(hint)) {
            return hint.trim();
        }

        String safeText = normalize(text);
        Matcher matcher = Pattern.compile("(?im)^(.*product manager.*)$").matcher(safeText);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        Matcher chineseMatcher = Pattern.compile("(?m)^.*产品经理.*$").matcher(safeText);
        if (chineseMatcher.find()) {
            return chineseMatcher.group().trim();
        }

        String lower = safeText.toLowerCase(Locale.ROOT);
        if (hasAny(lower, "ai", "genai", "llm", "人工智能", "大模型") && hasAny(lower, "product", "产品")) {
            return locale.isChinese() ? "AI产品经理" : "AI Product Manager";
        }
        if (hasAny(lower, "product", "产品")) {
            return locale.isChinese() ? "产品经理" : "Product Manager";
        }
        return locale.isChinese() ? "目标岗位" : "Target role";
    }

    public static String inferSeniority(String hint, String roleTitle, String text, SupportedLocale locale) {
        if (isMeaningful(hint)) {
            return hint.trim();
        }

        String combined = (normalize(roleTitle) + "\n" + normalize(text)).toLowerCase(Locale.ROOT);
        if (combined.contains("principal")) {
            return locale.isChinese() ? "首席/Principal" : "Principal";
        }
        if (combined.contains("staff")) {
            return locale.isChinese() ? "专家/Staff" : "Staff";
        }
        if (combined.contains("senior") || combined.contains("高级") || combined.contains("资深")) {
            return locale.isChinese() ? "高级" : "Senior";
        }
        if (combined.contains("lead") || combined.contains("负责人")) {
            return locale.isChinese() ? "负责人" : "Lead";
        }
        if (combined.contains("director") || combined.contains("总监")) {
            return locale.isChinese() ? "总监" : "Director";
        }
        if (combined.contains("intern") || combined.contains("internship") || combined.contains("实习") || combined.contains("校招生")) {
            return locale.isChinese() ? "初级/校招" : "Entry-level";
        }
        return locale.isChinese() ? "未明确说明" : "Not clearly stated";
    }

    public static List<String> inferRequirements(String text, SupportedLocale locale) {
        SignalProfile signals = analyzeSignals(text);
        List<String> requirements = new ArrayList<>();

        if (signals.aiStrategy()) {
            requirements.add(locale.isChinese() ? "AI/LLM 产品战略与落地执行能力" : "AI/LLM product strategy and execution");
        }
        if (signals.platform()) {
            requirements.add(locale.isChinese() ? "模型驱动工作流与平台化产品思维" : "Model-powered workflow and platform product thinking");
        }
        if (signals.metrics()) {
            requirements.add(locale.isChinese() ? "实验设计、KPI 负责制与迭代优化能力" : "Experimentation, KPI ownership, and iteration discipline");
        }
        if (signals.crossFunctional()) {
            requirements.add(locale.isChinese() ? "跨工程、设计与商业化团队的协同执行能力" : "Cross-functional execution across engineering, design, and GTM");
        }
        if (signals.governance() || signals.enterprise()) {
            requirements.add(locale.isChinese() ? "企业级落地、治理、信任与发布控制能力" : "Enterprise readiness, governance, trust, and rollout discipline");
        }
        if (signals.technical()) {
            requirements.add(locale.isChinese() ? "对评估、检索、可靠性或 API 的技术理解与表达能力" : "Technical fluency with evaluation, retrieval, reliability, or APIs");
        }
        if (signals.customer()) {
            requirements.add(locale.isChinese() ? "用户洞察与需求抽象能力" : "Customer discovery and requirements synthesis");
        }

        if (requirements.isEmpty()) {
            requirements.add(locale.isChinese() ? "产品推进与利益相关方对齐能力" : "Product execution and stakeholder alignment");
            requirements.add(locale.isChinese() ? "把模糊岗位要求转化为清晰行动方案的能力" : "Ability to translate ambiguous job requirements into a clear plan");
        }

        return distinct(requirements).stream().limit(5).toList();
    }

    public static List<String> inferNiceToHaves(String text, SupportedLocale locale) {
        String lower = normalize(text).toLowerCase(Locale.ROOT);
        List<String> niceToHaves = new ArrayList<>();
        if (hasAny(lower, "developer", "technical writing", "evangelism", "field", "开发者", "技术写作", "布道")) {
            niceToHaves.add(locale.isChinese() ? "面向开发者的沟通或技术赋能经验" : "Developer-facing communication or field enablement experience");
        }
        if (hasAny(lower, "launch", "growth", "go-to-market", "sales", "增长", "上市", "商业化", "销售")) {
            niceToHaves.add(locale.isChinese() ? "企业产品发布与 GTM 协同经验" : "Go-to-market partnership for enterprise product launches");
        }
        if (hasAny(lower, "research", "market", "competitive", "研究", "市场", "竞品")) {
            niceToHaves.add(locale.isChinese() ? "市场研究与竞品定位能力" : "Market research and competitive positioning depth");
        }
        if (niceToHaves.isEmpty()) {
            niceToHaves.add(locale.isChinese() ? "有 AI 实验或提示词迭代的实操案例" : "Hands-on AI experimentation or prompt iteration examples");
            niceToHaves.add(locale.isChinese() ? "面向管理层或客户的表达与故事化沟通能力" : "Visible executive communication or customer-facing storytelling");
        }
        return distinct(niceToHaves).stream().limit(3).toList();
    }

    public static List<String> inferKeywords(String text, String roleTitle, SupportedLocale locale) {
        SignalProfile signals = analyzeSignals(text);
        List<String> keywords = new ArrayList<>();
        if (isMeaningful(roleTitle)) {
            keywords.add(roleTitle.trim());
        }
        if (signals.aiStrategy()) {
            keywords.add(locale.isChinese() ? "AI/LLM 战略" : "AI/LLM strategy");
        }
        if (signals.platform()) {
            keywords.add(locale.isChinese() ? "工作流平台" : "workflow platform");
        }
        if (signals.metrics()) {
            keywords.add(locale.isChinese() ? "实验迭代" : "experimentation");
        }
        if (signals.governance()) {
            keywords.add(locale.isChinese() ? "治理" : "governance");
        }
        if (signals.technical()) {
            keywords.add(locale.isChinese() ? "技术理解" : "technical fluency");
        }
        if (signals.enterprise()) {
            keywords.add(locale.isChinese() ? "企业级落地" : "enterprise readiness");
        }
        return distinct(keywords);
    }

    public static List<String> inferInterviewFocusAreas(List<String> mustHaves, SupportedLocale locale) {
        List<String> focusAreas = new ArrayList<>();
        for (String mustHave : mustHaves) {
            String lower = mustHave.toLowerCase(Locale.ROOT);
            if (hasAny(lower, "ai", "llm", "人工智能", "大模型")) {
                focusAreas.add(locale.isChinese()
                        ? "准备说明你如何判断 AI 用例优先级、设置护栏，并定义产品价值。"
                        : "Explain how you prioritize AI use cases, guardrails, and product value.");
            }
            if (hasAny(lower, "platform", "workflow", "平台", "工作流")) {
                focusAreas.add(locale.isChinese()
                        ? "准备说明你如何在采用率、扩展性与可靠性之间做平台化权衡。"
                        : "Show how you make platform tradeoffs across adoption, extensibility, and reliability.");
            }
            if (hasAny(lower, "metric", "kpi", "iteration", "指标", "实验", "迭代")) {
                focusAreas.add(locale.isChinese()
                        ? "准备 KPI、实验和迭代优化的案例，最好带可量化结果。"
                        : "Prepare KPI, experimentation, and iteration stories with measurable outcomes.");
            }
            if (hasAny(lower, "governance", "trust", "enterprise", "治理", "信任", "企业")) {
                focusAreas.add(locale.isChinese()
                        ? "准备讨论企业级发布风险、治理机制和管理控制。"
                        : "Prepare to discuss enterprise rollout risk, governance, and admin controls.");
            }
            if (hasAny(lower, "technical", "api", "retrieval", "reliability", "技术", "接口", "检索", "可靠性")) {
                focusAreas.add(locale.isChinese()
                        ? "练习用业务能听懂的方式解释评估、检索质量、延迟和可靠性。"
                        : "Practice technical communication on evaluation, retrieval quality, latency, and reliability.");
            }
        }
        if (focusAreas.isEmpty()) {
            focusAreas.add(locale.isChinese()
                    ? "先澄清岗位范围、决策指标和协作模式。"
                    : "Clarify the role scope, decision metrics, and collaboration model.");
        }
        return distinct(focusAreas).stream().limit(5).toList();
    }

    public static boolean isMeaningful(String value) {
        if (isBlank(value)) {
            return false;
        }
        String trimmed = value.trim();
        return !Set.of("not provided", "none", "unknown", "not available", "未提供", "无", "未知").contains(trimmed.toLowerCase(Locale.ROOT));
    }

    public static List<String> distinct(List<String> items) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String item : items) {
            if (!isBlank(item)) {
                unique.add(item.trim());
            }
        }
        return List.copyOf(unique);
    }

    public static boolean hasAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static String normalize(String text) {
        return text == null ? "" : text.trim();
    }

    public static String clipLine(String line) {
        if (line.length() <= 140) {
            return line;
        }
        return line.substring(0, 137) + "...";
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record SignalProfile(
            boolean aiStrategy,
            boolean platform,
            boolean metrics,
            boolean governance,
            boolean crossFunctional,
            boolean technical,
            boolean customer,
            boolean enterprise,
            List<String> evidenceLines
    ) {
    }
}
