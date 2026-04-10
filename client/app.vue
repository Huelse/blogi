<script setup lang="ts">
const auth = useAuth()

function logout() {
  auth.clearSession()
  navigateTo('/')
}
</script>

<template>
  <div class="min-h-screen bg-ink text-stone-100">
    <div class="pointer-events-none fixed inset-0 bg-[radial-gradient(circle_at_top,_rgba(245,158,11,0.16),_transparent_32%),radial-gradient(circle_at_bottom_right,_rgba(251,191,36,0.1),_transparent_28%)]" />
    <header class="relative border-b border-white/10 bg-black/20 backdrop-blur">
      <nav class="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <NuxtLink to="/" class="text-sm font-semibold uppercase tracking-[0.35em] text-amber-300">
          Blogi
        </NuxtLink>

        <div class="flex items-center gap-3 text-sm text-stone-300">
          <NuxtLink class="transition hover:text-white" to="/">文章</NuxtLink>
          <NuxtLink
            v-if="auth.isAuthenticated.value"
            class="rounded-full border border-amber-300/40 px-4 py-2 text-amber-100 transition hover:border-amber-200 hover:text-white"
            to="/posts/new"
          >
            写文章
          </NuxtLink>
          <template v-if="auth.user.value">
            <span class="hidden text-stone-400 md:inline">{{ auth.user.value.displayName }}</span>
            <button
              class="rounded-full border border-white/15 px-4 py-2 transition hover:border-white/30 hover:text-white"
              type="button"
              @click="logout"
            >
              退出
            </button>
          </template>
          <template v-else>
            <NuxtLink class="transition hover:text-white" to="/login">登录</NuxtLink>
            <NuxtLink class="transition hover:text-white" to="/register">注册</NuxtLink>
          </template>
        </div>
      </nav>
    </header>

    <NuxtPage />
  </div>
</template>
