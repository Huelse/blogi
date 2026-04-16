<script setup lang="ts">
import {
  ArrowLeftOnRectangleIcon,
  ExclamationTriangleIcon,
  UserPlusIcon,
} from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type { AuthSession } from '~/types/blogi'
import { getErrorMessage } from '~/utils/errors'

const api = useApiClient()
const auth = useAuth()

const form = reactive({
  username: '',
  displayName: '',
  password: '',
})

const pending = ref(false)
const errorMessage = ref('')

async function submit() {
  pending.value = true
  errorMessage.value = ''

  try {
    const session = await api<AuthSession>('/auth/register', {
      method: 'POST',
      body: form,
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
    <UiCard class="p-8">
      <p class="text-brand text-sm uppercase tracking-[0.3em]">Register</p>
      <h1 class="text-title mt-4 text-3xl font-semibold">创建作者账号</h1>
      <p class="text-muted mt-3 text-sm leading-7">
        注册成功后会直接写入本地会话，可立即进入文章创建流程。
      </p>

      <form class="mt-8 space-y-5" @submit.prevent="submit">
        <div>
          <UiLabel for="username">用户名</UiLabel>
          <UiInput id="username" v-model="form.username" maxlength="32" required type="text" />
        </div>

        <div>
          <UiLabel for="displayName">昵称</UiLabel>
          <UiInput
            id="displayName"
            v-model="form.displayName"
            maxlength="40"
            required
            type="text"
          />
        </div>

        <div>
          <UiLabel for="password">密码</UiLabel>
          <UiInput id="password" v-model="form.password" minlength="6" required type="password" />
        </div>

        <UiAlert v-if="errorMessage" variant="destructive">
          <UiAlertDescription class="flex items-start gap-2">
            <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
            <span>{{ errorMessage }}</span>
          </UiAlertDescription>
        </UiAlert>

        <div class="flex flex-wrap gap-3">
          <UiButton :disabled="pending" type="submit">
            <UserPlusIcon aria-hidden="true" class="size-4" />
            {{ pending ? '注册中...' : '注册并登录' }}
          </UiButton>
          <NuxtLink :class="buttonVariants({ variant: 'secondary' })" to="/login">
            <ArrowLeftOnRectangleIcon aria-hidden="true" class="size-4" />
            已有账号，去登录
          </NuxtLink>
        </div>
      </form>
    </UiCard>
  </main>
</template>
