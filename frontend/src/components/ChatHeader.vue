<template>
  <header class="chat-header">
    <div class="header-content">
      <div class="logo-section">
        <div class="logo-icon" aria-hidden="true">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 18l2-2-2-2" />
            <path d="M8 6l-2 2 2 2" />
            <path d="M14.5 4l-5 16" />
          </svg>
        </div>
        <div class="logo-text">
          <h1>Code Forge AI</h1>
          <span class="tagline">Your Programming & Career Assistant</span>
        </div>
      </div>
      <button 
        class="new-chat-btn"
        @click="handleNewChat"
        :disabled="isStreaming"
        title="Start a new conversation"
      >
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M12 5v14" />
          <path d="M5 12h14" />
        </svg>
        <span>New Chat</span>
      </button>
    </div>
  </header>
</template>

<script setup>
defineProps({
  isStreaming: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['new-chat'])

function handleNewChat() {
  emit('new-chat')
}
</script>

<style scoped>
.chat-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  padding: var(--spacing-md) var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.header-content {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-md);
}

.logo-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.logo-icon svg {
  width: 24px;
  height: 24px;
}

.logo-text h1 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
  line-height: 1.2;
}

.tagline {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  display: block;
}

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  background-color: var(--color-primary);
  color: white;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 500;
  transition: background-color var(--transition-normal);
  cursor: pointer;
}

.new-chat-btn:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

.new-chat-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.new-chat-btn svg {
  width: 16px;
  height: 16px;
}

/* Mobile Responsive */
@media (max-width: 640px) {
  .chat-header {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .logo-text h1 {
    font-size: 1rem;
  }
  
  .tagline {
    display: none;
  }
  
  .new-chat-btn span {
    display: none;
  }
  
  .new-chat-btn {
    padding: var(--spacing-sm);
    border-radius: var(--radius-sm);
  }
  
  .new-chat-btn svg {
    width: 20px;
    height: 20px;
  }
}
</style>
