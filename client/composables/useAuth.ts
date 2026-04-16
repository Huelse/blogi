import type { ApiEnvelope, AuthSession, UserProfile } from '~/types/blogi'
import { getStatusCode } from '~/utils/http'

export function useAuth() {
  const config = useRuntimeConfig()
  const token = useCookie<string | null>('blogi_token', {
    default: () => null,
    sameSite: 'lax',
  })
  const user = useCookie<UserProfile | null>('blogi_user', {
    default: () => null,
    sameSite: 'lax',
  })

  const isAuthenticated = computed(() => Boolean(token.value && user.value))

  function setSession(session: AuthSession) {
    token.value = session.token
    user.value = session.user
  }

  function clearSession() {
    token.value = null
    user.value = null
  }

  async function restoreSession() {
    if (!token.value && !user.value) {
      return
    }

    if (!token.value || !user.value) {
      clearSession()
      return
    }

    try {
      const response = await $fetch<ApiEnvelope<UserProfile>>('/auth/me', {
        baseURL: config.public.apiBase,
        headers: {
          Authorization: `Bearer ${token.value}`,
        },
      })

      user.value = response.data
    } catch (error) {
      if (getStatusCode(error) === 401) {
        clearSession()
      }
    }
  }

  return {
    token,
    user,
    isAuthenticated,
    setSession,
    clearSession,
    restoreSession,
  }
}
