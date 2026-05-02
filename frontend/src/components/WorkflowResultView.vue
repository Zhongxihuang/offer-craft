<template>
  <section class="workflow-result">
    <div v-if="workflow" class="result-layout">
      <header class="result-overview">
        <div class="result-overview__main">
          <span class="result-kicker">{{ t('result.eyebrow') }}</span>
          <h2>{{ workflow.jdAnalysis?.roleTitle || workflow.decisionSummary?.recommendedPositioning || t('result.titleFallback') }}</h2>
          <p class="result-lead">{{ workflow.decisionSummary?.summary || t('result.description') }}</p>
          <p v-if="formattedGeneratedAt" class="result-meta">{{ t('result.generated', { value: formattedGeneratedAt }) }}</p>

          <div class="result-statuses">
            <div v-if="workflowNotice" class="result-status">
              <strong>{{ t('result.updatedAfterFollowUp') }}</strong>
              <p>{{ workflowNotice }}</p>
            </div>
            <div v-if="workflow.degradedMode && hasItems(workflow.degradationNotes)" class="result-status result-status--warning">
              <strong>{{ t('result.degradedAlertTitle') }}</strong>
              <p>{{ workflow.degradationNotes.join(' ') }}</p>
            </div>
            <div v-if="showLocaleNotice" class="result-status result-status--neutral">
              <strong>{{ t('result.localeNoticeTitle') }}</strong>
              <p>{{ t('result.localeNoticeBody', { current: currentLocaleLabel, content: contentLocaleLabel }) }}</p>
            </div>
          </div>

          <div class="result-stats">
            <article v-for="stat in overviewStats" :key="stat.label" class="result-stat">
              <span>{{ stat.label }}</span>
              <strong>{{ stat.value }}</strong>
            </article>
          </div>

          <div v-if="workflow.decisionSummary?.recommendedNextMove" class="result-next">
            <span class="result-section-kicker">{{ t('result.nextMoveTitle') }}</span>
            <p>{{ workflow.decisionSummary.recommendedNextMove }}</p>
          </div>
        </div>

        <aside class="result-overview__actions">
          <span class="result-section-kicker">{{ t('result.quickActions') }}</span>
          <button type="button" class="result-button result-button--primary" @click="$emit('start-new-analysis')">
            {{ t('result.startNew') }}
          </button>
          <button type="button" class="result-button result-button--secondary" @click="openSupportChat()">
            {{ t('result.openChat') }}
          </button>
          <button type="button" class="result-button result-button--ghost" @click="copyMarkdownSummary">
            {{ copyStatus || t('result.copyMarkdown') }}
          </button>
          <nav class="result-anchor-nav" :aria-label="t('result.resultNavigation')">
            <button type="button" @click="scrollToSection('decision')">{{ t('result.decisionSummary') }}</button>
            <button type="button" @click="scrollToSection('gaps')">{{ t('result.topThreeGaps') }}</button>
            <button type="button" @click="scrollToSection('actions')">{{ t('result.actionPlan') }}</button>
          </nav>
          <div class="result-chip-row">
            <button
              v-for="prompt in followUpPrompts"
              :key="prompt"
              type="button"
              class="result-chip result-chip--action"
              @click="openSupportChat(prompt)"
            >
              {{ prompt }}
            </button>
          </div>
        </aside>
      </header>

      <section v-if="comparisonResult?.items?.length" class="result-comparison">
        <div class="result-panel__head">
          <div>
            <span class="result-section-kicker">{{ t('result.comparisonSummary') }}</span>
            <h3>{{ t('result.comparisonSummary') }}</h3>
          </div>
          <span v-if="comparisonResult.recommendedWorkflowId" class="result-badge result-badge--decision">
            {{ t('result.recommendedFirst') }}
          </span>
        </div>
        <p class="result-copy">{{ comparisonResult.summary }}</p>
        <div class="result-comparison__grid">
          <article
            v-for="item in comparisonResult.items"
            :key="item.workflowId"
            class="result-comparison__item"
            :class="{ 'result-comparison__item--active': item.workflowId === comparisonResult.recommendedWorkflowId }"
          >
            <div>
              <strong>{{ item.targetRole || t('result.notAvailable') }}</strong>
              <p v-if="item.companyName">{{ item.companyName }}</p>
            </div>
            <div class="result-badges">
              <span class="result-badge">{{ localizedApplyVerdict(item.applyVerdict) }}</span>
              <span class="result-badge result-badge--muted">{{ t('result.priorityScore') }} {{ item.priorityScore }}</span>
            </div>
            <p v-if="item.topGap"><strong>{{ t('result.topThreeGaps') }}：</strong>{{ item.topGap }}</p>
            <p v-if="item.prepCost"><strong>{{ t('result.prepCost') }}：</strong>{{ item.prepCost }}</p>
          </article>
        </div>
      </section>

      <section class="result-workspace">
        <div class="result-main">
          <article id="result-decision" class="result-panel result-panel--decision">
            <div class="result-panel__head">
              <div>
                <span class="result-section-kicker">{{ t('result.executiveSummary') }}</span>
                <h3>{{ t('result.decisionSummary') }}</h3>
              </div>
              <div class="result-badges">
                <span v-if="workflow.decisionSummary?.fitLevel" class="result-badge">{{ localizedFitLevel(workflow.decisionSummary.fitLevel) }}</span>
                <span v-if="workflow.decisionSummary?.recommendedPositioning" class="result-badge result-badge--muted">
                  {{ workflow.decisionSummary.recommendedPositioning }}
                </span>
              </div>
            </div>
            <p class="result-copy">{{ workflow.decisionSummary?.summary }}</p>
            <div v-if="workflow.decisionSummary?.applyVerdict" class="result-callout result-callout--accent">
              <strong>{{ t('result.applyVerdict') }}</strong>
              <div class="result-badges">
                <span class="result-badge result-badge--decision">{{ localizedApplyVerdict(workflow.decisionSummary.applyVerdict) }}</span>
              </div>
              <p v-if="workflow.decisionSummary?.applyVerdictReason">{{ workflow.decisionSummary.applyVerdictReason }}</p>
            </div>
            <div v-if="hasItems(workflow.decisionSummary?.topPriorities)" class="result-callout">
              <strong>{{ t('result.topPriorities') }}</strong>
              <ul class="result-list">
                <li v-for="(item, index) in workflow.decisionSummary.topPriorities" :key="`priority-${index}`">{{ item }}</li>
              </ul>
            </div>
          </article>

          <article id="result-confidence" class="result-panel">
            <div class="result-panel__head">
              <div>
                <span class="result-section-kicker">{{ t('result.confidenceLabel') }}</span>
                <h3>{{ t('result.confidenceSummary') }}</h3>
              </div>
              <div class="result-badges">
                <span v-if="workflow.confidenceSummary?.overallConfidence" class="result-badge">{{ localizedConfidenceLevel(workflow.confidenceSummary.overallConfidence) }}</span>
                <span v-if="workflow.versionInfo" class="result-badge result-badge--muted">v{{ workflow.versionInfo.versionNumber }}</span>
              </div>
            </div>

            <div class="result-stage-grid">
              <article v-for="stage in stageConfidenceCards" :key="stage.key" class="result-stage">
                <span>{{ stage.label }}</span>
                <strong>{{ stage.value }}</strong>
                <p>{{ stage.signals }}</p>
              </article>
            </div>

            <div v-if="workflow.versionInfo?.versionReason" class="result-callout">
              <strong>{{ t('result.versionReason') }}</strong>
              <p>{{ workflow.versionInfo.versionReason }}</p>
            </div>
            <div v-if="workflow.confidenceSummary?.confidenceRationale" class="result-callout">
              <strong>{{ t('result.confidenceRationale') }}</strong>
              <p>{{ workflow.confidenceSummary.confidenceRationale }}</p>
            </div>

            <section v-if="hasItems(workflow.decisionDrivers)" class="result-block">
              <h4>{{ t('result.whyWeBelieveThis') }}</h4>
              <div class="result-driver-grid">
                <article v-for="(driver, index) in workflow.decisionDrivers" :key="`driver-${index}`" class="result-driver">
                  <span class="result-badge result-badge--muted">{{ sourceLabel(driver.sourceType) }}</span>
                  <p>{{ driver.statement }}</p>
                </article>
              </div>
            </section>

            <section v-if="hasItems(workflow.confidenceSummary?.blockingUncertainties)" class="result-block">
              <h4>{{ t('result.blockingUncertainties') }}</h4>
              <ul class="result-list">
                <li v-for="(item, index) in workflow.confidenceSummary.blockingUncertainties" :key="`blocking-${index}`">{{ item }}</li>
              </ul>
            </section>

            <section v-if="hasItems(workflow.clarificationQuestions)" class="result-block">
              <h4>{{ t('result.whatCouldChangeAnswer') }}</h4>
              <div class="result-chip-row">
                <button
                  v-for="(question, index) in workflow.clarificationQuestions"
                  :key="`clarification-${index}`"
                  type="button"
                  class="result-chip"
                  @click="openSupportChat(question, 'refine')"
                >
                  {{ question }}
                </button>
              </div>
            </section>

            <div class="result-split">
              <section v-if="hasItems(workflow.confidenceSummary?.strongestEvidence)">
                <h4>{{ t('result.strongestEvidence') }}</h4>
                <ul class="result-list">
                  <li v-for="(item, index) in workflow.confidenceSummary.strongestEvidence" :key="`strong-${index}`">{{ item }}</li>
                </ul>
              </section>
              <section v-if="hasItems(workflow.confidenceSummary?.missingEvidence)">
                <h4>{{ t('result.missingEvidence') }}</h4>
                <ul class="result-list">
                  <li v-for="(item, index) in workflow.confidenceSummary.missingEvidence" :key="`missing-${index}`">{{ item }}</li>
                </ul>
              </section>
              <section v-if="hasItems(workflow.confidenceSummary?.inferenceNotes)">
                <h4>{{ t('result.inferenceNotes') }}</h4>
                <ul class="result-list">
                  <li v-for="(item, index) in workflow.confidenceSummary.inferenceNotes" :key="`inference-${index}`">{{ item }}</li>
                </ul>
              </section>
              <section v-if="hasItems(workflow.confidenceSummary?.mostInfluentialGaps)">
                <h4>{{ t('result.mostInfluentialGaps') }}</h4>
                <ul class="result-list">
                  <li v-for="(item, index) in workflow.confidenceSummary.mostInfluentialGaps" :key="`influential-${index}`">{{ item }}</li>
                </ul>
              </section>
            </div>
          </article>

          <article id="result-gaps" class="result-panel result-panel--gap">
            <div class="result-panel__head">
              <div>
                <span class="result-section-kicker">{{ t('result.gapAnalysis') }}</span>
                <h3>{{ t('result.topThreeGaps') }}</h3>
              </div>
              <span v-if="workflow.gapAnalysis?.overallAssessment" class="result-badge">{{ localizedFitLevel(workflow.gapAnalysis.overallAssessment) }}</span>
            </div>
            <p class="result-copy">{{ workflow.gapAnalysis?.matchNarrative }}</p>
            <div v-if="hasItems(topPriorityGaps)" class="result-callout result-callout--accent">
              <strong>{{ t('result.topGapDecisionLens') }}</strong>
              <p>{{ t('result.topGapDecisionLensBody') }}</p>
            </div>
            <div v-if="hasItems(topPriorityGaps)" class="result-gap-grid">
              <article v-for="(gap, index) in topPriorityGaps" :key="`gap-${index}`" class="result-gap">
                <div class="result-gap__head">
                  <div>
                    <span class="result-gap__rank">{{ t('result.gapRank', { index: index + 1 }) }}</span>
                    <strong>{{ gap.requirement }}</strong>
                    <p v-if="gap.rankingReason">{{ gap.rankingReason }}</p>
                  </div>
                  <span v-if="gap.gapLevel" class="result-badge result-badge--danger">{{ localizedConfidenceLevel(gap.gapLevel) }}</span>
                </div>
                <div class="result-gap__meta">
                  <span v-if="gap.hiringImpact" class="result-badge result-badge--meta">
                    {{ t('result.hiringImpact') }}：{{ localizedImpactLevel(gap.hiringImpact) }}
                  </span>
                  <span v-if="gap.interviewRisk" class="result-badge result-badge--meta">
                    {{ t('result.interviewRisk') }}：{{ localizedImpactLevel(gap.interviewRisk) }}
                  </span>
                  <span v-if="gap.evidenceStrength" class="result-badge result-badge--meta">
                    {{ t('result.evidenceStrength') }}：{{ localizedImpactLevel(gap.evidenceStrength) }}
                  </span>
                </div>
                <div class="result-gap__decision-grid">
                  <section class="result-gap__decision-card">
                    <span>{{ t('result.whyThisGapMatters') }}</span>
                    <p>{{ gap.rankingReason || gapImpactFallback(gap) }}</p>
                  </section>
                  <section class="result-gap__decision-card">
                    <span>{{ t('result.evidenceWeakness') }}</span>
                    <p>{{ gap.candidateEvidence || t('result.noCandidateEvidenceForGap') }}</p>
                  </section>
                  <section class="result-gap__decision-card">
                    <span>{{ t('result.prepAction') }}</span>
                    <p>{{ gap.recommendation || t('result.noPrepActionForGap') }}</p>
                  </section>
                </div>
              </article>
            </div>
            <div class="result-split">
              <section v-if="hasItems(workflow.gapAnalysis?.matchingStrengths)">
                <h4>{{ t('result.matchingStrengths') }}</h4>
                <ul class="result-list">
                  <li v-for="(item, index) in workflow.gapAnalysis.matchingStrengths" :key="`match-${index}`">{{ item }}</li>
                </ul>
              </section>
              <section v-if="hasItems(workflow.gapAnalysis?.positioningAdvice)">
                <h4>{{ t('result.positioningAdvice') }}</h4>
                <ul class="result-list">
                  <li v-for="(item, index) in workflow.gapAnalysis.positioningAdvice" :key="`position-${index}`">{{ item }}</li>
                </ul>
              </section>
            </div>
          </article>

          <article id="result-prep" class="result-panel result-panel--prep">
            <div class="result-panel__head">
              <div>
                <span class="result-section-kicker">{{ t('result.interviewPrep') }}</span>
                <h3>{{ t('result.prepPlan') }}</h3>
              </div>
            </div>
            <p class="result-copy">{{ workflow.interviewPrep?.prepSummary }}</p>
            <div class="result-split">
              <section v-if="hasItems(workflow.interviewPrep?.prepPlan)">
                <h4>{{ t('result.prepPlan') }}</h4>
                <ul class="result-list">
                  <li v-for="(item, index) in workflow.interviewPrep.prepPlan" :key="`prep-${index}`">{{ item }}</li>
                </ul>
              </section>
              <section v-if="hasItems(workflow.interviewPrep?.technicalFocusAreas)">
                <h4>{{ t('result.technicalFocusAreas') }}</h4>
                <ul class="result-list">
                  <li v-for="(item, index) in workflow.interviewPrep.technicalFocusAreas" :key="`tech-${index}`">{{ item }}</li>
                </ul>
              </section>
              <section v-if="hasItems(workflow.interviewPrep?.behavioralQuestionPrompts)">
                <h4>{{ t('result.behavioralPrompts') }}</h4>
                <ul class="result-list">
                  <li v-for="(item, index) in workflow.interviewPrep.behavioralQuestionPrompts" :key="`behavior-${index}`">{{ item }}</li>
                </ul>
              </section>
              <section v-if="hasItems(workflow.interviewPrep?.resumeDefensePoints)">
                <h4>{{ t('result.resumeDefensePoints') }}</h4>
                <ul class="result-list">
                  <li v-for="(item, index) in workflow.interviewPrep.resumeDefensePoints" :key="`resume-${index}`">{{ item }}</li>
                </ul>
              </section>
              <section v-if="hasItems(workflow.interviewPrep?.companyResearchSuggestions)">
                <h4>{{ t('result.companyResearchSuggestions') }}</h4>
                <ul class="result-list">
                  <li v-for="(item, index) in workflow.interviewPrep.companyResearchSuggestions" :key="`company-${index}`">{{ item }}</li>
                </ul>
              </section>
            </div>
          </article>
        </div>

        <aside class="result-side">
          <article id="result-actions" class="result-panel result-panel--plan">
            <div class="result-panel__head">
              <div>
                <span class="result-section-kicker">{{ t('result.actionPlan') }}</span>
                <h3>{{ t('result.actionPlan') }}</h3>
              </div>
            </div>
            <div v-if="hasItems(workflow.actionPlan)" class="result-action-plan">
              <article v-for="(step, index) in workflow.actionPlan" :key="`action-step-${index}`" class="result-action-step">
                <div class="result-action-step__head">
                  <span class="result-badge result-badge--muted">{{ step.dayRange }}</span>
                  <strong>{{ step.title }}</strong>
                </div>
                <ul v-if="hasItems(step.actions)" class="result-list result-checklist">
                  <li v-for="(action, actionIndex) in step.actions" :key="`action-${index}-${actionIndex}`">
                    <label class="result-check">
                      <input
                        type="checkbox"
                        :checked="isActionDone(index, actionIndex)"
                        @change="toggleAction(index, actionIndex, $event.target.checked)"
                      />
                      <span>{{ action }}</span>
                    </label>
                  </li>
                </ul>
                <p v-if="step.successSignal" class="result-action-step__signal">
                  <strong>{{ t('result.successSignal') }}：</strong>{{ step.successSignal }}
                </p>
                <button
                  type="button"
                  class="result-chip result-chip--action"
                  @click="openSupportChat(step.title, 'refine')"
                >
                  {{ t('result.openChat') }}
                </button>
              </article>
            </div>
            <p v-else class="result-empty">{{ t('result.noNextSteps') }}</p>
          </article>

          <article class="result-panel">
            <div class="result-panel__head">
              <div>
                <span class="result-section-kicker">{{ t('result.nextSteps') }}</span>
                <h3>{{ t('result.nextSteps') }}</h3>
              </div>
            </div>
            <ul v-if="hasItems(workflow.nextSteps)" class="result-list">
              <li v-for="(item, index) in workflow.nextSteps" :key="`next-${index}`">{{ item }}</li>
            </ul>
            <p v-else class="result-empty">{{ t('result.noNextSteps') }}</p>
          </article>

          <article class="result-panel">
            <div class="result-panel__head">
              <div>
                <span class="result-section-kicker">{{ t('result.systemStatus') }}</span>
                <h3>{{ t('result.systemStatus') }}</h3>
              </div>
            </div>
            <div class="result-detail-grid">
              <div>
                <span class="result-label">{{ t('result.usedSearch') }}</span>
                <p>{{ workflow.usedSearch ? t('result.enabled') : t('result.disabled') }}</p>
              </div>
              <div>
                <span class="result-label">{{ t('result.usedRetrieval') }}</span>
                <p>{{ workflow.usedRetrieval ? t('result.enabled') : t('result.disabled') }}</p>
              </div>
              <div>
                <span class="result-label">{{ t('result.degradedMode') }}</span>
                <p>{{ workflow.degradedMode ? t('result.enabled') : t('result.disabled') }}</p>
              </div>
            </div>
            <section v-if="hasItems(workflow.degradationNotes)">
              <h4>{{ t('result.degradationNotes') }}</h4>
              <ul class="result-list">
                <li v-for="(item, index) in workflow.degradationNotes" :key="`degrade-${index}`">{{ item }}</li>
              </ul>
            </section>
          </article>

          <article class="result-panel">
            <div class="result-panel__head">
              <div>
                <span class="result-section-kicker">{{ t('result.versionHistory') }}</span>
                <h3>{{ t('result.versionHistory') }}</h3>
              </div>
            </div>
            <ul v-if="hasItems(workflowVersions)" class="result-list">
              <li v-for="version in workflowVersions" :key="version.workflowId">
                <strong>v{{ version.versionNumber }}</strong>
                <span> · {{ version.fitLevel ? localizedFitLevel(version.fitLevel) : t('result.notAvailable') }}</span>
                <span v-if="version.versionReason"> · {{ version.versionReason }}</span>
              </li>
            </ul>
            <p v-else class="result-empty">{{ t('result.noVersionHistory') }}</p>
          </article>
        </aside>
      </section>

      <section class="result-details">
        <article class="result-panel">
          <div class="result-panel__head">
            <div>
              <span class="result-section-kicker">{{ t('result.jdAnalysis') }}</span>
              <h3>{{ t('result.jdAnalysis') }}</h3>
            </div>
          </div>
          <p v-if="workflow.jdAnalysis?.confidence" class="result-signal-line">{{ signalSummary(workflow.jdAnalysis.confidence) }}</p>
          <div class="result-detail-grid">
            <div v-if="workflow.jdAnalysis?.roleTitle">
              <span class="result-label">{{ t('result.roleTitle') }}</span>
              <p>{{ workflow.jdAnalysis.roleTitle }}</p>
            </div>
            <div v-if="workflow.jdAnalysis?.seniority">
              <span class="result-label">{{ t('result.seniority') }}</span>
              <p>{{ workflow.jdAnalysis.seniority }}</p>
            </div>
          </div>
          <p class="result-copy">{{ workflow.jdAnalysis?.summary }}</p>
          <div class="result-split">
            <section v-if="hasItems(workflow.jdAnalysis?.mustHaveRequirements)">
              <h4>{{ t('result.mustHaveRequirements') }}</h4>
              <ul class="result-list">
                <li v-for="(item, index) in workflow.jdAnalysis.mustHaveRequirements" :key="`must-${index}`">{{ item }}</li>
              </ul>
            </section>
            <section v-if="hasItems(workflow.jdAnalysis?.niceToHaveRequirements)">
              <h4>{{ t('result.niceToHaveRequirements') }}</h4>
              <ul class="result-list">
                <li v-for="(item, index) in workflow.jdAnalysis.niceToHaveRequirements" :key="`nice-${index}`">{{ item }}</li>
              </ul>
            </section>
            <section v-if="hasItems(workflow.jdAnalysis?.keywords)">
              <h4>{{ t('result.keywords') }}</h4>
              <ul class="result-list">
                <li v-for="(item, index) in workflow.jdAnalysis.keywords" :key="`keyword-${index}`">{{ item }}</li>
              </ul>
            </section>
            <section v-if="hasItems(workflow.jdAnalysis?.interviewFocusAreas)">
              <h4>{{ t('result.interviewFocusAreas') }}</h4>
              <ul class="result-list">
                <li v-for="(item, index) in workflow.jdAnalysis.interviewFocusAreas" :key="`focus-${index}`">{{ item }}</li>
              </ul>
            </section>
          </div>
        </article>

        <article class="result-panel">
          <div class="result-panel__head">
            <div>
              <span class="result-section-kicker">{{ t('result.candidateAnalysis') }}</span>
              <h3>{{ t('result.candidateAnalysis') }}</h3>
            </div>
          </div>
          <p v-if="workflow.candidateAnalysis?.confidence" class="result-signal-line">{{ signalSummary(workflow.candidateAnalysis.confidence) }}</p>
          <p v-if="workflow.candidateAnalysis?.candidateHeadline" class="result-headline">{{ workflow.candidateAnalysis.candidateHeadline }}</p>
          <p class="result-copy">{{ workflow.candidateAnalysis?.summary }}</p>
          <div class="result-split">
            <section v-if="hasItems(workflow.candidateAnalysis?.strengths)">
              <h4>{{ t('result.strengths') }}</h4>
              <ul class="result-list">
                <li v-for="(item, index) in workflow.candidateAnalysis.strengths" :key="`strength-${index}`">{{ item }}</li>
              </ul>
            </section>
            <section v-if="hasItems(workflow.candidateAnalysis?.evidence)">
              <h4>{{ t('result.evidence') }}</h4>
              <ul class="result-list">
                <li v-for="(item, index) in workflow.candidateAnalysis.evidence" :key="`evidence-${index}`">{{ item }}</li>
              </ul>
            </section>
            <section v-if="hasItems(workflow.candidateAnalysis?.missingSignals)">
              <h4>{{ t('result.missingSignals') }}</h4>
              <ul class="result-list">
                <li v-for="(item, index) in workflow.candidateAnalysis.missingSignals" :key="`missing-signal-${index}`">{{ item }}</li>
              </ul>
            </section>
            <section v-if="hasItems(workflow.candidateAnalysis?.likelyFitAreas)">
              <h4>{{ t('result.likelyFitAreas') }}</h4>
              <ul class="result-list">
                <li v-for="(item, index) in workflow.candidateAnalysis.likelyFitAreas" :key="`fit-area-${index}`">{{ item }}</li>
              </ul>
            </section>
          </div>
        </article>

        <article class="result-panel">
          <div class="result-panel__head">
            <div>
              <span class="result-section-kicker">{{ t('result.supportCapabilities') }}</span>
              <h3>{{ t('result.supportCapabilities') }}</h3>
            </div>
          </div>
          <ul v-if="hasItems(workflow.supportCapabilities)" class="result-list">
            <li v-for="(item, index) in workflow.supportCapabilities" :key="`support-${index}`">{{ item }}</li>
          </ul>
          <p v-else class="result-empty">{{ t('result.noSupportCapabilities') }}</p>
        </article>
      </section>
    </div>

    <div v-else class="result-empty-state">
      <p>{{ t('result.noWorkflow') }}</p>
    </div>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  workflowResult: { type: Object, default: null },
  response: { type: Object, default: null },
  comparisonResult: { type: Object, default: null },
  workflowVersions: { type: Array, default: () => [] },
  workflowNotice: { type: String, default: '' }
})

const emit = defineEmits(['start-new-analysis', 'open-support-chat'])
const { locale, t } = useI18n()

const workflow = computed(() => props.workflowResult || props.response || null)
const completedActions = ref({})
const copyStatus = ref('')
const contentLocale = computed(() => normalizeLocale(workflow.value?.contentLocale))
const currentLocale = computed(() => normalizeLocale(locale.value))
const showLocaleNotice = computed(() => Boolean(workflow.value && contentLocale.value !== currentLocale.value))
const currentLocaleLabel = computed(() => localeLabel(currentLocale.value))
const contentLocaleLabel = computed(() => localeLabel(contentLocale.value))

const formattedGeneratedAt = computed(() => {
  const value = workflow.value?.generatedAt
  if (!value) return ''
  const parsed = new Date(value)
  if (Number.isNaN(parsed.getTime())) return ''
  return parsed.toLocaleString(locale.value, { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' })
})

const followUpPrompts = computed(() => [
  t('result.followUpPrompts.plan'),
  t('result.followUpPrompts.resume'),
  t('result.followUpPrompts.mock')
])

const topPriorityGaps = computed(() => {
  const gaps = workflow.value?.gapAnalysis?.priorityGaps
  return Array.isArray(gaps) ? gaps.slice(0, 3) : []
})

const overviewStats = computed(() => {
  if (!workflow.value) return []
  return [
    { label: t('result.fitLabel'), value: localizedFitLevel(workflow.value.decisionSummary?.fitLevel) || t('result.notAvailable') },
    { label: t('result.confidenceLabel'), value: localizedConfidenceLevel(workflow.value.confidenceSummary?.overallConfidence) || t('result.notAvailable') },
    { label: t('result.versionLabel'), value: workflow.value.versionInfo ? `v${workflow.value.versionInfo.versionNumber}` : 'v1' },
    { label: t('result.artifactLanguage'), value: contentLocaleLabel.value }
  ]
})

const stageConfidenceCards = computed(() => {
  if (!workflow.value) return []
  const stages = [
    { key: 'jd', label: t('result.jdAnalysis'), confidence: workflow.value.jdAnalysis?.confidence },
    { key: 'candidate', label: t('result.candidateAnalysis'), confidence: workflow.value.candidateAnalysis?.confidence },
    { key: 'gap', label: t('result.gapAnalysis'), confidence: workflow.value.gapAnalysis?.confidence },
    { key: 'prep', label: t('result.interviewPrep'), confidence: workflow.value.interviewPrep?.confidence }
  ]
  return stages.map((stage) => ({
    key: stage.key,
    label: stage.label,
    value: localizedConfidenceLevel(stage.confidence?.evidenceStrength) || t('result.notAvailable'),
    signals: signalSummary(stage.confidence)
  }))
})

watch(
  () => workflow.value?.workflowId,
  (workflowId) => {
    completedActions.value = loadCompletedActions(workflowId)
  },
  { immediate: true }
)

function normalizeLocale(value) {
  return typeof value === 'string' && value.toLowerCase().startsWith('zh') ? 'zh-CN' : 'en'
}

function localeLabel(value) {
  return value === 'zh-CN' ? t('locale.chinese') : t('locale.english')
}

function hasItems(value) {
  return Array.isArray(value) && value.length > 0
}

function signalSummary(confidence) {
  if (!confidence) return ''
  return t('result.signalSummary', {
    observed: confidence.observedSignalCount ?? 0,
    inferred: confidence.inferredSignalCount ?? 0,
    missing: confidence.missingSignalCount ?? 0
  })
}

function sourceLabel(sourceType) {
  const normalized = typeof sourceType === 'string' && sourceType.trim() ? sourceType.trim() : 'MODEL_INFERENCE'
  return t(`result.source.${normalized}`)
}

function localizedFitLevel(value) {
  const normalized = typeof value === 'string' ? value.trim() : ''
  if (!normalized) return ''
  const translated = t(`result.fitLevelMap.${normalized}`)
  return translated === `result.fitLevelMap.${normalized}` ? normalized : translated
}

function localizedConfidenceLevel(value) {
  const normalized = typeof value === 'string' ? value.trim() : ''
  if (!normalized) return ''
  const translated = t(`result.confidenceLevelMap.${normalized}`)
  return translated === `result.confidenceLevelMap.${normalized}` ? normalized : translated
}

function localizedImpactLevel(value) {
  const normalized = typeof value === 'string' ? value.trim() : ''
  if (!normalized) return ''
  const translated = t(`result.impactLevelMap.${normalized}`)
  return translated === `result.impactLevelMap.${normalized}` ? normalized : translated
}

function localizedApplyVerdict(value) {
  const normalized = typeof value === 'string' ? value.trim() : ''
  if (!normalized) return ''
  const translated = t(`result.applyVerdictMap.${normalized}`)
  return translated === `result.applyVerdictMap.${normalized}` ? normalized : translated
}

function gapImpactFallback(gap) {
  const impact = localizedImpactLevel(gap?.hiringImpact)
  const risk = localizedImpactLevel(gap?.interviewRisk)

  if (impact && risk) {
    return t('result.gapImpactFallbackWithSignals', { impact, risk })
  }

  return t('result.gapImpactFallback')
}

function openSupportChat(prompt, mode = 'ask') {
  emit('open-support-chat', { prompt, mode })
}

function scrollToSection(section) {
  const target = typeof document !== 'undefined' ? document.getElementById(`result-${section}`) : null
  target?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function copyMarkdownSummary() {
  const markdown = buildMarkdownSummary()
  if (!markdown) return

  try {
    await navigator.clipboard.writeText(markdown)
    copyStatus.value = t('result.copied')
  } catch (error) {
    copyStatus.value = t('result.copyFailed')
  }

  window.setTimeout(() => {
    copyStatus.value = ''
  }, 1800)
}

function buildMarkdownSummary() {
  if (!workflow.value) return ''
  const gaps = topPriorityGaps.value
    .map((gap, index) => [
      `${index + 1}. ${gap.requirement}`,
      gap.rankingReason ? `   ${t('result.whyThisGapMatters')}: ${gap.rankingReason}` : '',
      gap.candidateEvidence ? `   ${t('result.evidenceWeakness')}: ${gap.candidateEvidence}` : '',
      gap.recommendation ? `   ${t('result.prepAction')}: ${gap.recommendation}` : ''
    ].filter(Boolean).join('\n'))
    .join('\n')
  const actions = Array.isArray(workflow.value.actionPlan)
    ? workflow.value.actionPlan
        .map((step) => `- ${step.dayRange}: ${step.title}${Array.isArray(step.actions) ? `\n  - ${step.actions.join('\n  - ')}` : ''}`)
        .join('\n')
    : ''

  return [
    `# ${workflow.value.jdAnalysis?.roleTitle || workflow.value.decisionSummary?.recommendedPositioning || t('result.titleFallback')}`,
    '',
    `**${t('result.applyVerdict')}:** ${localizedApplyVerdict(workflow.value.decisionSummary?.applyVerdict) || t('result.notAvailable')}`,
    `**${t('result.confidenceLabel')}:** ${localizedConfidenceLevel(workflow.value.confidenceSummary?.overallConfidence) || t('result.notAvailable')}`,
    '',
    `## ${t('result.decisionSummary')}`,
    workflow.value.decisionSummary?.summary || '',
    workflow.value.decisionSummary?.recommendedNextMove ? `\n${workflow.value.decisionSummary.recommendedNextMove}` : '',
    '',
    `## ${t('result.topThreeGaps')}`,
    gaps || t('result.notAvailable'),
    '',
    `## ${t('result.actionPlan')}`,
    actions || t('result.notAvailable')
  ].join('\n')
}

function actionKey(stepIndex, actionIndex) {
  return `${stepIndex}:${actionIndex}`
}

function checklistStorageKey(workflowId) {
  return `careerAgent_actionPlan_${workflowId}`
}

function loadCompletedActions(workflowId) {
  if (!workflowId || typeof window === 'undefined') {
    return {}
  }

  try {
    const parsed = JSON.parse(window.localStorage.getItem(checklistStorageKey(workflowId)) || '{}')
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch (error) {
    return {}
  }
}

function persistCompletedActions() {
  const workflowId = workflow.value?.workflowId
  if (!workflowId || typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(checklistStorageKey(workflowId), JSON.stringify(completedActions.value))
}

function isActionDone(stepIndex, actionIndex) {
  return Boolean(completedActions.value[actionKey(stepIndex, actionIndex)])
}

function toggleAction(stepIndex, actionIndex, checked) {
  completedActions.value = {
    ...completedActions.value,
    [actionKey(stepIndex, actionIndex)]: Boolean(checked)
  }
  persistCompletedActions()
}
</script>

<style scoped>
.workflow-result,.result-layout,.result-main,.result-side,.result-details{display:grid;gap:var(--spacing-lg)}
.result-overview,.result-panel,.result-comparison,.result-empty-state{border:1px solid rgba(188,199,210,.84);background:rgba(255,255,255,.92);box-shadow:var(--shadow-md)}
.result-overview{display:grid;grid-template-columns:minmax(0,1.45fr) minmax(300px,.8fr);gap:var(--spacing-lg);padding:clamp(1.5rem,3.4vw,2.2rem);border-radius:var(--radius-xl);background:radial-gradient(circle at top right,rgba(20,124,131,.08),transparent 26%),radial-gradient(circle at bottom left,rgba(186,124,55,.08),transparent 22%),linear-gradient(180deg,rgba(255,255,255,.96),rgba(249,247,242,.96))}
.result-overview__main,.result-overview__actions,.result-statuses,.result-stats,.result-workspace,.result-split,.result-stage-grid,.result-driver-grid,.result-gap-grid,.result-detail-grid,.result-block{display:grid;gap:var(--spacing-md)}
.result-overview__actions{align-content:start}
.result-kicker,.result-section-kicker{display:inline-flex;align-items:center;width:fit-content;min-height:28px;padding:.28rem .74rem;border-radius:var(--radius-full);background:rgba(16,38,61,.08);color:var(--color-primary);font-size:.74rem;font-weight:800;letter-spacing:.12em;text-transform:uppercase}
.result-lead,.result-copy,.result-panel p{color:var(--color-text-muted);line-height:1.62}
.result-meta{color:var(--color-text-light);font-size:.9rem}
.result-status{display:grid;gap:.35rem;padding:1rem 1.05rem;border-radius:18px;border:1px solid rgba(20,124,131,.24);background:rgba(20,124,131,.06)}
.result-status p,.result-next p,.result-driver p,.result-gap p{margin:0}
.result-status--warning{border-color:rgba(186,124,55,.28);background:rgba(186,124,55,.08)}
.result-status--neutral{border-color:rgba(188,199,210,.72);background:rgba(245,247,250,.84)}
.result-stats{grid-template-columns:repeat(4,minmax(0,1fr));gap:12px}
.result-stat{display:grid;gap:.35rem;padding:.95rem 1rem;border-radius:20px;border:1px solid rgba(188,199,210,.74);background:rgba(255,255,255,.88)}
.result-stat span,.result-label,.result-stage span{font-size:.76rem;font-weight:800;letter-spacing:.1em;text-transform:uppercase;color:var(--color-text-light)}
.result-stat strong,.result-stage strong{color:var(--color-primary)}
.result-next,.result-callout{display:grid;gap:.45rem;padding:1rem 1.05rem;border-radius:20px;border:1px solid rgba(188,199,210,.72);background:rgba(255,255,255,.82)}
.result-button{min-height:50px;padding:.9rem 1rem;border-radius:18px;font-weight:700;text-align:left;transition:transform var(--transition-fast),box-shadow var(--transition-normal),border-color var(--transition-normal)}
.result-button--primary{background:linear-gradient(135deg,var(--color-primary),#173552);color:#fff;box-shadow:0 24px 50px -36px rgba(16,38,61,.76)}
.result-button--secondary{border:1px solid rgba(188,199,210,.82);background:rgba(255,255,255,.9);color:var(--color-primary)}
.result-button--ghost{border:1px dashed rgba(20,124,131,.42);background:rgba(20,124,131,.06);color:var(--color-secondary-hover)}
.result-anchor-nav{display:grid;gap:8px;padding:.75rem;border-radius:18px;border:1px solid rgba(188,199,210,.68);background:rgba(255,255,255,.7)}
.result-anchor-nav button{padding:.48rem .6rem;border-radius:12px;color:var(--color-text-muted);font-weight:700;text-align:left}
.result-anchor-nav button:hover{background:rgba(16,38,61,.07);color:var(--color-primary)}
.result-chip-row{display:flex;flex-wrap:wrap;gap:10px}
.result-chip{padding:.72rem .92rem;border-radius:var(--radius-full);border:1px solid rgba(188,199,210,.74);background:rgba(250,248,244,.92);color:var(--color-text);font-weight:600;text-align:left;transition:transform var(--transition-fast),box-shadow var(--transition-fast),border-color var(--transition-fast)}
.result-chip--action{color:var(--color-primary)}
.result-button:hover,.result-chip:hover{transform:translateY(-1px);box-shadow:var(--shadow-sm)}
.result-workspace{grid-template-columns:minmax(0,1.65fr) minmax(290px,.82fr)}
.result-details{grid-template-columns:repeat(3,minmax(0,1fr))}
.result-panel{padding:clamp(1.15rem,2.6vw,1.4rem);border-radius:var(--radius-lg);background:linear-gradient(180deg,rgba(255,255,255,.96),rgba(249,247,242,.94))}
.result-panel--decision{background:radial-gradient(circle at top right,rgba(20,124,131,.08),transparent 28%),linear-gradient(180deg,rgba(255,255,255,.98),rgba(248,246,241,.96))}
.result-panel--gap{background:radial-gradient(circle at top right,rgba(186,124,55,.08),transparent 28%),linear-gradient(180deg,rgba(255,255,255,.98),rgba(248,246,241,.96))}
.result-panel__head{display:flex;justify-content:space-between;gap:var(--spacing-md);align-items:flex-start}
.result-badges{display:flex;flex-wrap:wrap;gap:.45rem}
.result-badge{display:inline-flex;align-items:center;min-height:30px;padding:.32rem .68rem;border-radius:var(--radius-full);background:rgba(16,38,61,.08);color:var(--color-primary);font-size:.8rem;font-weight:700}
.result-badge--muted{background:rgba(20,124,131,.08);color:var(--color-secondary-hover)}
.result-badge--danger{background:rgba(180,72,72,.12);color:var(--color-error)}
.result-list{padding-left:1.15rem;display:grid;gap:.5rem;color:var(--color-text-muted)}
.result-stage-grid{grid-template-columns:repeat(4,minmax(0,1fr));gap:12px}
.result-stage{display:grid;gap:.3rem;padding:.9rem .95rem;border-radius:18px;border:1px solid rgba(188,199,210,.72);background:rgba(255,255,255,.84)}
.result-stage p,.result-signal-line{margin:0;font-size:.84rem;color:var(--color-text-light);line-height:1.45}
.result-driver-grid{gap:.75rem}
.result-driver,.result-gap{display:grid;gap:.75rem;padding:.95rem 1rem;border:1px solid rgba(188,199,210,.7);border-radius:18px;background:rgba(255,255,255,.86)}
.result-comparison{display:grid;gap:var(--spacing-md);padding:clamp(1.15rem,2.6vw,1.4rem);border-radius:var(--radius-lg);background:linear-gradient(180deg,rgba(255,255,255,.96),rgba(249,247,242,.94))}
.result-comparison__grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:12px}
.result-comparison__item{display:grid;gap:.7rem;padding:1rem;border:1px solid rgba(188,199,210,.74);border-radius:18px;background:rgba(255,255,255,.88)}
.result-comparison__item--active{border-color:rgba(20,124,131,.42);box-shadow:0 20px 42px -34px rgba(20,124,131,.9)}
.result-comparison__item p{margin:0;color:var(--color-text-muted)}
.result-gap__meta,.result-action-plan{display:grid;gap:.65rem}
.result-action-step{display:grid;gap:.65rem;padding:1rem 1.05rem;border:1px solid rgba(188,199,210,.7);border-radius:18px;background:rgba(255,255,255,.88)}
.result-action-step__head{display:grid;gap:.45rem}
.result-action-step__signal{margin:0;color:var(--color-text-muted)}
.result-checklist{padding-left:0;list-style:none}
.result-check{display:grid;grid-template-columns:18px 1fr;gap:.55rem;align-items:start;color:var(--color-text-muted)}
.result-check input{margin-top:.18rem;accent-color:var(--color-secondary)}
.result-split{grid-template-columns:repeat(auto-fit,minmax(240px,1fr))}
.result-detail-grid{grid-template-columns:repeat(auto-fit,minmax(180px,1fr))}
.result-gap__head{display:flex;justify-content:space-between;gap:var(--spacing-md);align-items:flex-start}
.result-gap__head p{margin-top:.3rem;color:var(--color-text-light);font-size:.86rem}
.result-gap__rank{display:inline-flex;margin-bottom:.35rem;color:var(--color-accent);font-size:.76rem;font-weight:800;letter-spacing:.09em;text-transform:uppercase}
.result-gap__decision-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:10px}
.result-gap__decision-card{display:grid;gap:.35rem;padding:.8rem .85rem;border-radius:16px;border:1px solid rgba(188,199,210,.66);background:rgba(250,248,244,.74)}
.result-gap__decision-card span{font-size:.74rem;font-weight:800;letter-spacing:.08em;text-transform:uppercase;color:var(--color-primary)}
.result-gap__decision-card p{font-size:.9rem;line-height:1.55;color:var(--color-text-muted)}
.result-headline{color:var(--color-primary);font-weight:700}
.result-callout--accent{border-color:rgba(20,124,131,.28);background:rgba(20,124,131,.06)}
.result-badge--decision{background:rgba(20,124,131,.12);color:var(--color-secondary-hover)}
.result-badge--meta{background:rgba(245,247,250,.92);color:var(--color-text)}
.result-empty,.result-empty-state{color:var(--color-text-light)}
.result-empty-state{padding:var(--spacing-xl);border-radius:var(--radius-lg);border:1px dashed rgba(188,199,210,.84);background:rgba(255,255,255,.82)}
@media (max-width:1100px){.result-overview,.result-workspace,.result-details{grid-template-columns:1fr}}
@media (max-width:820px){.result-stats,.result-stage-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.result-panel__head,.result-gap__head{flex-direction:column}.result-gap__decision-grid{grid-template-columns:1fr}}
@media (max-width:640px){.result-overview,.result-panel,.result-empty-state{padding:var(--spacing-md);border-radius:22px}.result-stats,.result-stage-grid{grid-template-columns:1fr}}
</style>
