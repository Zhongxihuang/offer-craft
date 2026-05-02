package com.workspace.codeforgeai.career.demo;

import com.workspace.codeforgeai.career.jd.JdAnalysisResult;
import com.workspace.codeforgeai.career.jd.JdParsingAiService;
import com.workspace.codeforgeai.common.i18n.SupportedLocale;
import com.workspace.codeforgeai.demo.DemoCareerHeuristics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(prefix = "career.ai", name = "mode", havingValue = "demo", matchIfMissing = true)
public class DemoJdParsingAiService implements JdParsingAiService {

    @Override
    public JdAnalysisResult analyze(String workflowInput) {
        SupportedLocale locale = DemoCareerHeuristics.requestedLocale(workflowInput);
        String targetRoleHint = DemoCareerHeuristics.extractLineValue(workflowInput, "Target role hint:");
        String targetLevelHint = DemoCareerHeuristics.extractLineValue(workflowInput, "Target level hint:");
        String rawJobDescription = DemoCareerHeuristics.extractSection(workflowInput, "Raw job description:");

        String roleTitle = DemoCareerHeuristics.inferRoleTitle(targetRoleHint, rawJobDescription, locale);
        String seniority = DemoCareerHeuristics.inferSeniority(targetLevelHint, roleTitle, rawJobDescription, locale);
        List<String> mustHaves = DemoCareerHeuristics.inferRequirements(rawJobDescription, locale);
        List<String> niceToHaves = DemoCareerHeuristics.inferNiceToHaves(rawJobDescription, locale);
        List<String> keywords = DemoCareerHeuristics.inferKeywords(rawJobDescription, roleTitle, locale);
        List<String> interviewFocusAreas = DemoCareerHeuristics.inferInterviewFocusAreas(mustHaves, locale);

        String summary = locale.isChinese()
                ? "%s更像是一个%s%s岗位，重点强调%s。".formatted(
                DemoCareerHeuristics.text(locale, "", seniority.equals("未明确说明") ? "" : seniority + "级"),
                roleTitle,
                seniority.equals("未明确说明") ? "" : "",
                String.join("、", keywords.stream().limit(3).toList())
        ).replace("  ", " ").trim()
                : "The JD points to %s %s with emphasis on %s."
                .formatted(
                        seniority.equals("Not clearly stated") ? "" : seniority,
                        roleTitle,
                        String.join(", ", keywords.stream().limit(3).toList())
                )
                .replace("  ", " ")
                .trim();

        return new JdAnalysisResult(
                roleTitle,
                seniority,
                summary,
                mustHaves,
                niceToHaves,
                keywords,
                interviewFocusAreas,
                null
        );
    }
}
