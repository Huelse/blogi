import type { AuthSession, UserProfile } from '~/types/blogi'

export function useAuth() {
  const token = useCookie<string | null>('blogi_token', {
    default: () => null,
    sameSite: 'lax'
  })
  const user = useCookie<UserProfile | null>('blogi_user', {
    default: () => null,
    sameSite: 'lax'
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

  return {
    token,
    user,
    isAuthenticated,
    setSession,
    clearSession
  }
}
