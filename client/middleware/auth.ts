import { createLoginRedirect, isAdminRoute } from '~/utils/authRedirect'

export default defineNuxtRouteMiddleware((to, _from) => {
  if (!isAdminRoute(to.path)) {
    return
  }

  const auth = useAuth()

  if (auth.isAuthenticated.value) {
    return
  }

  return navigateTo(createLoginRedirect(to.fullPath))
})
