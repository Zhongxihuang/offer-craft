package com.workspace.codeforgeai.career.workflow;

public enum WorkflowRefineAction {
    AUTO,
    EXPLAIN_RESULT,
    APPEND_CANDIDATE_EVIDENCE,
    CLARIFY_JD,
    RERUN_GAP_ANALYSIS,
    RERUN_INTERVIEW_PREP,
    CREATE_PREP_PLAN;

    public static WorkflowRefineAction from(String value) {
        if (value == null || value.isBlank()) {
            return AUTO;
        }

        for (WorkflowRefineAction action : values()) {
            if (action.name().equalsIgnoreCase(value.trim())) {
                return action;
            }
        }

        return AUTO;
    }
}
