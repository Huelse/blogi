type ThemePreference = 'dark' | 'light'

export function useTheme() {
  const preference = useCookie<ThemePreference>('blogi_theme', {
    default: () => 'dark',
    sameSite: 'lax'
  })

  const isDark = computed(() => preference.value !== 'light')

  function setTheme(theme: ThemePreference) {
    preference.value = theme
  }

  function toggleTheme() {
    setTheme(isDark.value ? 'light' : 'dark')
  }

  return {
    preference,
    isDark,
    setTheme,
    toggleTheme
  }
}
