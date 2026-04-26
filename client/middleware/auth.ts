import { createLoginRedirect } from '~/utils/authRedirect'

export default defineNuxtRouteMiddleware((to) => {
  const auth = useAuth()

  if (auth.isAuthenticated.value) {
    return
  }

  return navigateTo(createLoginRedirect(to.fullPath))
})
