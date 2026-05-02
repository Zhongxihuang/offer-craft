<template>
  <form class="chat-input" @submit.prevent="handleSubmit">
    <div class="input-container">
      <textarea
        ref="textareaRef"
        v-model="inputValue"
        :placeholder="resolvedPlaceholder"
        :disabled="disabled"
        @keydown="handleKeydown"
        rows="1"
        :aria-label="t('chatInput.ariaLabel')"
      ></textarea>
      <button
        type="submit"
        class="send-btn"
        :disabled="disabled || !inputValue.trim()"
        :title="disabled ? t('chatInput.waitTitle') : t('chatInput.sendTitle')"
      >
        <template v-if="isLoading">
          <svg class="animate-spin" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <circle cx="12" cy="12" r="10" stroke-opacity="0.25" />
            <path d="M12 2a10 10 0 0 1 10 10" stroke-opacity="1" />
          </svg>
          <span class="sr-only">{{ t('chatInput.loading') }}</span>
        </template>
        <template v-else>
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M22 2L11 13" />
            <path d="M22 2L15 22L11 13L2 9L22 2Z" />
          </svg>
          <span class="sr-only">{{ t('chatInput.send') }}</span>
        </template>
      </button>
    </div>
    <p class="input-hint">
      {{ t('chatInput.hint') }}
    </p>
  </form>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

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
    default: ''
  },
  prefillText: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['send'])
const { t } = useI18n()
const inputValue = ref('')
const textareaRef = ref(null)

const resolvedPlaceholder = computed(() => props.placeholder || t('chatInput.defaultPlaceholder'))

watch(inputValue, async () => {
  await nextTick()
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
    textareaRef.value.style.height = `${Math.min(textareaRef.value.scrollHeight, 200)}px`
  }
})

watch(
  () => props.prefillText,
  async (value) => {
    if (!value || !value.trim()) {
      return
    }

    inputValue.value = value
    await nextTick()

    if (textareaRef.value) {
      textareaRef.value.focus()
      const length = textareaRef.value.value.length
      textareaRef.value.setSelectionRange(length, length)
    }
  }
)

function handleKeydown(event) {
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
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
    }
  }
}

function focus() {
  textareaRef.value?.focus()
}

defineExpose({ focus })
</script>

<style scoped>
.chat-input {
  position: sticky;
  bottom: 0;
  padding: 0;
  background: transparent;
}

.input-container {
  margin: 0;
  display: flex;
  align-items: flex-end;
  gap: var(--spacing-sm);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(249, 247, 242, 0.98));
  border: 1px solid rgba(188, 199, 210, 0.82);
  border-radius: 20px;
  padding: var(--spacing-sm);
  box-shadow: var(--shadow-sm);
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal), transform var(--transition-fast);
}

.input-container:focus-within {
  border-color: rgba(20, 124, 131, 0.72);
  box-shadow:
    0 0 0 4px rgba(20, 124, 131, 0.08),
    var(--shadow-md);
  transform: translateY(-1px);
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
  background: linear-gradient(135deg, var(--color-primary), #173552);
  color: white;
  border-radius: var(--radius-md);
  box-shadow: 0 18px 34px -24px rgba(16, 38, 61, 0.72);
  transition: background-color var(--transition-normal), transform var(--transition-fast), box-shadow var(--transition-normal);
  cursor: pointer;
}

.send-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--color-primary-hover), #13304d);
  transform: translateY(-1px);
  box-shadow: 0 22px 38px -24px rgba(16, 38, 61, 0.82);
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
  margin: var(--spacing-xs) 0 0;
  font-size: 0.75rem;
  color: var(--color-text-light);
  text-align: left;
}

@media (max-width: 640px) {
  .chat-input {
    padding: 0;
  }

  .input-container {
    border-radius: 18px;
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
