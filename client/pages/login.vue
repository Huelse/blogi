<script setup lang="ts">
import type { AuthSession } from '~/types/blogi'
import { getErrorMessage } from '~/utils/errors'

const route = useRoute()
const api = useApiClient()
const auth = useAuth()

const form = reactive({
  username: '',
  password: ''
})

const pending = ref(false)
const errorMessage = ref('')

async function submit() {
  pending.value = true
  errorMessage.value = ''

  try {
    const session = await api<AuthSession>('/auth/login', {
      method: 'POST',
      body: form
    })

    auth.setSession(session)
    await navigateTo(typeof route.query.redirect === 'string' ? route.query.redirect : '/')
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <main class="mx-auto max-w-xl px-6 py-16">
    <section class="panel p-8">
      <p class="text-sm uppercase tracking-[0.3em] text-amber-300">Login</p>
      <h1 class="mt-4 text-3xl font-semibold text-white">登录 Blogi</h1>
      <p class="mt-3 text-sm leading-7 text-stone-400">使用用户名和密码换取 JWT，后续写作请求会自动附带认证头。</p>

      <form class="mt-8 space-y-5" @submit.prevent="submit">
        <div>
          <label class="mb-2 block text-sm text-stone-300" for="username">用户名</label>
          <input id="username" v-model="form.username" class="field" required type="text">
        </div>

        <div>
          <label class="mb-2 block text-sm text-stone-300" for="password">密码</label>
          <input id="password" v-model="form.password" class="field" required type="password">
        </div>

        <p v-if="errorMessage" class="rounded-2xl border border-rose-300/20 bg-rose-300/10 px-4 py-3 text-sm text-rose-100">
          {{ errorMessage }}
        </p>

        <div class="flex flex-wrap gap-3">
          <button class="btn-primary" :disabled="pending" type="submit">
            {{ pending ? '登录中...' : '登录' }}
          </button>
          <NuxtLink class="btn-secondary" to="/register">没有账号，去注册</NuxtLink>
        </div>
      </form>
    </section>
  </main>
</template>
