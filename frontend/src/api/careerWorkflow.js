import apiClient from './index'
import { i18n } from '../i18n'

export async function analyzeCareerWorkflow(payload) {
  const { data } = await apiClient.post(
    '/career/workflow/analyze',
    JSON.stringify(payload),
    {
      headers: {
        'Content-Type': 'application/json'
      }
    }
  )
  return data
}

export async function analyzeCareerWorkflowUpload(formData) {
  const { data } = await apiClient.post('/career/workflow/analyze-upload', formData)
  return data
}

export async function getCareerWorkflow(workflowId) {
  const normalizedWorkflowId = typeof workflowId === 'string' ? workflowId.trim() : ''

  if (!normalizedWorkflowId) {
    throw new Error(i18n.global.t('api.workflowIdRequired'))
  }

  const { data } = await apiClient.get(`/career/workflow/${encodeURIComponent(normalizedWorkflowId)}`)
  return data
}

export async function refineCareerWorkflow(workflowId, payload) {
  const normalizedWorkflowId = typeof workflowId === 'string' ? workflowId.trim() : ''

  if (!normalizedWorkflowId) {
    throw new Error(i18n.global.t('api.workflowIdRequired'))
  }

  const { data } = await apiClient.post(
    `/career/workflow/${encodeURIComponent(normalizedWorkflowId)}/refine`,
    JSON.stringify(payload ?? {}),
    {
      headers: {
        'Content-Type': 'application/json'
      }
    }
  )
  return data
}

export async function getCareerWorkflowVersions(workflowId) {
  const normalizedWorkflowId = typeof workflowId === 'string' ? workflowId.trim() : ''

  if (!normalizedWorkflowId) {
    throw new Error(i18n.global.t('api.workflowIdRequired'))
  }

  const { data } = await apiClient.get(`/career/workflow/${encodeURIComponent(normalizedWorkflowId)}/versions`)
  return Array.isArray(data) ? data : []
}

export function normalizeApiError(error) {
  const t = i18n.global.t
  const response = error?.response
  const data = response?.data ?? {}
  const status = response?.status ?? null
  const path = data?.path ?? error?.config?.url ?? ''
  const fieldErrors = normalizeFieldErrors(data, t)

  if (!response) {
    return {
      status,
      message: t('api.networkError'),
      fieldErrors,
      path,
      retryable: true
    }
  }

  return {
    status,
    message: resolveMessage({ status, data, error, fieldErrors }),
    fieldErrors,
    path,
    retryable: resolveRetryable(status)
  }
}

function normalizeFieldErrors(data, t) {
  const details = Array.isArray(data?.details)
    ? data.details
    : Array.isArray(data?.errors)
      ? data.errors
      : Array.isArray(data?.violations)
        ? data.violations
        : []

  return details.reduce((accumulator, detail) => {
    const field = typeof detail?.field === 'string' && detail.field.trim()
      ? detail.field.trim()
      : typeof detail?.name === 'string' && detail.name.trim()
        ? detail.name.trim()
        : ''
    const message = typeof detail?.message === 'string' && detail.message.trim()
      ? detail.message.trim()
      : typeof detail === 'string' && detail.trim()
        ? detail.trim()
        : t('api.unexpected')

    if (field) {
      accumulator[field] = accumulator[field] ? `${accumulator[field]} ${message}` : message
    }

    return accumulator
  }, {})
}

function resolveMessage({ status, data, error, fieldErrors }) {
  if (status === 404) {
    return t('api.analysisExpired')
  }

  if (typeof data?.message === 'string' && data.message.trim()) {
    return data.message.trim()
  }

  if (typeof data?.error === 'string' && data.error.trim()) {
    return data.error.trim()
  }

  if (status === 400 && Object.keys(fieldErrors).length > 0) {
    return t('api.highlightedFields')
  }

  return error?.message || t('api.unexpected')
}

function resolveRetryable(status) {
  if (status == null) {
    return true
  }

  if (status >= 500) {
    return true
  }

  return status === 408 || status === 429
}
