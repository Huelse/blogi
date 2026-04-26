const authEntryPaths = new Set(['/login', '/register'])
const defaultAuthenticatedPath = '/admin'

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
  if (authEntryPaths.has(pathname)) {
    return fallback
  }

  return redirect
}
