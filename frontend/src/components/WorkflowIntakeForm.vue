<template>
  <form ref="formRef" class="workflow-intake" @submit.prevent="handleSubmit">
    <section class="workflow-intake__hero">
      <div class="workflow-intake__hero-copy">
        <div class="workflow-intake__eyebrow">{{ t('intake.eyebrow') }}</div>
        <h2>{{ t('intake.title') }}</h2>
        <p class="workflow-intake__lead">{{ t('intake.description') }}</p>

        <div class="workflow-intake__hero-actions">
          <button type="button" class="workflow-intake__hero-primary" @click="scrollToWorkspace">
            {{ t('intake.primaryCta') }}
          </button>
          <button type="button" class="workflow-intake__hero-secondary" @click="scrollToProof">
            {{ t('intake.secondaryCta') }}
          </button>
        </div>

        <ol class="workflow-intake__step-list">
          <li>{{ t('intake.stepOne') }}</li>
          <li>{{ t('intake.stepTwo') }}</li>
          <li>{{ t('intake.stepThree') }}</li>
        </ol>

        <div v-if="errorMessage" class="workflow-intake__banner" role="alert">
          <strong>{{ t('intake.bannerTitle') }}</strong>
          <span>{{ errorMessage }}</span>
        </div>
      </div>

      <aside class="workflow-intake__hero-panel">
        <span class="workflow-intake__panel-label">{{ t('intake.heroPanelEyebrow') }}</span>
        <h3>{{ t('intake.heroPanelTitle') }}</h3>
        <p>{{ t('intake.heroPanelBody') }}</p>

        <div class="workflow-intake__hero-stat-grid">
          <article
            v-for="item in outcomeHighlights"
            :key="item.title"
            class="workflow-intake__hero-stat"
          >
            <h4>{{ item.title }}</h4>
            <p>{{ item.body }}</p>
          </article>
        </div>
      </aside>
    </section>

    <section ref="proofSection" class="workflow-intake__proof-strip">
      <article
        v-for="(item, index) in heroHighlights"
        :key="item.title"
        class="workflow-intake__proof-card"
      >
        <span class="workflow-intake__proof-index">0{{ index + 1 }}</span>
        <h3>{{ item.title }}</h3>
        <p>{{ item.body }}</p>
      </article>
    </section>

    <section ref="workspaceSection" class="workflow-intake__workspace">
      <div class="workflow-intake__workspace-main">
        <div class="workflow-intake__workspace-copy">
          <span class="workflow-intake__section-kicker">{{ t('intake.workspaceEyebrow') }}</span>
          <h3>{{ t('intake.workspaceTitle') }}</h3>
          <p>{{ t('intake.workspaceBody') }}</p>
        </div>

        <article class="workflow-intake__document-card">
          <div class="workflow-intake__document-header">
            <div>
              <span class="workflow-intake__document-index">01</span>
              <h4>{{ t('intake.jobDescription') }}</h4>
              <p>{{ t('intake.jobDescriptionBody') }}</p>
            </div>
            <span class="workflow-intake__document-badge">{{ t('intake.primarySource') }}</span>
          </div>

          <label class="workflow-intake__field">
            <span class="workflow-intake__label">{{ t('intake.jobDescription') }}</span>
            <textarea
              :value="form.jobDescription"
              rows="11"
              :placeholder="t('intake.jobDescriptionPlaceholder')"
              @input="updateField('jobDescription', $event.target.value)"
              aria-describedby="jobDescription-error"
            ></textarea>
            <small id="jobDescription-error" class="workflow-intake__error" v-if="fieldError('jobDescription')">
              {{ fieldError('jobDescription') }}
            </small>
          </label>

          <div class="workflow-intake__upload">
            <div class="workflow-intake__upload-row">
              <div>
                <span class="workflow-intake__label">{{ t('intake.uploadJobDescription') }}</span>
                <small class="workflow-intake__hint">{{ t('intake.uploadHint') }}</small>
              </div>
              <button
                v-if="selectedFilename(form.jobDescriptionFile)"
                class="workflow-intake__upload-clear"
                type="button"
                @click="clearFile('jobDescriptionFile')"
              >
                {{ t('intake.removeFile') }}
              </button>
            </div>
            <input
              ref="jobDescriptionFileInput"
              type="file"
              accept=".pdf,.txt,.md"
              @change="handleFileChange('jobDescriptionFile', $event)"
            />
            <small class="workflow-intake__file-name" v-if="selectedFilename(form.jobDescriptionFile)">
              {{ t('intake.selectedFile', { name: selectedFilename(form.jobDescriptionFile) }) }}
            </small>
            <small class="workflow-intake__error" v-if="fieldError('jobDescriptionFile')">
              {{ fieldError('jobDescriptionFile') }}
            </small>
          </div>
        </article>

        <article class="workflow-intake__document-card">
          <div class="workflow-intake__document-header">
            <div>
              <span class="workflow-intake__document-index">02</span>
              <h4>{{ t('intake.candidateProfile') }}</h4>
              <p>{{ t('intake.candidateProfileBody') }}</p>
            </div>
            <span class="workflow-intake__document-badge">{{ t('intake.primarySource') }}</span>
          </div>

          <label class="workflow-intake__field">
            <span class="workflow-intake__label">{{ t('intake.candidateProfile') }}</span>
            <textarea
              :value="form.candidateProfile"
              rows="11"
              :placeholder="t('intake.candidateProfilePlaceholder')"
              @input="updateField('candidateProfile', $event.target.value)"
              aria-describedby="candidateProfile-error"
            ></textarea>
            <small id="candidateProfile-error" class="workflow-intake__error" v-if="fieldError('candidateProfile')">
              {{ fieldError('candidateProfile') }}
            </small>
          </label>

          <div class="workflow-intake__upload">
            <div class="workflow-intake__upload-row">
              <div>
                <span class="workflow-intake__label">{{ t('intake.uploadCandidateProfile') }}</span>
                <small class="workflow-intake__hint">{{ t('intake.uploadHint') }}</small>
              </div>
              <button
                v-if="selectedFilename(form.candidateProfileFile)"
                class="workflow-intake__upload-clear"
                type="button"
                @click="clearFile('candidateProfileFile')"
              >
                {{ t('intake.removeFile') }}
              </button>
            </div>
            <input
              ref="candidateProfileFileInput"
              type="file"
              accept=".pdf,.txt,.md"
              @change="handleFileChange('candidateProfileFile', $event)"
            />
            <small class="workflow-intake__file-name" v-if="selectedFilename(form.candidateProfileFile)">
              {{ t('intake.selectedFile', { name: selectedFilename(form.candidateProfileFile) }) }}
            </small>
            <small class="workflow-intake__error" v-if="fieldError('candidateProfileFile')">
              {{ fieldError('candidateProfileFile') }}
            </small>
          </div>
        </article>
      </div>

      <aside class="workflow-intake__workspace-sidebar">
        <div class="workflow-intake__sidebar-copy">
          <span class="workflow-intake__section-kicker">{{ t('intake.sidebarLabel') }}</span>
          <p>{{ t('intake.sidebarBody') }}</p>
        </div>

        <label class="workflow-intake__field">
          <span class="workflow-intake__label">{{ t('intake.targetRole') }}</span>
          <input
            type="text"
            :value="form.targetRole"
            :placeholder="t('intake.targetRolePlaceholder')"
            @input="updateField('targetRole', $event.target.value)"
          />
          <small class="workflow-intake__hint">{{ t('intake.optional') }}</small>
        </label>

        <label class="workflow-intake__field">
          <span class="workflow-intake__label">{{ t('intake.targetLevel') }}</span>
          <input
            type="text"
            :value="form.targetLevel"
            :placeholder="t('intake.targetLevelPlaceholder')"
            @input="updateField('targetLevel', $event.target.value)"
          />
          <small class="workflow-intake__hint">{{ t('intake.optional') }}</small>
        </label>

        <label class="workflow-intake__field">
          <span class="workflow-intake__label">{{ t('intake.companyName') }}</span>
          <input
            type="text"
            :value="form.companyName"
            :placeholder="t('intake.companyNamePlaceholder')"
            @input="updateField('companyName', $event.target.value)"
          />
          <small class="workflow-intake__hint">{{ t('intake.optional') }}</small>
        </label>

        <label class="workflow-intake__field">
          <span class="workflow-intake__label">{{ t('intake.focusAreas') }}</span>
          <textarea
            :value="form.focusAreasText"
            rows="5"
            :placeholder="t('intake.focusAreasPlaceholder')"
            @input="updateField('focusAreasText', $event.target.value)"
          ></textarea>
          <small class="workflow-intake__hint">{{ t('intake.optionalCommaSeparated') }}</small>
        </label>

        <div class="workflow-intake__deliverables">
          <span class="workflow-intake__label">{{ t('intake.deliverablesLabel') }}</span>
          <ul>
            <li>{{ t('intake.deliverableOne') }}</li>
            <li>{{ t('intake.deliverableTwo') }}</li>
            <li>{{ t('intake.deliverableThree') }}</li>
          </ul>
        </div>

        <label class="workflow-intake__toggle">
          <input
            type="checkbox"
            :checked="Boolean(form.includeCompanyResearch)"
            @change="updateField('includeCompanyResearch', $event.target.checked)"
          />
          <span>
            <strong>{{ t('intake.includeCompanyResearch') }}</strong>
            <small>{{ t('intake.includeCompanyResearchHint') }}</small>
          </span>
        </label>

        <label class="workflow-intake__toggle workflow-intake__toggle--compare">
          <input
            type="checkbox"
            :checked="Boolean(form.compareMode)"
            :disabled="hasSelectedFiles"
            @change="updateField('compareMode', $event.target.checked)"
          />
          <span>
            <strong>{{ t('intake.compareMode') }}</strong>
            <small>{{ hasSelectedFiles ? t('intake.compareModeDisabled') : t('intake.compareModeHint') }}</small>
          </span>
        </label>

        <section v-if="form.compareMode" class="workflow-intake__comparison">
          <div class="workflow-intake__comparison-head">
            <div>
              <span class="workflow-intake__label">{{ t('intake.comparisonTargets') }}</span>
              <small class="workflow-intake__hint">{{ t('intake.comparisonTargetsHint') }}</small>
            </div>
            <button
              type="button"
              class="workflow-intake__small-action"
              :disabled="comparisonTargets.length >= 5"
              @click="addComparisonTarget"
            >
              {{ t('intake.addComparisonTarget') }}
            </button>
          </div>

          <article
            v-for="(target, index) in comparisonTargets"
            :key="target.key"
            class="workflow-intake__comparison-card"
          >
            <div class="workflow-intake__comparison-card-head">
              <strong>{{ t('intake.comparisonTargetLabel', { index: index + 1 }) }}</strong>
              <button
                v-if="comparisonTargets.length > 2"
                type="button"
                class="workflow-intake__upload-clear"
                @click="removeComparisonTarget(index)"
              >
                {{ t('intake.removeComparisonTarget') }}
              </button>
            </div>

            <label class="workflow-intake__field">
              <span class="workflow-intake__label">{{ t('intake.targetRole') }}</span>
              <input
                type="text"
                :value="target.targetRole"
                :placeholder="t('intake.targetRolePlaceholder')"
                @input="updateComparisonTarget(index, 'targetRole', $event.target.value)"
              />
            </label>

            <label class="workflow-intake__field">
              <span class="workflow-intake__label">{{ t('intake.companyName') }}</span>
              <input
                type="text"
                :value="target.companyName"
                :placeholder="t('intake.companyNamePlaceholder')"
                @input="updateComparisonTarget(index, 'companyName', $event.target.value)"
              />
            </label>

            <label class="workflow-intake__field">
              <span class="workflow-intake__label">{{ t('intake.jobDescription') }}</span>
              <textarea
                :value="target.jobDescription"
                rows="5"
                :placeholder="t('intake.jobDescriptionPlaceholder')"
                @input="updateComparisonTarget(index, 'jobDescription', $event.target.value)"
              ></textarea>
            </label>
          </article>

          <small class="workflow-intake__error" v-if="fieldError('comparisonTargets') || fieldError('comparisonTargetsText')">
            {{ fieldError('comparisonTargets') || fieldError('comparisonTargetsText') }}
          </small>
        </section>

        <div v-if="isSubmitting" class="workflow-intake__progress" role="status" aria-live="polite">
          <strong>{{ t('intake.loadingTitle') }}</strong>
          <ol>
            <li>{{ t('intake.loadingParseJd') }}</li>
            <li>{{ t('intake.loadingAnalyzeCandidate') }}</li>
            <li>{{ t('intake.loadingRankGaps') }}</li>
            <li>{{ t('intake.loadingPrepPlan') }}</li>
          </ol>
        </div>

        <button class="workflow-intake__submit" type="submit" :disabled="isSubmitting">
          <span v-if="isSubmitting" class="workflow-intake__spinner" aria-hidden="true"></span>
          {{ isSubmitting ? t('intake.submitting') : t('intake.submit') }}
        </button>

        <p class="workflow-intake__footnote">
          {{ t('intake.submitFootnote') }}
        </p>
      </aside>
    </section>
  </form>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  },
  isSubmitting: {
    type: Boolean,
    default: false
  },
  errorMessage: {
    type: String,
    default: ''
  },
  fieldErrors: {
    type: [Object, Array],
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])
const { t } = useI18n()
const jobDescriptionFileInput = ref(null)
const candidateProfileFileInput = ref(null)
const proofSection = ref(null)
const workspaceSection = ref(null)
const formRef = ref(null)

const heroHighlights = computed(() => [
  { title: t('intake.proofTitle'), body: t('intake.proofBody') },
  { title: t('intake.gapTitle'), body: t('intake.gapBody') },
  { title: t('intake.prepTitle'), body: t('intake.prepBody') }
])

const outcomeHighlights = computed(() => [
  { title: t('intake.heroStatVerdictTitle'), body: t('intake.heroStatVerdictBody') },
  { title: t('intake.heroStatGapTitle'), body: t('intake.heroStatGapBody') },
  { title: t('intake.heroStatPrepTitle'), body: t('intake.heroStatPrepBody') }
])

const form = computed(() => ({
  jobDescription: '',
  candidateProfile: '',
  targetRole: '',
  targetLevel: '',
  companyName: '',
  focusAreasText: '',
  includeCompanyResearch: false,
  compareMode: false,
  comparisonTargets: [
    { targetRole: '', companyName: '', jobDescription: '' },
    { targetRole: '', companyName: '', jobDescription: '' }
  ],
  comparisonTargetsText: '',
  jobDescriptionFile: null,
  candidateProfileFile: null,
  ...props.modelValue
}))

const hasSelectedFiles = computed(() => Boolean(selectedFilename(form.value.jobDescriptionFile) || selectedFilename(form.value.candidateProfileFile)))
const comparisonTargets = computed(() => {
  const targets = Array.isArray(form.value.comparisonTargets) ? form.value.comparisonTargets : []
  return targets.map((target, index) => ({
    key: target?.key || `comparison-target-${index}`,
    targetRole: target?.targetRole || '',
    companyName: target?.companyName || '',
    jobDescription: target?.jobDescription || ''
  }))
})

function getFieldError(field) {
  const errors = props.fieldErrors

  if (Array.isArray(errors)) {
    const match = errors.find((entry) => entry?.field === field)
    return match?.message || ''
  }

  if (errors && typeof errors === 'object') {
    const value = errors[field]
    if (Array.isArray(value)) {
      return value[0] || ''
    }
    return value || ''
  }

  return ''
}

function fieldError(field) {
  return getFieldError(field)
}

function updateField(field, value) {
  emit('update:modelValue', {
    ...form.value,
    [field]: value
  })
}

function updateComparisonTargets(nextTargets) {
  updateField('comparisonTargets', nextTargets.map(({ key, ...target }) => target))
}

function updateComparisonTarget(index, field, value) {
  const nextTargets = comparisonTargets.value.map((target, targetIndex) => {
    if (targetIndex !== index) return target
    return { ...target, [field]: value }
  })
  updateComparisonTargets(nextTargets)
}

function addComparisonTarget() {
  if (comparisonTargets.value.length >= 5) {
    return
  }
  updateComparisonTargets([
    ...comparisonTargets.value,
    { targetRole: '', companyName: '', jobDescription: '' }
  ])
}

function removeComparisonTarget(index) {
  if (comparisonTargets.value.length <= 2) {
    return
  }
  updateComparisonTargets(comparisonTargets.value.filter((_, targetIndex) => targetIndex !== index))
}

function handleFileChange(field, event) {
  const file = event?.target?.files?.[0] ?? null
  updateField(field, file)
}

function clearFile(field) {
  if (field === 'jobDescriptionFile' && jobDescriptionFileInput.value) {
    jobDescriptionFileInput.value.value = ''
  }

  if (field === 'candidateProfileFile' && candidateProfileFileInput.value) {
    candidateProfileFileInput.value.value = ''
  }

  updateField(field, null)
}

function selectedFilename(file) {
  return file?.name || ''
}

function normalizeText(value) {
  return typeof value === 'string' ? value.trim() : value
}

function buildPayload() {
  return {
    ...form.value,
    targetRole: normalizeText(form.value.targetRole) || null,
    targetLevel: normalizeText(form.value.targetLevel) || null,
    companyName: normalizeText(form.value.companyName) || null,
    jobDescription: normalizeText(form.value.jobDescription),
    candidateProfile: normalizeText(form.value.candidateProfile),
    includeCompanyResearch: Boolean(form.value.includeCompanyResearch),
    comparisonTargets: comparisonTargets.value.map(({ key, ...target }) => target),
    jobDescriptionFile: form.value.jobDescriptionFile ?? null,
    candidateProfileFile: form.value.candidateProfileFile ?? null
  }
}

function handleSubmit() {
  emit('submit', buildPayload())
}

function scrollToWorkspace() {
  workspaceSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function scrollToProof() {
  proofSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

watch(
  () => props.fieldErrors,
  async () => {
    await nextTick()
    const firstError = formRef.value?.querySelector('.workflow-intake__error')
    firstError?.closest('label, section, article, div')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  },
  { deep: true }
)
</script>

<style scoped>
.workflow-intake {
  display: grid;
  gap: var(--spacing-xl);
}

.workflow-intake__hero,
.workflow-intake__proof-card,
.workflow-intake__document-card,
.workflow-intake__comparison-card,
.workflow-intake__workspace-sidebar {
  border: 1px solid rgba(188, 199, 210, 0.82);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-md);
}

.workflow-intake__comparison {
  display: grid;
  gap: 0.9rem;
}

.workflow-intake__comparison-head,
.workflow-intake__comparison-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.8rem;
}

.workflow-intake__comparison-card {
  display: grid;
  gap: 0.85rem;
  padding: 1rem;
  border-radius: 20px;
}

.workflow-intake__small-action {
  min-height: 36px;
  padding: 0.52rem 0.8rem;
  border-radius: var(--radius-full);
  background: rgba(16, 38, 61, 0.08);
  color: var(--color-primary);
  font-weight: 800;
}

.workflow-intake__small-action:disabled {
  cursor: not-allowed;
  opacity: 0.48;
}

.workflow-intake__hero {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.88fr);
  gap: var(--spacing-xl);
  padding: clamp(1.6rem, 4vw, 2.5rem);
  border-radius: var(--radius-xl);
  background:
    radial-gradient(circle at top right, rgba(20, 124, 131, 0.08), transparent 28%),
    radial-gradient(circle at bottom left, rgba(186, 124, 55, 0.09), transparent 24%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 246, 241, 0.96));
}

.workflow-intake__eyebrow,
.workflow-intake__section-kicker,
.workflow-intake__panel-label {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0.28rem 0.74rem;
  border-radius: var(--radius-full);
  background: rgba(16, 38, 61, 0.08);
  color: var(--color-primary);
  font-size: 0.74rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.workflow-intake__hero-copy {
  display: grid;
  align-content: start;
  gap: var(--spacing-md);
}

.workflow-intake__lead {
  max-width: 60ch;
  color: var(--color-text-muted);
  font-size: 1.08rem;
  line-height: 1.62;
}

.workflow-intake__hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding-top: 4px;
}

.workflow-intake__hero-primary,
.workflow-intake__hero-secondary {
  min-height: 50px;
  padding: 0.88rem 1.1rem;
  border-radius: 18px;
  font-weight: 700;
  transition:
    transform var(--transition-fast),
    box-shadow var(--transition-normal),
    border-color var(--transition-normal);
}

.workflow-intake__hero-primary {
  background: linear-gradient(135deg, var(--color-primary), #173552);
  color: #ffffff;
  box-shadow: 0 28px 54px -36px rgba(16, 38, 61, 0.78);
}

.workflow-intake__hero-secondary {
  border: 1px solid rgba(188, 199, 210, 0.82);
  background: rgba(255, 255, 255, 0.8);
  color: var(--color-primary);
}

.workflow-intake__hero-primary:hover,
.workflow-intake__hero-secondary:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.workflow-intake__step-list {
  display: grid;
  gap: 0.7rem;
  padding-left: 1.15rem;
  color: var(--color-text-muted);
}

.workflow-intake__step-list li::marker {
  color: var(--color-secondary);
  font-weight: 700;
}

.workflow-intake__banner {
  display: grid;
  gap: 0.2rem;
  padding: var(--spacing-md);
  border-radius: 18px;
  border: 1px solid rgba(180, 72, 72, 0.2);
  background: rgba(249, 236, 235, 0.9);
  color: var(--color-error);
}

.workflow-intake__hero-panel {
  display: grid;
  align-content: start;
  gap: 12px;
  padding: 1.45rem;
  border-radius: calc(var(--radius-lg) + 2px);
  border: 1px solid rgba(188, 199, 210, 0.76);
  background:
    linear-gradient(180deg, rgba(247, 249, 252, 0.94), rgba(255, 255, 255, 0.96));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.74);
}

.workflow-intake__hero-panel p {
  color: var(--color-text-muted);
}

.workflow-intake__hero-stat-grid {
  display: grid;
  gap: 12px;
  margin-top: 4px;
}

.workflow-intake__hero-stat {
  padding: 1rem 1.05rem;
  border-radius: 18px;
  border: 1px solid rgba(188, 199, 210, 0.7);
  background: rgba(255, 255, 255, 0.88);
}

.workflow-intake__hero-stat h4 {
  font-size: 0.96rem;
}

.workflow-intake__hero-stat p {
  margin-top: 0.35rem;
  font-size: 0.92rem;
  line-height: 1.52;
}

.workflow-intake__proof-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--spacing-md);
}

.workflow-intake__proof-card {
  padding: 1.3rem 1.28rem;
  border-radius: calc(var(--radius-lg) - 2px);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(249, 247, 242, 0.94));
}

.workflow-intake__proof-index {
  display: inline-flex;
  margin-bottom: 0.7rem;
  color: var(--color-accent);
  font-size: 0.76rem;
  font-weight: 800;
  letter-spacing: 0.14em;
}

.workflow-intake__proof-card p {
  margin-top: 0.45rem;
  color: var(--color-text-muted);
}

.workflow-intake__workspace {
  display: grid;
  grid-template-columns: minmax(0, 1.75fr) minmax(320px, 0.95fr);
  gap: var(--spacing-lg);
}

.workflow-intake__workspace-main {
  display: grid;
  gap: var(--spacing-lg);
}

.workflow-intake__workspace-copy {
  display: grid;
  gap: 10px;
}

.workflow-intake__workspace-copy p {
  max-width: 68ch;
  color: var(--color-text-muted);
}

.workflow-intake__document-card {
  display: grid;
  gap: var(--spacing-lg);
  padding: clamp(1.2rem, 2.6vw, 1.5rem);
  border-radius: var(--radius-lg);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(249, 247, 242, 0.94));
}

.workflow-intake__document-header {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-md);
  align-items: flex-start;
}

.workflow-intake__document-header p {
  margin-top: 0.35rem;
  color: var(--color-text-muted);
}

.workflow-intake__document-index {
  display: inline-flex;
  margin-bottom: 0.45rem;
  color: var(--color-text-light);
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.14em;
}

.workflow-intake__document-badge {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0.35rem 0.72rem;
  border-radius: var(--radius-full);
  background: rgba(20, 124, 131, 0.08);
  color: var(--color-secondary-hover);
  font-size: 0.78rem;
  font-weight: 700;
  white-space: nowrap;
}

.workflow-intake__workspace-sidebar {
  position: sticky;
  top: 114px;
  align-self: start;
  display: grid;
  gap: var(--spacing-lg);
  padding: clamp(1.2rem, 2.5vw, 1.5rem);
  border-radius: var(--radius-lg);
  background:
    radial-gradient(circle at top right, rgba(20, 124, 131, 0.08), transparent 28%),
    linear-gradient(180deg, rgba(250, 248, 244, 0.98), rgba(255, 255, 255, 0.98));
}

.workflow-intake__sidebar-copy {
  display: grid;
  gap: 10px;
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid rgba(188, 199, 210, 0.64);
}

.workflow-intake__sidebar-copy p,
.workflow-intake__footnote {
  color: var(--color-text-muted);
}

.workflow-intake__field {
  display: grid;
  gap: 0.5rem;
}

.workflow-intake__label {
  font-weight: 700;
  color: var(--color-primary);
}

.workflow-intake__field textarea,
.workflow-intake__field input[type='text'] {
  width: 100%;
  padding: 1rem 1.05rem;
  border: 1px solid rgba(188, 199, 210, 0.78);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  color: var(--color-text);
  line-height: 1.58;
  transition:
    border-color var(--transition-normal),
    box-shadow var(--transition-normal),
    background-color var(--transition-normal);
}

.workflow-intake__field textarea:focus,
.workflow-intake__field input[type='text']:focus,
.workflow-intake__upload input[type='file']:focus-visible {
  border-color: rgba(20, 124, 131, 0.72);
  box-shadow: 0 0 0 4px rgba(20, 124, 131, 0.08);
}

.workflow-intake__field textarea {
  resize: vertical;
  min-height: 220px;
}

.workflow-intake__hint,
.workflow-intake__file-name {
  color: var(--color-text-light);
  font-size: 0.84rem;
}

.workflow-intake__error {
  color: var(--color-error);
  font-size: 0.84rem;
}

.workflow-intake__upload {
  display: grid;
  gap: 0.65rem;
  padding: 1rem;
  border: 1px dashed rgba(20, 124, 131, 0.36);
  border-radius: 18px;
  background: rgba(20, 124, 131, 0.04);
}

.workflow-intake__upload-row {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-md);
  align-items: flex-start;
}

.workflow-intake__upload-clear {
  color: var(--color-secondary-hover);
  font-weight: 700;
  white-space: nowrap;
}

.workflow-intake__deliverables {
  display: grid;
  gap: 0.7rem;
  padding: 1rem;
  border: 1px solid rgba(188, 199, 210, 0.74);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.84);
}

.workflow-intake__deliverables ul {
  padding-left: 1.15rem;
  display: grid;
  gap: 0.55rem;
  color: var(--color-text-muted);
}

.workflow-intake__toggle {
  display: flex;
  gap: var(--spacing-sm);
  align-items: flex-start;
  padding: 0.2rem 0;
}

.workflow-intake__toggle input {
  margin-top: 0.25rem;
}

.workflow-intake__toggle small {
  display: block;
  margin-top: 0.2rem;
  color: var(--color-text-light);
}

.workflow-intake__submit {
  min-height: 54px;
  border-radius: 18px;
  background: linear-gradient(135deg, var(--color-primary), #173552);
  color: #ffffff;
  font-weight: 700;
  box-shadow: 0 28px 54px -38px rgba(16, 38, 61, 0.8);
  transition:
    transform var(--transition-fast),
    box-shadow var(--transition-normal),
    filter var(--transition-normal);
}

.workflow-intake__submit:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: saturate(1.03);
}

.workflow-intake__progress {
  display: grid;
  gap: 0.7rem;
  padding: 1rem;
  border-radius: 18px;
  border: 1px solid rgba(20, 124, 131, 0.22);
  background: linear-gradient(135deg, rgba(20, 124, 131, 0.08), rgba(255, 255, 255, 0.88));
  color: var(--color-text);
}

.workflow-intake__progress strong {
  color: var(--color-primary);
}

.workflow-intake__progress ol {
  display: grid;
  gap: 0.45rem;
  margin: 0;
  padding-left: 1.2rem;
  color: var(--color-text-muted);
  font-size: 0.9rem;
  line-height: 1.45;
}

.workflow-intake__spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  margin-right: 0.5rem;
  border: 2px solid rgba(255, 255, 255, 0.45);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: workflow-intake-spin 0.8s linear infinite;
}

@keyframes workflow-intake-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1040px) {
  .workflow-intake__hero,
  .workflow-intake__workspace {
    grid-template-columns: 1fr;
  }

  .workflow-intake__proof-strip {
    grid-template-columns: 1fr;
  }

  .workflow-intake__workspace-sidebar {
    position: static;
  }
}

@media (max-width: 640px) {
  .workflow-intake__hero,
  .workflow-intake__document-card,
  .workflow-intake__workspace-sidebar {
    padding: var(--spacing-md);
    border-radius: 22px;
  }

  .workflow-intake__hero-actions,
  .workflow-intake__document-header,
  .workflow-intake__upload-row {
    flex-direction: column;
  }

  .workflow-intake__hero-primary,
  .workflow-intake__hero-secondary {
    width: 100%;
  }
}
</style>
