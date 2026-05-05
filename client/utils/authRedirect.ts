const AUTH_ENTRY_PATHS = new Set(['/login', '/register'])
const defaultAuthenticatedPath = '/admin'
const adminPath = '/admin'

function isSafeInternalPath(path: string) {
  return path.startsWith('/') && !path.startsWith('//')
}

export function createLoginRedirect(path: string) {
  return {
    path: '/login',
    query: {
      redirect: path,
    },
  }
}

export function resolveAuthRedirect(redirect: unknown, fallback = defaultAuthenticatedPath) {
  if (typeof redirect !== 'string' || !isSafeInternalPath(redirect)) {
    return fallback
  }

  const [pathname] = redirect.split('?')
  if (!pathname || AUTH_ENTRY_PATHS.has(pathname)) {
    return fallback
  }

  return redirect
}

export function isAuthEntryPath(pathname: string) {
  return AUTH_ENTRY_PATHS.has(pathname)
}

export function isAdminRoute(pathname: string) {
  return pathname === adminPath || pathname.startsWith(`${adminPath}/`)
}
