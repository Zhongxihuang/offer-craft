package com.workspace.codeforgeai.career.workflow;

import java.util.List;

public record ActionPlanStep(
        String dayRange,
        String title,
        List<String> actions,
        String successSignal
) {
}
