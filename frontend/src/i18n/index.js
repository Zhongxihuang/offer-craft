import { createI18n } from 'vue-i18n'
import { messages } from './messages'

export const LOCALE_STORAGE_KEY = 'careerAgent_locale'
export const SUPPORTED_LOCALES = ['en', 'zh-CN']

export function normalizeLocale(value) {
  if (typeof value !== 'string' || !value.trim()) {
    return 'en'
  }

  return value.trim().toLowerCase().startsWith('zh') ? 'zh-CN' : 'en'
}

export function detectBrowserLocale() {
  if (typeof window === 'undefined') {
    return 'en'
  }

  return normalizeLocale(window.navigator?.language || 'en')
}

export function getStoredLocale() {
  if (typeof window === 'undefined') {
    return 'en'
  }

  return normalizeLocale(window.localStorage.getItem(LOCALE_STORAGE_KEY) || detectBrowserLocale())
}

export const i18n = createI18n({
  legacy: false,
  locale: getStoredLocale(),
  fallbackLocale: 'en',
  messages
})

export function setPreferredLocale(locale) {
  const normalized = normalizeLocale(locale)
  i18n.global.locale.value = normalized

  if (typeof window !== 'undefined') {
    window.localStorage.setItem(LOCALE_STORAGE_KEY, normalized)
  }

  return normalized
}

export function getCurrentLocale() {
  return normalizeLocale(i18n.global.locale.value)
}
