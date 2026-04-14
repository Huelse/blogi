<script setup lang="ts">
const auth = useAuth()
const theme = useTheme()

const nextThemeLabel = computed(() => theme.isDark.value ? '浅色' : '深色')

function logout() {
  auth.clearSession()
  navigateTo('/')
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
          <NuxtLink class="nav-link hidden sm:inline-flex" to="/">首页</NuxtLink>
          <button
            class="icon-button"
            :aria-label="theme.isDark.value ? '切换到明亮主题' : '切换到黑暗主题'"
            type="button"
            @click="theme.toggleTheme()"
          >
            {{ nextThemeLabel }}
          </button>
          <NuxtLink
            v-if="auth.isAuthenticated.value"
            class="btn-secondary"
            to="/posts/new"
          >
            写文章
          </NuxtLink>
          <template v-if="auth.user.value">
            <span class="user-chip">{{ auth.user.value.displayName }}</span>
            <button class="btn-secondary" type="button" @click="logout">退出</button>
          </template>
          <template v-else>
            <NuxtLink class="nav-link hidden sm:inline-flex" to="/register">注册</NuxtLink>
            <NuxtLink class="btn-primary" to="/login">登录</NuxtLink>
          </template>
        </div>
      </nav>
    </header>

    <NuxtPage />
  </div>
</template>
