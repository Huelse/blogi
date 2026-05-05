import { isAuthEntryPath, resolveAuthRedirect } from '~/utils/authRedirect'

export default defineNuxtRouteMiddleware((to, _from) => {
  if (!isAuthEntryPath(to.path)) {
    return
  }

  const auth = useAuth()

  if (!auth.isAuthenticated.value) {
    return
  }

  return navigateTo(resolveAuthRedirect(to.query.redirect))
})
