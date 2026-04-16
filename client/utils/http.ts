export function getStatusCode(error: unknown) {
  const normalizedError = error as {
    status?: number
    statusCode?: number
    response?: { status?: number }
  }

  return normalizedError.statusCode ?? normalizedError.status ?? normalizedError.response?.status
}
