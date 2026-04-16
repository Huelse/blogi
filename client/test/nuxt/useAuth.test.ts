import { afterEach, describe, expect, it, vi } from 'vitest'
import { useAuth } from '../../composables/useAuth'

describe('useAuth', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    useAuth().clearSession()
  })

  it('persists and clears the auth session', () => {
    const auth = useAuth()

    expect(auth.isAuthenticated.value).toBe(false)

    auth.setSession({
      token: 'token-123',
      user: {
        id: 1,
        username: 'max',
        displayName: 'Max'
      }
    })

    expect(auth.token.value).toBe('token-123')
    expect(auth.user.value?.displayName).toBe('Max')
    expect(auth.isAuthenticated.value).toBe(true)

    auth.clearSession()

    expect(auth.token.value).toBeNull()
    expect(auth.user.value).toBeNull()
    expect(auth.isAuthenticated.value).toBe(false)
  })

  it('clears incomplete local sessions during restore', async () => {
    const auth = useAuth()

    auth.user.value = {
      id: 1,
      username: 'max',
      displayName: 'Max'
    }

    await auth.restoreSession()

    expect(auth.token.value).toBeNull()
    expect(auth.user.value).toBeNull()
    expect(auth.isAuthenticated.value).toBe(false)
  })

  it('clears the session when the backend rejects the token', async () => {
    const auth = useAuth()

    auth.setSession({
      token: 'expired-token',
      user: {
        id: 1,
        username: 'max',
        displayName: 'Max'
      }
    })

    vi.stubGlobal('$fetch', vi.fn().mockRejectedValue({ statusCode: 401 }))

    await auth.restoreSession()

    expect(auth.token.value).toBeNull()
    expect(auth.user.value).toBeNull()
    expect(auth.isAuthenticated.value).toBe(false)
  })
})
