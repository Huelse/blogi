<script setup lang="ts">
import {
  ArrowLeftOnRectangleIcon,
  ArrowRightOnRectangleIcon,
  HomeIcon,
  MoonIcon,
  PencilSquareIcon,
  SunIcon,
  UserCircleIcon,
  UserPlusIcon,
} from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'

const auth = useAuth()
const theme = useTheme()

const nextThemeLabel = computed(() => (theme.isDark.value ? '浅色' : '深色'))

async function logout() {
  auth.clearSession()
  await navigateTo('/')
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
            首页
          </NuxtLink>
          <UiButton
            size="sm"
            variant="secondary"
            :aria-label="theme.isDark.value ? '切换到明亮主题' : '切换到黑暗主题'"
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
          <NuxtLink
            v-if="auth.isAuthenticated.value"
            :class="buttonVariants({ variant: 'secondary', size: 'sm' })"
            to="/posts/new"
          >
            <PencilSquareIcon aria-hidden="true" class="size-4" />
            写文章
          </NuxtLink>
          <template v-if="auth.isAuthenticated.value && auth.user.value">
            <UiBadge class="hidden md:inline-flex" variant="muted">
              <UserCircleIcon aria-hidden="true" class="size-4" />
              {{ auth.user.value.displayName }}
            </UiBadge>
            <UiButton size="sm" variant="secondary" type="button" @click="logout">
              <ArrowRightOnRectangleIcon aria-hidden="true" class="size-4" />
              退出
            </UiButton>
          </template>
          <template v-else>
            <NuxtLink
              :class="buttonVariants({ variant: 'secondary', size: 'sm' })"
              class="hidden sm:inline-flex"
              to="/register"
            >
              <UserPlusIcon aria-hidden="true" class="size-4" />
              注册
            </NuxtLink>
            <NuxtLink :class="buttonVariants({ size: 'sm' })" to="/login">
              <ArrowLeftOnRectangleIcon aria-hidden="true" class="size-4" />
              登录
            </NuxtLink>
          </template>
        </div>
      </nav>
    </header>

    <NuxtPage />
  </div>
</template>
