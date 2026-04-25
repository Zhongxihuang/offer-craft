<template>
  <div class="app-container">
    <ChatHeader
      :mode="ui.mode"
      :is-chat-streaming="chat.isStreaming"
      :is-workflow-busy="ui.isSubmittingWorkflow || ui.isLoadingWorkflow"
      :current-locale="currentLocale"
      @select-mode="handleSelectMode"
      @primary-action="handlePrimaryAction"
      @change-locale="handleChangeLocale"
    />

    <main class="app-main">
      <section class="app-shell">
        <div v-if="ui.mode === 'workflow'" class="workflow-shell">
          <div v-if="ui.isLoadingWorkflow" class="status-card">
            <span class="status-card__eyebrow">{{ t('app.restoringEyebrow') }}</span>
            <h2>{{ t('app.restoringTitle') }}</h2>
            <p>{{ t('app.restoringBody') }}</p>
          </div>

          <WorkflowIntakeForm
            v-else-if="ui.workflowView === 'form'"
            :model-value="workflowForm"
            :is-submitting="ui.isSubmittingWorkflow"
            :error-message="ui.workflowError || ''"
            :field-errors="fieldErrors"
            @update:modelValue="updateWorkflowForm"
            @submit="handleWorkflowSubmit"
          />

          <WorkflowResultView
            v-else
            :workflow-result="workflowResult"
            :workflow-versions="workflowVersions"
            :workflow-notice="workflowNotice"
            @start-new-analysis="handleStartNewAnalysis"
            @open-support-chat="handleOpenSupportChat"
          />
        </div>

        <SupportChatPanel
          v-else
          :messages="chat.messages"
          :is-streaming="chat.isStreaming"
          :prefill-text="chat.prefillText"
          :preferred-mode="chat.prefillMode"
          :workflow-context="activeWorkflowChatContext"
          @send="handleSendMessage"
          @retry="handleRetry"
          @prefill-consumed="handlePrefillConsumed"
        />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import ChatHeader from './components/ChatHeader.vue'
import SupportChatPanel from './components/SupportChatPanel.vue'
import WorkflowIntakeForm from './components/WorkflowIntakeForm.vue'
import WorkflowResultView from './components/WorkflowResultView.vue'
import { createChatStream, closeChatStream } from './api/chat'
import {
  analyzeCareerWorkflow,
  analyzeCareerWorkflowUpload,
  getCareerWorkflow,
  getCareerWorkflowVersions,
  refineCareerWorkflow,
  normalizeApiError
} from './api/careerWorkflow'
import { normalizeLocale as normalizePreferredLocale, setPreferredLocale } from './i18n'

const MEMORY_STORAGE_KEY = 'careerAgent_memoryId'
const WORKFLOW_ID_STORAGE_KEY = 'careerAgent_currentWorkflowId'
const WORKFLOW_META_STORAGE_KEY = 'careerAgent_workflowMeta'
const MAX_MEMORY_ID = 2147483647
const { locale, t } = useI18n()

const ui = reactive({
  mode: 'workflow',
  workflowView: 'form',
  isSubmittingWorkflow: false,
  isLoadingWorkflow: false,
  workflowError: null
})

const workflowForm = reactive(createInitialWorkflowForm())

const fieldErrors = reactive({})
const workflowResult = ref(null)
const currentWorkflowId = ref(null)
const workflowMeta = ref(null)
const workflowVersions = ref([])
const workflowNotice = ref('')
const currentLocale = computed(() => normalizePreferredLocale(locale.value))

const chat = reactive({
  memoryId: null,
  messages: [],
  isStreaming: false,
  currentEventSource: null,
  lastUserMessage: '',
  prefillText: '',
  prefillMode: 'ask',
  linkedWorkflowId: null
})

const activeWorkflowChatContext = computed(() => {
  if (!chat.linkedWorkflowId) {
    return null
  }

  return {
    workflowId: chat.linkedWorkflowId,
    generatedAt: workflowResult.value?.generatedAt ?? workflowMeta.value?.generatedAt ?? null,
    roleTitle: workflowResult.value?.jdAnalysis?.roleTitle ?? workflowMeta.value?.roleTitle ?? null,
    companyName: workflowMeta.value?.companyName ?? null,
    fitLevel: workflowResult.value?.decisionSummary?.fitLevel ?? null,
    positioning: workflowResult.value?.decisionSummary?.recommendedPositioning ?? null,
    contentLocale: workflowResult.value?.contentLocale ?? workflowMeta.value?.contentLocale ?? 'en',
    versionNumber: workflowResult.value?.versionInfo?.versionNumber ?? workflowMeta.value?.versionNumber ?? null,
    overallConfidence: workflowResult.value?.confidenceSummary?.overallConfidence ?? null
  }
})

function generateMemoryId() {
  return Math.floor(Math.random() * (MAX_MEMORY_ID - 1)) + 1
}

function isValidMemoryId(value) {
  return Number.isInteger(value) && value > 0 && value <= MAX_MEMORY_ID
}

function initMemoryId() {
  const stored = window.localStorage.getItem(MEMORY_STORAGE_KEY)
  const parsed = Number.parseInt(stored ?? '', 10)

  if (isValidMemoryId(parsed)) {
    chat.memoryId = parsed
    return
  }

  chat.memoryId = generateMemoryId()
  window.localStorage.setItem(MEMORY_STORAGE_KEY, String(chat.memoryId))
}

function replaceFieldErrors(nextErrors = {}) {
  Object.keys(fieldErrors).forEach((key) => {
    delete fieldErrors[key]
  })

  Object.entries(nextErrors).forEach(([key, value]) => {
    if (value) {
      fieldErrors[key] = value
    }
  })
}

function clearWorkflowErrors() {
  ui.workflowError = null
  replaceFieldErrors({})
}

function clearWorkflowNotice() {
  workflowNotice.value = ''
}

function localizedFallback(english, chinese) {
  return currentLocale.value === 'zh-CN' ? chinese : english
}

function updateWorkflowForm(nextValue) {
  Object.assign(workflowForm, nextValue)
}

function createInitialWorkflowForm() {
  return {
    targetRole: '',
    targetLevel: '',
    companyName: '',
    jobDescription: '',
    candidateProfile: '',
    focusAreasText: '',
    includeCompanyResearch: false,
    jobDescriptionFile: null,
    candidateProfileFile: null
  }
}

function persistWorkflowState(response) {
  currentWorkflowId.value = response?.workflowId ?? null

  if (!currentWorkflowId.value) {
    return
  }

  workflowMeta.value = {
    workflowId: currentWorkflowId.value,
    generatedAt: response?.generatedAt ?? null,
    roleTitle: response?.jdAnalysis?.roleTitle ?? workflowForm.targetRole ?? null,
    companyName: workflowForm.companyName || null,
    contentLocale: normalizePreferredLocale(response?.contentLocale ?? currentLocale.value),
    versionNumber: response?.versionInfo?.versionNumber ?? 1
  }

  window.localStorage.setItem(WORKFLOW_ID_STORAGE_KEY, currentWorkflowId.value)
  window.localStorage.setItem(WORKFLOW_META_STORAGE_KEY, JSON.stringify(workflowMeta.value))
}

function clearStoredWorkflow() {
  currentWorkflowId.value = null
  workflowMeta.value = null
  workflowVersions.value = []
  window.localStorage.removeItem(WORKFLOW_ID_STORAGE_KEY)
  window.localStorage.removeItem(WORKFLOW_META_STORAGE_KEY)
}

function attachWorkflowToChat(workflowId, { resetSessionIfChanged = false } = {}) {
  const normalizedWorkflowId = normalizeOptionalString(workflowId)

  if (!normalizedWorkflowId) {
    chat.linkedWorkflowId = null
    return
  }

  if (resetSessionIfChanged && chat.linkedWorkflowId && chat.linkedWorkflowId !== normalizedWorkflowId) {
    resetChatSession(normalizedWorkflowId)
    return
  }

  chat.linkedWorkflowId = normalizedWorkflowId
}

function buildWorkflowPayload(formPayload) {
  const focusAreas = String(formPayload.focusAreasText ?? '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)

  const payload = {
    memoryId: chat.memoryId,
    jobDescription: formPayload.jobDescription,
    candidateProfile: formPayload.candidateProfile,
    includeCompanyResearch: Boolean(formPayload.includeCompanyResearch),
    locale: currentLocale.value
  }

  const targetRole = normalizeOptionalString(formPayload.targetRole)
  const targetLevel = normalizeOptionalString(formPayload.targetLevel)
  const companyName = normalizeOptionalString(formPayload.companyName)

  if (targetRole) {
    payload.targetRole = targetRole
  }

  if (targetLevel) {
    payload.targetLevel = targetLevel
  }

  if (companyName) {
    payload.companyName = companyName
  }

  if (focusAreas.length > 0) {
    payload.focusAreas = focusAreas
  }

  return payload
}

function buildWorkflowUploadFormData(formPayload) {
  const formData = new FormData()
  const focusAreas = String(formPayload.focusAreasText ?? '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .join(',')

  formData.append('memoryId', String(chat.memoryId))
  formData.append('includeCompanyResearch', String(Boolean(formPayload.includeCompanyResearch)))
  formData.append('locale', currentLocale.value)

  appendOptionalFormValue(formData, 'workflowId', currentWorkflowId.value)
  appendOptionalFormValue(formData, 'targetRole', formPayload.targetRole)
  appendOptionalFormValue(formData, 'targetLevel', formPayload.targetLevel)
  appendOptionalFormValue(formData, 'companyName', formPayload.companyName)
  appendOptionalFormValue(formData, 'focusAreas', focusAreas)
  appendOptionalFormValue(formData, 'jobDescriptionText', formPayload.jobDescription)
  appendOptionalFormValue(formData, 'candidateProfileText', formPayload.candidateProfile)

  if (isWorkflowFile(formPayload.jobDescriptionFile)) {
    formData.append('jobDescriptionFile', formPayload.jobDescriptionFile)
  }

  if (isWorkflowFile(formPayload.candidateProfileFile)) {
    formData.append('candidateProfileFile', formPayload.candidateProfileFile)
  }

  return formData
}

function appendOptionalFormValue(formData, fieldName, value) {
  const normalized = normalizeOptionalString(value)

  if (normalized) {
    formData.append(fieldName, normalized)
  }
}

function normalizeOptionalString(value) {
  return typeof value === 'string' && value.trim() ? value.trim() : null
}

function hasSelectedWorkflowFiles(formPayload) {
  return isWorkflowFile(formPayload.jobDescriptionFile) || isWorkflowFile(formPayload.candidateProfileFile)
}

function isWorkflowFile(value) {
  if (!value || typeof value !== 'object') {
    return false
  }

  return typeof value.name === 'string' && value.name.trim().length > 0
}

function clearWorkflowFiles() {
  workflowForm.jobDescriptionFile = null
  workflowForm.candidateProfileFile = null
}

async function restoreWorkflowIfNeeded() {
  const storedWorkflowId = window.localStorage.getItem(WORKFLOW_ID_STORAGE_KEY)
  const storedMeta = window.localStorage.getItem(WORKFLOW_META_STORAGE_KEY)

  if (storedMeta) {
    try {
      workflowMeta.value = JSON.parse(storedMeta)
      if (workflowMeta.value && typeof workflowMeta.value === 'object') {
        workflowMeta.value.contentLocale = normalizePreferredLocale(workflowMeta.value.contentLocale)
      }
    } catch (error) {
      workflowMeta.value = null
      window.localStorage.removeItem(WORKFLOW_META_STORAGE_KEY)
    }
  }

  if (!storedWorkflowId) {
    ui.workflowView = 'form'
    return
  }

  ui.isLoadingWorkflow = true
  currentWorkflowId.value = storedWorkflowId

  try {
    const response = await getCareerWorkflow(storedWorkflowId)
    workflowResult.value = response
    persistWorkflowState(response)
    await loadWorkflowVersions(response.workflowId)
    attachWorkflowToChat(response.workflowId)
    ui.workflowView = 'result'
  } catch (error) {
    const normalizedError = normalizeApiError(error)

    if (normalizedError.status === 404) {
      clearStoredWorkflow()
      ui.workflowError = normalizedError.message
    } else {
      ui.workflowError = t('app.restoreFailed')
    }

    ui.workflowView = 'form'
  } finally {
    ui.isLoadingWorkflow = false
  }
}

async function loadWorkflowVersions(workflowId) {
  const normalizedWorkflowId = normalizeOptionalString(workflowId)

  if (!normalizedWorkflowId) {
    workflowVersions.value = []
    return
  }

  try {
    workflowVersions.value = await getCareerWorkflowVersions(normalizedWorkflowId)
  } catch (error) {
    workflowVersions.value = []
  }
}

function handleSelectMode(mode) {
  if (mode === 'chat' && currentWorkflowId.value) {
    attachWorkflowToChat(currentWorkflowId.value)
  }

  if (!isValidMemoryId(chat.memoryId)) {
    chat.memoryId = generateMemoryId()
    window.localStorage.setItem(MEMORY_STORAGE_KEY, String(chat.memoryId))
  }

  ui.mode = mode
}

function handleChangeLocale(nextLocale) {
  setPreferredLocale(nextLocale)
}

function handlePrimaryAction() {
  if (ui.mode === 'chat') {
    handleNewChat()
    return
  }

  handleStartNewAnalysis()
}

function handleStartNewAnalysis() {
  workflowResult.value = null
  clearStoredWorkflow()
  clearWorkflowErrors()
  clearWorkflowNotice()
  clearWorkflowFiles()
  chat.linkedWorkflowId = null
  chat.prefillText = ''
  chat.prefillMode = 'ask'
  ui.workflowView = 'form'
  ui.mode = 'workflow'
}

function handleOpenSupportChat(payload) {
  const prompt = typeof payload === 'string'
    ? payload
    : typeof payload?.prompt === 'string'
      ? payload.prompt
      : t('app.defaultFollowUp')
  const mode = typeof payload === 'object' && payload?.mode === 'refine' ? 'refine' : 'ask'

  attachWorkflowToChat(currentWorkflowId.value ?? workflowResult.value?.workflowId ?? null)
  chat.prefillText = prompt
  chat.prefillMode = mode
  ui.mode = 'chat'
}

function handlePrefillConsumed() {
  chat.prefillText = ''
}

async function handleWorkflowSubmit(formPayload) {
  clearWorkflowErrors()
  clearWorkflowNotice()
  ui.isSubmittingWorkflow = true

  if (!isValidMemoryId(chat.memoryId)) {
    chat.memoryId = generateMemoryId()
    window.localStorage.setItem(MEMORY_STORAGE_KEY, String(chat.memoryId))
  }

  try {
    const response = hasSelectedWorkflowFiles(formPayload)
      ? await analyzeCareerWorkflowUpload(buildWorkflowUploadFormData(formPayload))
      : await analyzeCareerWorkflow(buildWorkflowPayload(formPayload))
    workflowResult.value = response
    persistWorkflowState(response)
    await loadWorkflowVersions(response.workflowId)
    clearWorkflowFiles()
    attachWorkflowToChat(response.workflowId, { resetSessionIfChanged: true })
    ui.workflowView = 'result'
  } catch (error) {
    const normalizedError = normalizeApiError(error)
    replaceFieldErrors(normalizedError.fieldErrors)
    ui.workflowError = normalizedError.message
    ui.workflowView = 'form'
  } finally {
    ui.isSubmittingWorkflow = false
  }
}

function handleNewChat() {
  resetChatSession(currentWorkflowId.value)
  ui.mode = 'chat'
}

async function handleSendMessage(payload) {
  const messageText = typeof payload === 'string'
    ? payload
    : typeof payload?.message === 'string'
      ? payload.message
      : ''
  const messageMode = typeof payload === 'object' && payload?.mode === 'refine' ? 'refine' : 'ask'

  if (!messageText?.trim() || chat.isStreaming) {
    return
  }

  if (!isValidMemoryId(chat.memoryId)) {
    chat.memoryId = generateMemoryId()
    window.localStorage.setItem(MEMORY_STORAGE_KEY, String(chat.memoryId))
  }

  chat.lastUserMessage = messageText

  chat.messages.push({
    id: Date.now(),
    role: 'user',
    content: messageText,
    timestamp: new Date(),
    isStreaming: false,
    isError: false
  })

  if (messageMode === 'refine' && chat.linkedWorkflowId) {
    await handleRefineRequest(messageText)
    return
  }

  chat.messages.push({
    id: Date.now() + 1,
    role: 'assistant',
    content: '',
    timestamp: new Date(),
    isStreaming: true,
    isError: false
  })

  chat.isStreaming = true

  try {
    chat.currentEventSource = createChatStream(chat.memoryId, messageText, {
      onMessage: (chunk) => {
        const lastMessage = chat.messages[chat.messages.length - 1]

        if (lastMessage?.role === 'assistant') {
          lastMessage.content += chunk
        }
      },
      onError: (error) => {
        const lastMessage = chat.messages[chat.messages.length - 1]

        if (lastMessage?.role === 'assistant') {
          lastMessage.isStreaming = false
          lastMessage.isError = true
          lastMessage.content = error.message || t('api.connectionLost')
        }

        chat.isStreaming = false
        chat.currentEventSource = null
      },
      onComplete: () => {
        const lastMessage = chat.messages[chat.messages.length - 1]

        if (lastMessage?.role === 'assistant') {
          lastMessage.isStreaming = false
        }

        chat.isStreaming = false
        chat.currentEventSource = null
      }
    }, {
      workflowId: chat.linkedWorkflowId,
      locale: currentLocale.value,
      errorMessage: t('api.connectionLost')
    })
  } catch (error) {
    const lastMessage = chat.messages[chat.messages.length - 1]

    if (lastMessage?.role === 'assistant') {
      lastMessage.isStreaming = false
      lastMessage.isError = true
      lastMessage.content = t('api.connectFailed')
    }

    chat.isStreaming = false
    chat.currentEventSource = null
  }
}

async function handleRefineRequest(messageText) {
  try {
    const refineResponse = await refineCareerWorkflow(chat.linkedWorkflowId, {
      memoryId: chat.memoryId,
      locale: currentLocale.value,
      action: 'AUTO',
      message: messageText
    })

    if (refineResponse?.assistantMessage) {
      chat.messages.push({
        id: Date.now() + 1,
        role: 'assistant',
        content: refineResponse.assistantMessage,
        timestamp: new Date(),
        isStreaming: false,
        isError: false
      })
    }

    if (refineResponse?.workflow) {
      workflowResult.value = refineResponse.workflow
      persistWorkflowState(refineResponse.workflow)
      attachWorkflowToChat(refineResponse.workflow.workflowId)
      workflowVersions.value = Array.isArray(refineResponse.versions) ? refineResponse.versions : []
      workflowNotice.value = refineResponse.assistantMessage || localizedFallback(
        'Updated the workflow after your follow-up and saved a new version.',
        '系统已根据你的追问更新分析结果，并保存为新版本。'
      )
      ui.workflowView = 'result'
      ui.mode = 'workflow'
    }
  } catch (error) {
    const normalizedError = normalizeApiError(error)
    chat.messages.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: normalizedError.message || t('api.unexpected'),
      timestamp: new Date(),
      isStreaming: false,
      isError: true
    })
  }
}

function handleRetry() {
  if (!chat.lastUserMessage) {
    return
  }

  const lastMessage = chat.messages[chat.messages.length - 1]
  if (lastMessage?.isError) {
    chat.messages.pop()
  }

  const maybeUserMessage = chat.messages[chat.messages.length - 1]
  if (maybeUserMessage?.role === 'user') {
    chat.messages.pop()
  }

  handleSendMessage(chat.lastUserMessage)
}

function closeCurrentStream() {
  if (chat.currentEventSource) {
    closeChatStream(chat.currentEventSource)
  }
}

function resetChatSession(nextWorkflowId = currentWorkflowId.value) {
  closeCurrentStream()
  chat.messages = []
  chat.isStreaming = false
  chat.currentEventSource = null
  chat.lastUserMessage = ''
  chat.prefillText = ''
  chat.prefillMode = 'ask'
  chat.memoryId = generateMemoryId()
  chat.linkedWorkflowId = normalizeOptionalString(nextWorkflowId)
  window.localStorage.setItem(MEMORY_STORAGE_KEY, String(chat.memoryId))
}

onMounted(async () => {
  setPreferredLocale(currentLocale.value)
  initMemoryId()
  await restoreWorkflowIfNeeded()
})

watch(
  currentLocale,
  (nextLocale) => {
    document.title = t('app.browserTitle')
    document.documentElement.lang = nextLocale
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  closeCurrentStream()
})
</script>

<style scoped>
.app-container {
  position: relative;
  isolation: isolate;
  overflow: hidden;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(circle at top left, rgba(20, 124, 131, 0.08), transparent 24%),
    radial-gradient(circle at 95% 0%, rgba(186, 124, 55, 0.08), transparent 20%),
    var(--color-background);
}

.app-container::before,
.app-container::after {
  content: '';
  position: absolute;
  inset: auto;
  border-radius: 50%;
  pointer-events: none;
  z-index: -1;
  filter: blur(16px);
}

.app-container::before {
  top: 96px;
  right: -120px;
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(20, 124, 131, 0.16), transparent 68%);
}

.app-container::after {
  bottom: 12%;
  left: -120px;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(186, 124, 55, 0.14), transparent 68%);
}

.app-main {
  flex: 1;
  min-height: 0;
  padding-block: var(--spacing-lg) var(--spacing-3xl);
}

.app-shell {
  width: min(var(--shell-width), 100%);
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 104px);
}

.workflow-shell {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.status-card {
  max-width: 760px;
  padding: clamp(1.5rem, 3vw, 2rem);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(188, 199, 210, 0.84);
  background:
    radial-gradient(circle at top right, rgba(20, 124, 131, 0.08), transparent 28%),
    radial-gradient(circle at bottom left, rgba(186, 124, 55, 0.08), transparent 22%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(249, 247, 242, 0.96));
  box-shadow: var(--shadow-md);
}

.status-card__eyebrow {
  display: inline-flex;
  margin-bottom: var(--spacing-sm);
  padding: 5px 11px;
  border-radius: var(--radius-full);
  background: rgba(16, 38, 61, 0.08);
  color: var(--color-primary);
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.11em;
  text-transform: uppercase;
}

.status-card h2 {
  margin: 0;
  max-width: 18ch;
  font-size: clamp(1.55rem, 3vw, 2.05rem);
  color: var(--color-primary);
}

.status-card p {
  margin-top: var(--spacing-sm);
  color: var(--color-text-muted);
  max-width: 58ch;
  font-size: 1rem;
}

@media (max-width: 640px) {
  .app-main {
    padding-block: var(--spacing-md) var(--spacing-2xl);
  }

  .app-shell {
    padding: 0 var(--spacing-md);
    min-height: calc(100vh - 88px);
  }

  .status-card {
    max-width: none;
    padding: var(--spacing-md);
  }
}
</style>
