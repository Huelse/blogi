import type { FetchOptions } from 'ofetch'
import type { ApiEnvelope } from '~/types/blogi'
import { getStatusCode } from '~/utils/http'

export function useApiClient() {
  const config = useRuntimeConfig()
  const auth = useAuth()

  return async function request<T>(path: string, options: FetchOptions = {}) {
    const headers = {
      ...(auth.token.value ? { Authorization: `Bearer ${auth.token.value}` } : {}),
      ...((options.headers as Record<string, string> | undefined) ?? {})
    }

    try {
      const response = await $fetch<ApiEnvelope<T>>(path, {
        baseURL: config.public.apiBase,
        ...options,
        headers
      })

      return response.data
    } catch (error) {
      if (getStatusCode(error) === 401) {
        auth.clearSession()
      }

      throw error
    }
  }
}
