<template>
  <div
    class="message-bubble"
    :class="[
      isUser ? 'user-message' : 'ai-message',
      { 'error-message': isError }
    ]"
  >
    <div class="avatar" :class="isUser ? 'user-avatar' : 'ai-avatar'" aria-hidden="true">
      <template v-if="isUser">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
      </template>
      <template v-else>
        <img src="/career-agent-mark.svg" alt="" />
      </template>
    </div>

    <div class="message-content">
      <div class="message-header">
        <span class="sender-name">{{ isUser ? t('chatMessage.you') : t('chatMessage.agent') }}</span>
        <span class="timestamp">{{ formattedTime }}</span>
      </div>
      <div
        class="message-text"
        :class="{ streaming: isStreaming }"
        v-html="renderedContent"
      ></div>

      <button
        v-if="isError && showRetry"
        class="retry-btn"
        @click="$emit('retry')"
      >
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8" />
          <path d="M21 3v5h-5" />
          <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16" />
          <path d="M8 16H3v5" />
        </svg>
        {{ t('chatMessage.retry') }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  isUser: {
    type: Boolean,
    default: false
  },
  timestamp: {
    type: Date,
    default: () => new Date()
  },
  isStreaming: {
    type: Boolean,
    default: false
  },
  isError: {
    type: Boolean,
    default: false
  },
  showRetry: {
    type: Boolean,
    default: false
  }
})

defineEmits(['retry'])
const { locale, t } = useI18n()

const formattedTime = computed(() => {
  const date = new Date(props.timestamp)
  return date.toLocaleTimeString(locale.value, {
    hour: '2-digit',
    minute: '2-digit'
  })
})

const renderedContent = computed(() => {
  if (!props.content) {
    return props.isStreaming ? '<span class="typing-indicator"></span>' : ''
  }

  let text = props.content
  text = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  text = text.replace(/```(\w*)\n?([\s\S]*?)```/g, (match, lang, code) => {
    return `<pre class="code-block"><code>${code.trim()}</code></pre>`
  })
  text = text.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
  text = text.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  text = text.replace(/\*([^*]+)\*/g, '<em>$1</em>')
  text = text.replace(/\n/g, '<br>')

  return text
})
</script>

<style scoped>
.message-bubble {
  display: flex;
  gap: var(--spacing-md);
  width: min(88%, 860px);
  padding: 0.15rem;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  margin-left: auto;
  flex-direction: row-reverse;
}

.ai-message {
  margin-right: auto;
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid rgba(188, 199, 210, 0.78);
  box-shadow: 0 16px 30px -24px rgba(16, 38, 61, 0.32);
}

.avatar svg,
.avatar img {
  width: 20px;
  height: 20px;
}

.user-avatar {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 246, 241, 0.98));
  color: var(--color-primary);
}

.ai-avatar {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 246, 241, 0.98));
  color: white;
}

.message-content {
  flex: 1;
  min-width: 0;
  display: grid;
  gap: 0.45rem;
}

.message-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding-inline: 0.25rem;
}

.user-message .message-header {
  flex-direction: row-reverse;
}

.sender-name {
  font-weight: 700;
  font-size: 0.78rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-primary);
}

.timestamp {
  font-size: 0.75rem;
  color: var(--color-text-light);
}

.message-text {
  padding: 0.95rem 1rem;
  border-radius: 20px;
  font-size: 0.97rem;
  line-height: 1.68;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.user-message .message-text {
  background: linear-gradient(135deg, var(--color-primary), #173552);
  color: white;
  border-bottom-right-radius: 10px;
  box-shadow: 0 22px 42px -30px rgba(16, 38, 61, 0.72);
}

.ai-message .message-text {
  background:
    radial-gradient(circle at top right, rgba(20, 124, 131, 0.06), transparent 24%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(249, 247, 242, 0.98));
  color: var(--color-text);
  border: 1px solid rgba(188, 199, 210, 0.78);
  border-bottom-left-radius: 10px;
  box-shadow: var(--shadow-sm);
}

.error-message .message-text {
  background: linear-gradient(180deg, rgba(252, 237, 238, 0.95), rgba(255, 245, 245, 0.95));
  color: var(--color-error);
  border-color: rgba(201, 74, 74, 0.32);
}

.message-text.streaming::after {
  content: '▏';
  display: inline-block;
  animation: blink 1s step-end infinite;
  margin-left: 3px;
  color: currentColor;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.typing-indicator {
  position: relative;
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: var(--color-text-light);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator::before,
.typing-indicator::after {
  content: '';
  position: absolute;
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: var(--color-text-light);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator::before {
  left: 12px;
  animation-delay: 0.2s;
}

.typing-indicator::after {
  left: 24px;
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.retry-btn {
  margin-top: var(--spacing-sm);
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.55rem 0.8rem;
  border-radius: var(--radius-full);
  border: 1px solid rgba(201, 74, 74, 0.24);
  background: rgba(252, 237, 238, 0.95);
  color: var(--color-error);
  font-weight: 700;
}

.retry-btn svg {
  width: 16px;
  height: 16px;
}

:deep(.code-block) {
  background: #13283e;
  color: #e9f0f7;
  padding: var(--spacing-md);
  border-radius: 16px;
  overflow-x: auto;
  margin: var(--spacing-sm) 0;
  border: 1px solid rgba(186, 124, 55, 0.18);
  font-family: 'SFMono-Regular', 'Consolas', 'Liberation Mono', monospace;
  font-size: 0.875rem;
  line-height: 1.5;
}

:deep(.inline-code) {
  background-color: rgba(16, 42, 67, 0.08);
  padding: 0.15rem 0.35rem;
  border-radius: 8px;
  font-family: 'SFMono-Regular', 'Consolas', 'Liberation Mono', monospace;
  font-size: 0.875em;
}

@media (max-width: 640px) {
  .message-bubble {
    width: 100%;
    gap: var(--spacing-sm);
  }

  .avatar {
    width: 38px;
    height: 38px;
  }

  .message-text {
    padding: 0.85rem 0.9rem;
    font-size: 0.94rem;
  }
}
</style>
