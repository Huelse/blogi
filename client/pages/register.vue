<script setup lang="ts">
import type { AuthSession } from '~/types/blogi'
import { getErrorMessage } from '~/utils/errors'

const api = useApiClient()
const auth = useAuth()

const form = reactive({
  username: '',
  displayName: '',
  password: ''
})

const pending = ref(false)
const errorMessage = ref('')

async function submit() {
  pending.value = true
  errorMessage.value = ''

  try {
    const session = await api<AuthSession>('/auth/register', {
      method: 'POST',
      body: form
    })

    auth.setSession(session)
    await navigateTo('/')
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
      <p class="text-sm uppercase tracking-[0.3em] text-amber-300">Register</p>
      <h1 class="mt-4 text-3xl font-semibold text-white">创建作者账号</h1>
      <p class="mt-3 text-sm leading-7 text-stone-400">注册成功后会直接写入本地会话，可立即进入文章创建流程。</p>

      <form class="mt-8 space-y-5" @submit.prevent="submit">
        <div>
          <label class="mb-2 block text-sm text-stone-300" for="username">用户名</label>
          <input id="username" v-model="form.username" class="field" maxlength="32" required type="text">
        </div>

        <div>
          <label class="mb-2 block text-sm text-stone-300" for="displayName">昵称</label>
          <input id="displayName" v-model="form.displayName" class="field" maxlength="40" required type="text">
        </div>

        <div>
          <label class="mb-2 block text-sm text-stone-300" for="password">密码</label>
          <input id="password" v-model="form.password" class="field" minlength="6" required type="password">
        </div>

        <p v-if="errorMessage" class="rounded-2xl border border-rose-300/20 bg-rose-300/10 px-4 py-3 text-sm text-rose-100">
          {{ errorMessage }}
        </p>

        <div class="flex flex-wrap gap-3">
          <button class="btn-primary" :disabled="pending" type="submit">
            {{ pending ? '注册中...' : '注册并登录' }}
          </button>
          <NuxtLink class="btn-secondary" to="/login">已有账号，去登录</NuxtLink>
        </div>
      </form>
    </section>
  </main>
</template>
