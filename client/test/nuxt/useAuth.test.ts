import { describe, expect, it } from 'vitest'
import { useAuth } from '../../composables/useAuth'

describe('useAuth', () => {
  it('persists and clears the auth session', () => {
    const auth = useAuth()

    expect(auth.isAuthenticated.value).toBe(false)

    auth.setSession({
      token: 'token-123',
      user: {
        id: 1,
        email: 'max@example.com',
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
})
