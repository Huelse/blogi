function getDefaultErrorMessage() {
  try {
    const i18n = (
      useNuxtApp() as {
        $i18n?: {
          t: (key: string) => string
        }
      }
    ).$i18n

    const translated = i18n?.t('common.requestFailed')
    if (typeof translated === 'string' && translated.length > 0) {
      return translated
    }
  } catch {
    // fall back to a static message when no Nuxt app context is active
  }

  return 'Request failed. Please try again later.'
}

export function getErrorMessage(error: unknown, fallbackMessage = getDefaultErrorMessage()) {
  const message = (error as { data?: { message?: string } })?.data?.message
  if (typeof message === 'string' && message.length > 0) {
    return message
  }

  return fallbackMessage
}
