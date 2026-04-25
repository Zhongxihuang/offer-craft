<template>
  <section class="support-chat-panel">
    <header class="chat-workspace">
      <div class="chat-workspace__copy">
        <span class="chat-workspace__eyebrow">
          {{ hasWorkflowContext ? t('supportChat.linkedEyebrow') : t('supportChat.emptyEyebrow') }}
        </span>
        <h2>{{ hasWorkflowContext ? t('supportChat.linkedTitle') : t('supportChat.emptyTitle') }}</h2>
        <p>{{ hasWorkflowContext ? t('supportChat.linkedDescription') : t('supportChat.emptyDescription') }}</p>
      </div>

      <div v-if="hasWorkflowContext" class="chat-workspace__context">
        <span v-if="workflowContext?.roleTitle" class="chat-pill">{{ workflowContext.roleTitle }}</span>
        <span v-if="workflowContext?.companyName" class="chat-pill">{{ workflowContext.companyName }}</span>
        <span v-if="workflowContext?.fitLevel" class="chat-pill chat-pill--accent">{{ localizedFitLevel(workflowContext.fitLevel) }}</span>
        <span v-if="workflowContext?.versionNumber" class="chat-pill chat-pill--muted">v{{ workflowContext.versionNumber }}</span>
        <span v-if="workflowContext?.workflowId" class="chat-pill chat-pill--muted">{{ shortWorkflowId }}</span>
      </div>
    </header>

    <div v-if="showLocaleNotice" class="chat-notice">
      <strong>{{ t('supportChat.localeNoticeTitle') }}</strong>
      <p>{{ t('supportChat.localeNoticeBody', { current: currentLocaleLabel, content: contentLocaleLabel }) }}</p>
    </div>

    <section class="chat-shell">
      <div class="chat-shell__topbar">
        <div>
          <span class="chat-shell__label">{{ t('supportChat.conversationLabel') }}</span>
          <p class="chat-shell__hint">
            {{ sendMode === 'refine' ? t('supportChat.modeRefineHelp') : t('supportChat.modeAskHelp') }}
          </p>
        </div>

        <div v-if="hasWorkflowContext" class="chat-mode-switch">
          <button
            type="button"
            class="chat-mode-switch__btn"
            :class="{ active: sendMode === 'ask' }"
            @click="sendMode = 'ask'"
          >
            {{ t('supportChat.modeAsk') }}
          </button>
          <button
            type="button"
            class="chat-mode-switch__btn"
            :class="{ active: sendMode === 'refine' }"
            @click="sendMode = 'refine'"
          >
            {{ t('supportChat.modeRefine') }}
          </button>
        </div>
      </div>

      <div class="chat-shell__conversation">
        <div v-if="!hasMessages" class="chat-empty-state">
          <span class="chat-shell__label">{{ t('supportChat.conversationLabel') }}</span>
          <h3>{{ hasWorkflowContext ? t('supportChat.linkedTitle') : t('supportChat.emptyTitle') }}</h3>
          <p>{{ hasWorkflowContext ? t('supportChat.linkedDescription') : t('supportChat.emptyDescription') }}</p>
          <p v-if="resolvedPrefillText" class="chat-empty-state__prefill">{{ t('supportChat.prefillLoaded') }}</p>
        </div>

        <div v-else ref="messageListRef" class="message-list" aria-live="polite">
          <ChatMessage
            v-for="(message, index) in normalizedMessages"
            :key="message.key"
            :content="message.content"
            :is-user="message.isUser"
            :timestamp="message.timestamp"
            :is-streaming="message.isStreaming"
            :is-error="message.isError"
            :show-retry="message.showRetry"
            @retry="emitRetry(message.raw, index)"
          />
        </div>
      </div>

      <div v-if="resolvedPrefillText" class="chat-prefill">
        <span class="chat-shell__label">{{ t('supportChat.suggestedFollowUp') }}</span>
        <p>{{ resolvedPrefillText }}</p>
      </div>

      <ChatInput
        class="chat-shell__input"
        :disabled="isStreaming"
        :is-loading="isStreaming"
        :prefill-text="resolvedPrefillText"
        :placeholder="t('supportChat.inputPlaceholder')"
        @send="handleSend"
      />
    </section>
  </section>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import ChatInput from './ChatInput.vue'
import ChatMessage from './ChatMessage.vue'

const props = defineProps({
  messages: { type: Array, default: () => [] },
  isStreaming: { type: Boolean, default: false },
  prefillText: { type: String, default: '' },
  preferredMode: { type: String, default: 'ask' },
  workflowContext: { type: Object, default: null }
})

const emit = defineEmits(['send', 'retry', 'prefill-consumed'])
const { locale, t } = useI18n()
const messageListRef = ref(null)
const sendMode = ref('ask')

const normalizedMessages = computed(() => {
  return (Array.isArray(props.messages) ? props.messages : []).map((message, index) => ({
    key: message?.id ?? `${index}-${message?.timestamp ?? 'message'}`,
    raw: message,
    content: typeof message?.content === 'string' ? message.content : typeof message?.text === 'string' ? message.text : '',
    isUser: Boolean(message?.isUser ?? message?.role === 'user' ?? message?.sender === 'user'),
    timestamp: message?.timestamp ?? message?.createdAt ?? new Date(),
    isStreaming: Boolean(message?.isStreaming),
    isError: Boolean(message?.isError ?? message?.error),
    showRetry: Boolean(message?.showRetry ?? message?.canRetry ?? message?.isError ?? message?.error)
  }))
})

const hasMessages = computed(() => normalizedMessages.value.length > 0)
const resolvedPrefillText = computed(() => props.prefillText.trim())
const hasWorkflowContext = computed(() => Boolean(props.workflowContext?.workflowId))
const shortWorkflowId = computed(() => {
  if (!props.workflowContext?.workflowId) return ''
  return t('supportChat.workflowShortId', { id: props.workflowContext.workflowId.slice(0, 8) })
})
const contentLocale = computed(() => normalizeLocale(props.workflowContext?.contentLocale))
const currentLocale = computed(() => normalizeLocale(locale.value))
const showLocaleNotice = computed(() => Boolean(hasWorkflowContext.value && contentLocale.value !== currentLocale.value))
const currentLocaleLabel = computed(() => localeLabel(currentLocale.value))
const contentLocaleLabel = computed(() => localeLabel(contentLocale.value))

function normalizeLocale(value) {
  return typeof value === 'string' && value.toLowerCase().startsWith('zh') ? 'zh-CN' : 'en'
}

function localeLabel(value) {
  return value === 'zh-CN' ? t('locale.chinese') : t('locale.english')
}

function localizedFitLevel(value) {
  const normalized = typeof value === 'string' ? value.trim() : ''
  if (!normalized) return ''
  const translated = t(`result.fitLevelMap.${normalized}`)
  return translated === `result.fitLevelMap.${normalized}` ? normalized : translated
}

function handleSend(message) {
  emit('send', { message, mode: hasWorkflowContext.value ? sendMode.value : 'ask' })
}

function emitRetry(message, index) {
  emit('retry', message ?? normalizedMessages.value[index]?.raw ?? null)
}

async function scrollToBottom() {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTo({ top: messageListRef.value.scrollHeight, behavior: 'smooth' })
  }
}

watch(() => normalizedMessages.value.length, scrollToBottom)
watch(() => normalizedMessages.value[normalizedMessages.value.length - 1]?.content, scrollToBottom)

watch(
  resolvedPrefillText,
  async (value) => {
    if (!value) return
    await nextTick()
    emit('prefill-consumed')
  }
)

watch(
  hasWorkflowContext,
  (value) => {
    if (!value) sendMode.value = 'ask'
  },
  { immediate: true }
)

watch(
  () => props.preferredMode,
  (value) => {
    sendMode.value = value === 'refine' && hasWorkflowContext.value ? 'refine' : 'ask'
  },
  { immediate: true }
)
</script>

<style scoped>
.support-chat-panel,.chat-shell{display:grid;gap:var(--spacing-md)}
.chat-workspace,.chat-shell{border:1px solid rgba(188,199,210,.84);background:rgba(255,255,255,.92);box-shadow:var(--shadow-md)}
.chat-workspace{display:grid;grid-template-columns:minmax(0,1.4fr) minmax(260px,.85fr);gap:var(--spacing-lg);padding:clamp(1.25rem,3vw,1.7rem);border-radius:var(--radius-xl);background:radial-gradient(circle at top right,rgba(20,124,131,.08),transparent 24%),radial-gradient(circle at bottom left,rgba(186,124,55,.08),transparent 20%),linear-gradient(180deg,rgba(255,255,255,.96),rgba(249,247,242,.96))}
.chat-workspace__copy{display:grid;gap:10px}
.chat-workspace__copy p,.chat-notice p,.chat-shell__hint,.chat-empty-state p,.chat-prefill p{color:var(--color-text-muted);line-height:1.58}
.chat-workspace__eyebrow,.chat-shell__label{display:inline-flex;align-items:center;width:fit-content;min-height:28px;padding:.28rem .74rem;border-radius:var(--radius-full);background:rgba(16,38,61,.08);color:var(--color-primary);font-size:.74rem;font-weight:800;letter-spacing:.12em;text-transform:uppercase}
.chat-workspace__context,.chat-mode-switch,.chat-prefill,.chat-notice{display:flex;flex-wrap:wrap;gap:10px}
.chat-workspace__context{align-content:start;justify-content:flex-end}
.chat-pill{display:inline-flex;align-items:center;min-height:32px;padding:.38rem .78rem;border-radius:var(--radius-full);border:1px solid rgba(188,199,210,.78);background:rgba(255,255,255,.88);color:var(--color-text);font-size:.82rem;font-weight:700}
.chat-pill--accent{background:rgba(186,124,55,.12);border-color:rgba(186,124,55,.28);color:var(--color-warning)}
.chat-pill--muted{color:var(--color-text-light)}
.chat-notice{display:grid;gap:.35rem;padding:1rem 1.05rem;border-radius:18px;border:1px solid rgba(188,199,210,.76);background:rgba(245,247,250,.84)}
.chat-shell{padding:clamp(1rem,2.4vw,1.25rem);border-radius:var(--radius-xl);background:linear-gradient(180deg,rgba(255,255,255,.98),rgba(247,245,240,.96))}
.chat-shell__topbar{display:grid;grid-template-columns:minmax(0,1fr) auto;gap:var(--spacing-md);align-items:start}
.chat-mode-switch{padding:4px;border-radius:var(--radius-full);border:1px solid rgba(188,199,210,.78);background:rgba(244,241,234,.88)}
.chat-mode-switch__btn{min-height:40px;padding:.68rem .95rem;border-radius:var(--radius-full);color:var(--color-text-light);font-weight:700;transition:background-color var(--transition-normal),box-shadow var(--transition-normal),color var(--transition-normal)}
.chat-mode-switch__btn.active{background:#fff;color:var(--color-primary);box-shadow:var(--shadow-sm)}
.chat-shell__conversation{min-height:460px;border:1px solid rgba(188,199,210,.74);border-radius:24px;background:linear-gradient(180deg,rgba(255,255,255,.94),rgba(247,245,240,.94));overflow:hidden}
.chat-empty-state{min-height:460px;display:grid;align-content:center;gap:10px;padding:clamp(1.5rem,4vw,2.4rem);background:radial-gradient(circle at top right,rgba(20,124,131,.08),transparent 28%),radial-gradient(circle at bottom left,rgba(186,124,55,.08),transparent 20%),linear-gradient(180deg,rgba(255,255,255,.98),rgba(249,247,242,.98))}
.chat-empty-state__prefill{display:inline-flex;width:fit-content;align-items:center;padding:.52rem .78rem;border-radius:var(--radius-full);background:rgba(16,38,61,.08);color:var(--color-primary);font-weight:600}
.message-list{display:flex;flex-direction:column;gap:var(--spacing-md);min-height:460px;padding:clamp(.85rem,2vw,1.15rem);overflow-y:auto}
.chat-prefill{display:grid;gap:.35rem;padding:1rem;border:1px solid rgba(188,199,210,.74);border-radius:20px;background:rgba(255,255,255,.84)}
.chat-shell__input{margin-top:0}
@media (max-width:960px){.chat-workspace,.chat-shell__topbar{grid-template-columns:1fr}.chat-workspace__context{justify-content:flex-start}}
@media (max-width:640px){.chat-workspace,.chat-shell{padding:var(--spacing-md);border-radius:22px}.chat-shell__conversation,.chat-empty-state,.message-list{min-height:380px}.chat-mode-switch{width:100%}.chat-mode-switch__btn{flex:1;text-align:center}}
</style>
