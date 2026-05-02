package com.workspace.codeforgeai.career.workflow;

import com.workspace.codeforgeai.common.i18n.SupportedLocale;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class WorkflowRefineRouter {

    public WorkflowRefineAction resolve(String actionValue, String message, SupportedLocale locale) {
        WorkflowRefineAction explicitAction = WorkflowRefineAction.from(actionValue);
        if (explicitAction != WorkflowRefineAction.AUTO) {
            return explicitAction;
        }

        String normalized = message == null ? "" : message.trim();
        String lower = normalized.toLowerCase(Locale.ROOT);

        if (containsAny(lower, "re-run gap", "rerun gap", "refresh gap", "update gap", "重新分析差距", "重新看差距", "更新差距")) {
            return WorkflowRefineAction.RERUN_GAP_ANALYSIS;
        }
        if (containsAny(lower, "re-run prep", "rerun prep", "refresh prep", "update prep", "7-day prep", "prep plan", "重新生成面试准备", "更新准备计划", "7天准备")) {
            return lower.contains("7-day") || lower.contains("prep plan") || lower.contains("7天")
                    ? WorkflowRefineAction.CREATE_PREP_PLAN
                    : WorkflowRefineAction.RERUN_INTERVIEW_PREP;
        }
        if (containsAny(lower, "clarify jd", "clarify the jd", "the jd also says", "岗位还要求", "补充 jd", "补充岗位", "职位还要求")) {
            return WorkflowRefineAction.CLARIFY_JD;
        }
        if (looksLikeCandidateEvidence(lower, locale)) {
            return WorkflowRefineAction.APPEND_CANDIDATE_EVIDENCE;
        }
        return WorkflowRefineAction.EXPLAIN_RESULT;
    }

    private boolean looksLikeCandidateEvidence(String lower, SupportedLocale locale) {
        if (containsAny(lower, "i also", "i led", "i built", "i shipped", "i owned", "i worked on", "i have experience", "i improved")) {
            return true;
        }

        if (locale.isChinese() && containsAny(lower, "我还", "我也", "我做过", "我负责", "我主导", "我参与过", "补充一下经历", "我之前")) {
            return true;
        }

        return containsAny(lower, "resume bullet", "candidate evidence", "补充经历", "补充简历", "加到简历");
    }

    private boolean containsAny(String text, String... candidates) {
        for (String candidate : candidates) {
            if (text.contains(candidate.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
