<script setup lang="ts">
import type { AuthSession } from '~/types/blogi'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
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
    <UiCard class="p-8">
      <p class="text-brand text-sm uppercase tracking-[0.3em]">Login</p>
      <h1 class="text-title mt-4 text-3xl font-semibold">登录 Blogi</h1>
      <p class="text-muted mt-3 text-sm leading-7">使用用户名和密码换取 JWT，后续写作请求会自动附带认证头。</p>

      <form class="mt-8 space-y-5" @submit.prevent="submit">
        <div>
          <UiLabel for="username">用户名</UiLabel>
          <UiInput id="username" v-model="form.username" required type="text" />
        </div>

        <div>
          <UiLabel for="password">密码</UiLabel>
          <UiInput id="password" v-model="form.password" required type="password" />
        </div>

        <UiAlert v-if="errorMessage" variant="destructive">
          <UiAlertDescription>{{ errorMessage }}</UiAlertDescription>
        </UiAlert>

        <div class="flex flex-wrap gap-3">
          <UiButton :disabled="pending" type="submit">{{ pending ? '登录中...' : '登录' }}</UiButton>
          <NuxtLink :class="buttonVariants({ variant: 'secondary' })" to="/register">没有账号，去注册</NuxtLink>
        </div>
      </form>
    </UiCard>
  </main>
</template>
