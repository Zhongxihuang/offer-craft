<template>
  <form class="chat-input" @submit.prevent="handleSubmit">
    <div class="input-container">
      <textarea
        ref="textareaRef"
        v-model="inputValue"
        :placeholder="placeholder"
        :disabled="disabled"
        @keydown="handleKeydown"
        rows="1"
        aria-label="Type your message"
      ></textarea>
      <button 
        type="submit" 
        class="send-btn"
        :disabled="disabled || !inputValue.trim()"
        :title="disabled ? 'Please wait for the response' : 'Send message'"
      >
        <template v-if="isLoading">
          <svg class="animate-spin" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <circle cx="12" cy="12" r="10" stroke-opacity="0.25" />
            <path d="M12 2a10 10 0 0 1 10 10" stroke-opacity="1" />
          </svg>
          <span class="sr-only">Loading</span>
        </template>
        <template v-else>
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M22 2L11 13" />
            <path d="M22 2L15 22L11 13L2 9L22 2Z" />
          </svg>
          <span class="sr-only">Send</span>
        </template>
      </button>
    </div>
    <p class="input-hint">
      Press <kbd>Enter</kbd> to send, <kbd>Shift + Enter</kbd> for new line
    </p>
  </form>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: 'Ask about programming, interviews, or career advice...'
  }
})

const emit = defineEmits(['send'])

const inputValue = ref('')
const textareaRef = ref(null)

// Auto-resize textarea
watch(inputValue, async () => {
  await nextTick()
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
    textareaRef.value.style.height = Math.min(textareaRef.value.scrollHeight, 200) + 'px'
  }
})

function handleKeydown(event) {
  // Send on Enter, new line on Shift+Enter
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSubmit()
  }
}

function handleSubmit() {
  const message = inputValue.value.trim()
  if (message && !props.disabled) {
    emit('send', message)
    inputValue.value = ''
    // Reset textarea height
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
    }
  }
}

// Focus input method
function focus() {
  textareaRef.value?.focus()
}

defineExpose({ focus })
</script>

<style scoped>
.chat-input {
  position: sticky;
  bottom: 0;
  background-color: var(--color-background);
  padding: var(--spacing-md) var(--spacing-lg);
  border-top: 1px solid var(--color-border);
}

.input-container {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  align-items: flex-end;
  gap: var(--spacing-sm);
  background-color: var(--color-surface);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--spacing-sm);
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal);
}

.input-container:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.input-container textarea {
  flex: 1;
  min-height: 44px;
  max-height: 200px;
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: 1rem;
  line-height: 1.5;
  color: var(--color-text);
  background: transparent;
  resize: none;
  overflow-y: auto;
}

.input-container textarea::placeholder {
  color: var(--color-text-light);
}

.input-container textarea:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.send-btn {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-primary);
  color: white;
  border-radius: var(--radius-md);
  transition: background-color var(--transition-normal), transform var(--transition-fast);
  cursor: pointer;
}

.send-btn:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
  transform: scale(1.02);
}

.send-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.send-btn:disabled {
  background-color: var(--color-text-light);
  cursor: not-allowed;
}

.send-btn svg {
  width: 20px;
  height: 20px;
}

.input-hint {
  max-width: 900px;
  margin: var(--spacing-xs) auto 0;
  font-size: 0.75rem;
  color: var(--color-text-light);
  text-align: center;
}

.input-hint kbd {
  display: inline-block;
  padding: 2px 6px;
  background-color: var(--color-border);
  border-radius: var(--radius-sm);
  font-family: inherit;
  font-size: 0.6875rem;
  color: var(--color-text-muted);
}

/* Mobile Responsive */
@media (max-width: 640px) {
  .chat-input {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .input-container {
    border-radius: var(--radius-md);
  }
  
  .input-container textarea {
    font-size: 0.9375rem;
    min-height: 40px;
    padding: var(--spacing-xs) var(--spacing-sm);
  }
  
  .send-btn {
    width: 40px;
    height: 40px;
  }
  
  .send-btn svg {
    width: 18px;
    height: 18px;
  }
  
  .input-hint {
    display: none;
  }
}
</style>
