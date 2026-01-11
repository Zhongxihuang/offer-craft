<template>
  <div 
    class="message-bubble"
    :class="[
      isUser ? 'user-message' : 'ai-message',
      { 'error-message': isError }
    ]"
  >
    <!-- Avatar -->
    <div class="avatar" :class="isUser ? 'user-avatar' : 'ai-avatar'" aria-hidden="true">
      <template v-if="isUser">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
      </template>
      <template v-else>
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 8V4H8" />
          <rect width="16" height="12" x="4" y="8" rx="2" />
          <path d="M2 14h2" />
          <path d="M20 14h2" />
          <path d="M15 13v2" />
          <path d="M9 13v2" />
        </svg>
      </template>
    </div>
    
    <!-- Message Content -->
    <div class="message-content">
      <div class="message-header">
        <span class="sender-name">{{ isUser ? 'You' : 'Code Forge AI' }}</span>
        <span class="timestamp">{{ formattedTime }}</span>
      </div>
      <div 
        class="message-text" 
        :class="{ 'streaming': isStreaming }"
        v-html="renderedContent"
      ></div>
      
      <!-- Retry Button for Error Messages -->
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
        Retry
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

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

// Format timestamp
const formattedTime = computed(() => {
  const date = new Date(props.timestamp)
  return date.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: true
  })
})

// Simple markdown-like rendering
const renderedContent = computed(() => {
  if (!props.content) {
    return props.isStreaming ? '<span class="typing-indicator"></span>' : ''
  }
  
  let text = props.content
  
  // Escape HTML first
  text = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  
  // Code blocks (```...```)
  text = text.replace(/```(\w*)\n?([\s\S]*?)```/g, (match, lang, code) => {
    return `<pre class="code-block"><code>${code.trim()}</code></pre>`
  })
  
  // Inline code (`...`)
  text = text.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
  
  // Bold (**...**)
  text = text.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  
  // Italic (*...*)
  text = text.replace(/\*([^*]+)\*/g, '<em>$1</em>')
  
  // Line breaks
  text = text.replace(/\n/g, '<br>')
  
  return text
})
</script>

<style scoped>
.message-bubble {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  max-width: 85%;
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

/* User messages aligned to the right */
.user-message {
  margin-left: auto;
  flex-direction: row-reverse;
}

/* AI messages aligned to the left */
.ai-message {
  margin-right: auto;
}

/* Avatar Styles */
.avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar svg {
  width: 20px;
  height: 20px;
}

.user-avatar {
  background-color: var(--color-primary);
  color: white;
}

.ai-avatar {
  background-color: var(--color-secondary);
  color: white;
}

/* Message Content */
.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
}

.user-message .message-header {
  flex-direction: row-reverse;
}

.sender-name {
  font-weight: 600;
  font-size: 0.875rem;
  color: var(--color-text);
}

.timestamp {
  font-size: 0.75rem;
  color: var(--color-text-light);
}

.message-text {
  padding: var(--spacing-md);
  border-radius: var(--radius-lg);
  font-size: 0.9375rem;
  line-height: 1.6;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.user-message .message-text {
  background-color: var(--color-primary);
  color: white;
  border-bottom-right-radius: var(--radius-sm);
}

.ai-message .message-text {
  background-color: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-bottom-left-radius: var(--radius-sm);
  box-shadow: var(--shadow-sm);
}

/* Error Message Styles */
.error-message .message-text {
  background-color: var(--color-error-bg);
  color: var(--color-error);
  border-color: var(--color-error);
}

/* Streaming Indicator */
.message-text.streaming::after {
  content: '▋';
  display: inline-block;
  animation: blink 1s step-end infinite;
  margin-left: 2px;
  color: var(--color-primary);
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* Typing Indicator */
.typing-indicator {
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
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: var(--color-text-light);
  border-radius: 50%;
  margin-left: 4px;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator::before {
  animation-delay: 0.2s;
}

.typing-indicator::after {
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

/* Code Styling */
:deep(.code-block) {
  background-color: #1e293b;
  color: #e2e8f0;
  padding: var(--spacing-md);
  border-radius: var(--radius-sm);
  overflow-x: auto;
  margin: var(--spacing-sm) 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875rem;
  line-height: 1.5;
}

:deep(.inline-code) {
  background-color: rgba(0, 0, 0, 0.08);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875em;
}

.user-message :deep(.inline-code) {
  background-color: rgba(255, 255, 255, 0.2);
}

/* Retry Button */
.retry-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-sm);
  background-color: var(--color-error);
  color: white;
  border-radius: var(--radius-sm);
  font-size: 0.8125rem;
  font-weight: 500;
  transition: background-color var(--transition-normal);
  cursor: pointer;
}

.retry-btn:hover {
  background-color: #b91c1c;
}

.retry-btn svg {
  width: 14px;
  height: 14px;
}

/* Mobile Responsive */
@media (max-width: 640px) {
  .message-bubble {
    max-width: 95%;
    padding: var(--spacing-sm);
    gap: var(--spacing-sm);
  }
  
  .avatar {
    width: 32px;
    height: 32px;
  }
  
  .avatar svg {
    width: 16px;
    height: 16px;
  }
  
  .message-text {
    padding: var(--spacing-sm) var(--spacing-md);
    font-size: 0.875rem;
  }
}
</style>
