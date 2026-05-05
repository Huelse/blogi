<script setup lang="ts">
import {
  ArrowRightOnRectangleIcon,
  Cog6ToothIcon,
  HomeIcon,
  LanguageIcon,
  MoonIcon,
  SunIcon,
  UserCircleIcon,
} from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'

const auth = useAuth()
const theme = useTheme()
const { locale, setLocale, t } = useI18n()

const nextThemeLabel = computed(() =>
  theme.isDark.value ? t('app.theme.light') : t('app.theme.dark'),
)
const nextLocaleCode = computed(() => (locale.value === 'zh' ? 'en' : 'zh'))
const nextLocaleLabel = computed(() =>
  nextLocaleCode.value === 'zh' ? t('common.language.zh') : t('common.language.en'),
)

async function logout() {
  auth.clearSession()
  await navigateTo('/')
}

async function toggleLocale() {
  await setLocale(nextLocaleCode.value)
}
</script>

<template>
  <div :data-theme="theme.preference.value" class="app-shell min-h-screen bg-ink">
    <div class="pointer-events-none fixed inset-0 app-glow" />
    <header class="surface-nav relative">
      <nav class="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <NuxtLink to="/" class="brand-link text-sm font-semibold uppercase tracking-[0.35em]">
          Blogi
        </NuxtLink>

        <div class="flex items-center gap-2 text-sm sm:gap-3">
          <NuxtLink
            :class="buttonVariants({ variant: 'secondary', size: 'sm' })"
            class="hidden sm:inline-flex"
            to="/"
          >
            <HomeIcon aria-hidden="true" class="size-4" />
            {{ t('app.home') }}
          </NuxtLink>
          <UiButton
            size="sm"
            variant="secondary"
            :aria-label="t('app.languageSwitch', { locale: nextLocaleLabel })"
            type="button"
            @click="toggleLocale"
          >
            <LanguageIcon aria-hidden="true" class="size-4" />
            {{ nextLocaleLabel }}
          </UiButton>
          <UiButton
            size="sm"
            variant="secondary"
            :aria-label="
              theme.isDark.value ? t('app.theme.switchToLight') : t('app.theme.switchToDark')
            "
            type="button"
            @click="theme.toggleTheme()"
          >
            <component
              :is="theme.isDark.value ? SunIcon : MoonIcon"
              aria-hidden="true"
              class="size-4"
            />
            {{ nextThemeLabel }}
          </UiButton>
          <template v-if="auth.isAuthenticated.value && auth.user.value">
            <NuxtLink :class="buttonVariants({ variant: 'secondary', size: 'sm' })" to="/admin">
              <Cog6ToothIcon aria-hidden="true" class="size-4" />
              {{ t('app.admin') }}
            </NuxtLink>
            <UiBadge class="hidden gap-2 md:inline-flex" variant="muted">
              <img
                v-if="auth.user.value.avatarUrl"
                :alt="auth.user.value.displayName"
                class="size-4 rounded-full object-cover"
                :src="auth.user.value.avatarUrl"
              />
              <UserCircleIcon v-else aria-hidden="true" class="size-4" />
              {{ auth.user.value.displayName }}
            </UiBadge>
            <UiButton size="sm" variant="secondary" type="button" @click="logout">
              <ArrowRightOnRectangleIcon aria-hidden="true" class="size-4" />
              {{ t('app.logout') }}
            </UiButton>
          </template>
        </div>
      </nav>
    </header>

    <NuxtPage />
  </div>
</template>
