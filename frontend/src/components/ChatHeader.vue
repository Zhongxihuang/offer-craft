<template>
  <header class="chat-header">
    <div class="header-content">
      <div class="brand-section">
        <div class="brand-mark">
          <img class="logo-icon" src="/career-agent-mark.svg" alt="" aria-hidden="true" />
        </div>
        <div class="brand-copy">
          <span class="brand-kicker">{{ t('header.kicker') }}</span>
          <div class="brand-title-row">
            <h1>{{ t('header.title') }}</h1>
            <span v-if="t('header.productType')" class="brand-tag">{{ t('header.productType') }}</span>
          </div>
          <p class="tagline">{{ t('header.tagline') }}</p>
        </div>
      </div>

      <div class="header-controls">
        <nav class="mode-switcher" :aria-label="t('header.navigation')">
          <button
            type="button"
            class="mode-btn"
            :class="{ active: mode === 'workflow' }"
            @click="emit('select-mode', 'workflow')"
          >
            {{ t('header.workflow') }}
          </button>
          <button
            type="button"
            class="mode-btn"
            :class="{ active: mode === 'chat' }"
            @click="emit('select-mode', 'chat')"
          >
            {{ t('header.chat') }}
          </button>
        </nav>

        <div class="header-actions">
          <div class="locale-switcher" :aria-label="t('header.localeLabel')">
            <button
              type="button"
              class="locale-btn"
              :class="{ active: currentLocale === 'zh-CN' }"
              @click="emit('change-locale', 'zh-CN')"
            >
              {{ t('header.localeChinese') }}
            </button>
            <button
              type="button"
              class="locale-btn"
              :class="{ active: currentLocale === 'en' }"
              @click="emit('change-locale', 'en')"
            >
              {{ t('header.localeEnglish') }}
            </button>
          </div>

          <button
            type="button"
            class="context-btn"
            :disabled="actionDisabled"
            @click="emit('primary-action')"
          >
            <span class="context-btn__label">{{ actionLabel }}</span>
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M5 12h14" />
              <path d="M12 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  mode: {
    type: String,
    default: 'workflow'
  },
  isChatStreaming: {
    type: Boolean,
    default: false
  },
  isWorkflowBusy: {
    type: Boolean,
    default: false
  },
  currentLocale: {
    type: String,
    default: 'en'
  }
})

const emit = defineEmits(['select-mode', 'primary-action', 'change-locale'])
const { t } = useI18n()

const actionLabel = computed(() => {
  return props.mode === 'chat' ? t('header.newChat') : t('header.newAnalysis')
})

const actionDisabled = computed(() => {
  return props.mode === 'chat' ? props.isChatStreaming : props.isWorkflowBusy
})
</script>

<style scoped>
.chat-header {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 18px var(--spacing-lg) 0;
  background:
    linear-gradient(180deg, rgba(250, 248, 244, 0.92), rgba(250, 248, 244, 0.74) 72%, transparent);
  backdrop-filter: blur(18px);
}

.header-content {
  width: min(var(--shell-width), 100%);
  margin: 0 auto;
  padding: 18px 22px;
  border: 1px solid rgba(188, 199, 210, 0.88);
  border-radius: 30px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(250, 248, 244, 0.88)),
    rgba(255, 255, 255, 0.84);
  box-shadow: var(--shadow-md);
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(0, 1fr);
  gap: var(--spacing-lg);
  align-items: center;
}

.brand-section {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.brand-mark {
  width: 58px;
  height: 58px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  background: linear-gradient(180deg, rgba(16, 38, 61, 0.06), rgba(16, 38, 61, 0.02));
  border: 1px solid rgba(188, 199, 210, 0.78);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.74);
}

.logo-icon {
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  filter: drop-shadow(0 12px 22px rgba(16, 38, 61, 0.12));
}

.brand-copy {
  min-width: 0;
}

.brand-kicker {
  display: inline-flex;
  align-items: center;
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.13em;
  text-transform: uppercase;
  color: var(--color-text-light);
}

.brand-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 0.35rem;
  flex-wrap: wrap;
}

.brand-copy h1 {
  margin: 0;
  font-size: 1.32rem;
  line-height: 1.05;
  color: var(--color-primary);
}

.brand-tag {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0.32rem 0.72rem;
  border-radius: var(--radius-full);
  border: 1px solid rgba(188, 199, 210, 0.8);
  background: rgba(245, 247, 250, 0.86);
  color: var(--color-text-muted);
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.tagline {
  margin-top: 0.38rem;
  max-width: 56ch;
  color: var(--color-text-muted);
  font-size: 0.92rem;
  line-height: 1.45;
}

.header-controls {
  display: grid;
  gap: 12px;
  justify-items: end;
}

.mode-switcher,
.locale-switcher {
  display: inline-flex;
  align-items: center;
  padding: 4px;
  border-radius: var(--radius-full);
  border: 1px solid rgba(188, 199, 210, 0.78);
  background: rgba(243, 240, 233, 0.86);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.62);
}

.mode-btn,
.locale-btn {
  min-height: 40px;
  padding: 0.72rem 1rem;
  border-radius: var(--radius-full);
  color: var(--color-text-light);
  font-size: 0.88rem;
  font-weight: 700;
  transition:
    background-color var(--transition-normal),
    color var(--transition-normal),
    box-shadow var(--transition-normal);
}

.locale-btn {
  min-width: 62px;
  padding-inline: 0.82rem;
}

.mode-btn.active,
.locale-btn.active {
  background: rgba(255, 255, 255, 0.98);
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.context-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 48px;
  padding: 0.82rem 1rem 0.82rem 1.08rem;
  border-radius: 18px;
  background: linear-gradient(135deg, var(--color-primary), #173552);
  color: #ffffff;
  box-shadow: 0 24px 46px -30px rgba(16, 38, 61, 0.72);
  transition:
    transform var(--transition-fast),
    box-shadow var(--transition-normal),
    filter var(--transition-normal);
}

.context-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: saturate(1.04);
  box-shadow: 0 28px 50px -30px rgba(16, 38, 61, 0.78);
}

.context-btn__label {
  font-weight: 700;
}

.context-btn svg {
  width: 16px;
  height: 16px;
}

@media (max-width: 1080px) {
  .header-content {
    grid-template-columns: 1fr;
  }

  .header-controls {
    justify-items: start;
  }

  .header-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 640px) {
  .chat-header {
    padding: 14px var(--spacing-md) 0;
  }

  .header-content {
    padding: 16px;
    border-radius: 24px;
  }

  .brand-mark {
    width: 52px;
    height: 52px;
  }

  .brand-copy h1 {
    font-size: 1.16rem;
  }

  .tagline {
    font-size: 0.86rem;
  }

  .mode-switcher,
  .locale-switcher,
  .header-actions,
  .context-btn {
    width: 100%;
  }

  .mode-btn,
  .locale-btn {
    flex: 1;
    justify-content: center;
    text-align: center;
  }

  .context-btn {
    justify-content: center;
  }
}
</style>
