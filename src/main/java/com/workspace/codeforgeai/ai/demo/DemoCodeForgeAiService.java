package com.workspace.codeforgeai.ai.demo;

import com.workspace.codeforgeai.ai.CodeForgeAiService;
import com.workspace.codeforgeai.common.i18n.SupportedLocale;
import com.workspace.codeforgeai.demo.DemoCareerHeuristics;
import dev.langchain4j.service.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@ConditionalOnProperty(prefix = "career.ai", name = "mode", havingValue = "demo", matchIfMissing = true)
public class DemoCodeForgeAiService implements CodeForgeAiService {

    private static final Pattern FOCUS_GAP_PATTERN = Pattern.compile("(?i)(?:focus gap is|remember that my focus gap is)\\s+([^\\.!\\n]+)");
    private final Map<Integer, DemoConversationState> conversations = new ConcurrentHashMap<>();

    @Override
    public String chat(String userMessage) {
        return respond(null, userMessage);
    }

    @Override
    public Report chatForReport(String userMessage) {
        String answer = respond(null, userMessage);
        return new Report(
                "Demo Career Support Summary",
                List.of(
                        "Clarify the top requirements before tailoring your materials.",
                        "Turn the highest-risk gaps into one resume adjustment and one mock interview story.",
                        answer
                )
        );
    }

    @Override
    public Result<String> chatWithRag(String userMessage) {
        return new Result<>(respond(null, userMessage), null, List.of(), null, List.of());
    }

    @Override
    public Flux<String> chatWithStream(int memeryId, String message) {
        String response = respond(memeryId, message);
        return Flux.fromIterable(chunk(response));
    }

    private String respond(Integer memoryId, String message) {
        if (message.contains("Support chat mode: workflow-linked follow-up")) {
            return respondToWorkflowFollowUp(message, memoryId);
        }

        if (message.contains("Support chat mode: locale-aware follow-up")) {
            return respondToLocaleAwareFollowUp(message, memoryId);
        }

        DemoConversationState state = session(memoryId);
        rememberFocusGap(state, message);
        SupportedLocale locale = resolveLocale(message);
        String lower = message.toLowerCase();

        if (lower.contains("what focus gap did i mention") || lower.contains("我刚才提到的差距是什么")) {
            return state.focusGap == null
                    ? DemoCareerHeuristics.text(locale,
                    "You have not shared a focus gap in this session yet. If you want, tell me the gap and I will keep using it in follow-up guidance.",
                    "你在这个会话里还没有告诉我你的重点差距。如果愿意，你现在可以告诉我，我会在后续建议里持续沿用。")
                    : DemoCareerHeuristics.text(locale,
                    "You said your focus gap is %s. I would turn that into one resume proof point and one interview story next.".formatted(state.focusGap),
                    "你刚才提到的重点差距是 %s。下一步我会把它转成一条简历证据和一个面试故事。".formatted(state.focusGap));
        }

        if (lower.contains("who am i") || lower.contains("我是谁")) {
            return state.lastIdentity == null
                    ? DemoCareerHeuristics.text(locale,
                    "You have not introduced yourself in this session yet.",
                    "你在这个会话里还没有介绍过自己。")
                    : DemoCareerHeuristics.text(locale,
                    "In this session, you introduced yourself as %s.".formatted(state.lastIdentity),
                    "在这个会话里，你介绍自己是 %s。".formatted(state.lastIdentity));
        }

        rememberIdentity(state, message);
        if (lower.contains("leetcode") || lower.contains("力扣")) {
            return DemoCareerHeuristics.text(locale,
                    "LeetCode is a coding interview practice platform. In this project it remains a support capability, not the main workflow.",
                    "LeetCode 是一个编程面试练习平台。在这个项目里，它仍然只是辅助能力，不是主工作流。");
        }

        return DemoCareerHeuristics.text(locale,
                "This local demo mode keeps chat useful without external providers. Ask about your top gaps, resume framing, mock questions, or a 7-day prep plan and I will respond with workflow-aware guidance.",
                "这个本地 demo 模式即使没有外部模型也能继续提供帮助。你可以问我重点差距、简历 framing、模拟题，或者 7 天准备计划，我会给出基于 workflow 的建议。");
    }

    private String respondToWorkflowFollowUp(String message, Integer memoryId) {
        DemoConversationState state = session(memoryId);
        SupportedLocale locale = resolveLocale(message);
        String followUp = DemoCareerHeuristics.extractSection(message, "User follow-up:");
        List<String> topPriorities = splitPipeList(DemoCareerHeuristics.extractLineValue(message, "- Top priorities:"));
        List<String> priorityGaps = splitPipeList(DemoCareerHeuristics.extractLineValue(message, "- Priority gaps:"));
        List<String> strengths = splitPipeList(DemoCareerHeuristics.extractLineValue(message, "- Strengths:"));
        List<String> prepPlan = splitPipeList(DemoCareerHeuristics.extractLineValue(message, "- Prep plan:"));
        String fitLevel = DemoCareerHeuristics.extractLineValue(message, "- Fit level:");
        String roleTitle = DemoCareerHeuristics.extractLineValue(message, "- Role title:");
        String summary = DemoCareerHeuristics.extractLineValue(message, "- Narrative:");
        state.lastWorkflowSummary = summary;

        String lower = followUp.toLowerCase();
        if (lower.contains("7-day prep plan") || lower.contains("prep plan") || lower.contains("7天") || lower.contains("准备计划")) {
            List<String> plan = prepPlan.isEmpty() ? defaultPrepPlan(priorityGaps, locale) : prepPlan;
            return DemoCareerHeuristics.text(locale,
                    "Here is a focused 7-day plan for %s:\n- %s".formatted(
                            DemoCareerHeuristics.isMeaningful(roleTitle) ? roleTitle : "this workflow",
                            String.join("\n- ", plan)
                    ),
                    "%s 的 7 天聚焦准备计划如下：\n- %s".formatted(
                            DemoCareerHeuristics.isMeaningful(roleTitle) ? roleTitle : "当前分析结果",
                            String.join("\n- ", plan)
                    ));
        }

        if (lower.contains("resume") || lower.contains("frame my background") || lower.contains("简历") || lower.contains("包装我的背景")) {
            String strongest = strengths.isEmpty() ? "your core PM execution and prioritization signal" : strengths.get(0);
            String topGap = priorityGaps.isEmpty() ? "the role-specific AI depth" : priorityGaps.get(0);
            return DemoCareerHeuristics.text(locale,
                    """
                    Frame your resume around transferability, not apology:
                    - Lead with %s.
                    - Translate adjacent work into direct evidence for %s.
                    - Add one bullet that shows KPI ownership and one that shows cross-functional execution.
                    - In interviews, acknowledge the gap briefly and pivot to your learning velocity plus relevant adjacent wins.
                    """.formatted(strongest, topGap).trim(),
                    """
                    你的简历 framing 要强调“可迁移能力”，而不是先道歉：
                    - 先突出 %s。
                    - 把邻近经验翻译成和 %s 直接相关的证据。
                    - 至少补一条 KPI 负责经历和一条跨团队推进经历。
                    - 面试里简短承认差距，然后立刻转向你的学习速度和相关相邻成果。
                    """.formatted(strongest, topGap).trim());
        }

        if (lower.contains("mock interview questions") || lower.contains("mock questions") || lower.contains("模拟面试") || lower.contains("模拟题")) {
            List<String> gaps = priorityGaps.isEmpty() ? topPriorities : priorityGaps;
            String gap = gaps.isEmpty() ? "technical fluency with model-powered systems" : gaps.get(0);
            return DemoCareerHeuristics.text(locale,
                    """
                    Mock interview questions for the riskiest area:
                    1. Why is %s important in this role right now?
                    2. Tell me about a time you handled an adjacent challenge that maps to %s.
                    3. How would you de-risk this gap in your first 90 days?
                    4. What metrics would you watch to know your approach is working?
                    """.formatted(gap, gap).trim(),
                    """
                    围绕最高风险领域的模拟面试题：
                    1. 为什么 %s 会成为这个岗位当前最关键的能力之一？
                    2. 讲一个你处理过的相邻问题，它和 %s 有什么映射关系？
                    3. 如果你入职后的前 90 天要降低这个差距，你会怎么做？
                    4. 你会看哪些指标来判断自己的方案是否有效？
                    """.formatted(gap, gap).trim());
        }

        if (lower.contains("why") || lower.contains("explain") || lower.contains("为什么") || lower.contains("解释")) {
            String topGap = priorityGaps.isEmpty() ? "the highest-risk requirement" : priorityGaps.get(0);
            return DemoCareerHeuristics.text(locale,
                    "The biggest concern is %s because it is directly tied to the fit narrative (%s). The best response is to connect adjacent evidence to that requirement, then show a concrete preparation plan.".formatted(
                            topGap,
                            DemoCareerHeuristics.isMeaningful(fitLevel) ? fitLevel : "role readiness"
                    ),
                    "最大的顾虑在于 %s，因为它直接影响整体匹配判断（%s）。最好的回应方式，是先用邻近证据把它接上，再给出一套具体准备计划。".formatted(
                            topGap,
                            DemoCareerHeuristics.isMeaningful(fitLevel) ? fitLevel : "岗位匹配度"
                    ));
        }

        return DemoCareerHeuristics.text(locale,
                """
                Based on the saved workflow for %s, the current fit is %s.
                Summary: %s
                Most useful next move: turn the top priorities into one tighter resume narrative and one focused mock interview sprint.
                """.formatted(
                DemoCareerHeuristics.isMeaningful(roleTitle) ? roleTitle : "this role",
                DemoCareerHeuristics.isMeaningful(fitLevel) ? fitLevel : "not specified",
                DemoCareerHeuristics.isMeaningful(summary) ? summary : state.lastWorkflowSummary
        ).trim(),
                """
                基于已保存的 %s workflow，当前匹配判断是 %s。
                总结：%s
                当前最有价值的动作，是把最高优先级事项收敛成一版更紧的简历叙事，再做一轮聚焦模拟面试。
                """.formatted(
                        DemoCareerHeuristics.isMeaningful(roleTitle) ? roleTitle : "岗位",
                        DemoCareerHeuristics.isMeaningful(fitLevel) ? fitLevel : "未明确",
                        DemoCareerHeuristics.isMeaningful(summary) ? summary : state.lastWorkflowSummary
                ).trim());
    }

    private String respondToLocaleAwareFollowUp(String message, Integer memoryId) {
        String followUp = DemoCareerHeuristics.extractSection(message, "User follow-up:");
        return respond(memoryId, """
                Requested response locale: %s
                %s
                """.formatted(resolveLocale(message).languageTag(), followUp));
    }

    private DemoConversationState session(Integer memoryId) {
        if (memoryId == null) {
            return new DemoConversationState();
        }
        return conversations.computeIfAbsent(memoryId, ignored -> new DemoConversationState());
    }

    private void rememberFocusGap(DemoConversationState state, String message) {
        Matcher matcher = FOCUS_GAP_PATTERN.matcher(message);
        if (matcher.find()) {
            state.focusGap = matcher.group(1).trim();
        }
    }

    private void rememberIdentity(DemoConversationState state, String message) {
        Matcher matcher = Pattern.compile("(?i)i am ([^\\.!\\n]+)").matcher(message);
        if (matcher.find()) {
            state.lastIdentity = matcher.group(1).trim();
        }
    }

    private List<String> chunk(String content) {
        List<String> chunks = new ArrayList<>();
        for (String paragraph : content.split("\\n")) {
            if (paragraph.isBlank()) {
                continue;
            }
            String trimmed = paragraph.trim();
            for (int index = 0; index < trimmed.length(); index += 120) {
                chunks.add(trimmed.substring(index, Math.min(index + 120, trimmed.length())));
            }
        }
        return chunks.isEmpty() ? List.of(content) : chunks;
    }

    private List<String> splitPipeList(String value) {
        if (DemoCareerHeuristics.isBlank(value) || value.equals("None") || value.equals("Not available")) {
            return List.of();
        }
        return List.of(value.split("\\|"))
                .stream()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private List<String> defaultPrepPlan(List<String> priorityGaps, SupportedLocale locale) {
        String topGap = priorityGaps.isEmpty() ? "your highest-risk requirement" : priorityGaps.get(0);
        if (locale.isChinese()) {
            return List.of(
                    "第 1-2 天：围绕" + topGap + "重写你的自我介绍和简历 bullet。",
                    "第 3 天：分别准备一个跨团队推进案例和一个 KPI 负责案例。",
                    "第 4 天：练习用非技术听众也能听懂的方式解释关键技术点。",
                    "第 5 天：针对这个差距准备最难的追问。",
                    "第 6-7 天：做模拟面试并收敛你的表达。"
            );
        }
        return List.of(
                "Day 1-2: rewrite your intro and resume bullets around " + topGap + ".",
                "Day 3: prepare one STAR story for cross-functional execution and one for KPI ownership.",
                "Day 4: practice your technical explanation in plain language.",
                "Day 5: rehearse the toughest follow-up questions on the gap.",
                "Day 6-7: run mock interviews and tighten your talking points."
        );
    }

    private SupportedLocale resolveLocale(String message) {
        return SupportedLocale.from(DemoCareerHeuristics.extractLineValue(message, "Requested response locale:"));
    }

    private static final class DemoConversationState {
        private String focusGap;
        private String lastIdentity;
        private String lastWorkflowSummary;
    }
}
