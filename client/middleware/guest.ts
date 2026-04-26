import { resolveAuthRedirect } from '~/utils/authRedirect'

export default defineNuxtRouteMiddleware((to) => {
  const auth = useAuth()

  if (!auth.isAuthenticated.value) {
    return
  }

  return navigateTo(resolveAuthRedirect(to.query.redirect))
})
