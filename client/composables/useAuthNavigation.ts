import { resolveAuthRedirect } from '~/utils/authRedirect'

export function useAuthNavigation() {
  const route = useRoute()

  function resolveRedirectTarget() {
    return resolveAuthRedirect(route.query.redirect)
  }

  function navigateAfterAuth() {
    return navigateTo(resolveRedirectTarget())
  }

  return {
    navigateAfterAuth,
    resolveRedirectTarget,
  }
}
