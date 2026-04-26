<script setup lang="ts">
import {
  ArrowLeftOnRectangleIcon,
  ExclamationTriangleIcon,
  UserPlusIcon,
} from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type { AuthSession } from '~/types/blogi'
import { resolveAuthRedirect } from '~/utils/authRedirect'
import { getErrorMessage } from '~/utils/errors'

definePageMeta({
  middleware: 'guest',
})

const route = useRoute()
const api = useApiClient()
const auth = useAuth()
const { t } = useI18n()

const form = reactive({
  username: '',
  password: '',
})

const pending = ref(false)
const errorMessage = ref('')
const authEntryQuery = computed(() =>
  typeof route.query.redirect === 'string' ? { redirect: route.query.redirect } : undefined,
)

async function submit() {
  pending.value = true
  errorMessage.value = ''

  try {
    const session = await api<AuthSession>('/auth/login', {
      method: 'POST',
      body: form,
    })

    auth.setSession(session)
    await navigateTo(resolveAuthRedirect(route.query.redirect))
  } catch (error) {
    errorMessage.value = getErrorMessage(error, t('common.requestFailed'))
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <main class="mx-auto max-w-xl px-6 py-16">
    <UiCard class="p-8">
      <p class="text-brand text-sm uppercase tracking-[0.3em]">{{ t('auth.login.eyebrow') }}</p>
      <h1 class="text-title mt-4 text-3xl font-semibold">{{ t('auth.login.title') }}</h1>
      <p class="text-muted mt-3 text-sm leading-7">{{ t('auth.login.description') }}</p>

      <form class="mt-8 space-y-5" @submit.prevent="submit">
        <div>
          <UiLabel for="username">{{ t('auth.login.username') }}</UiLabel>
          <UiInput id="username" v-model="form.username" required type="text" />
        </div>

        <div>
          <UiLabel for="password">{{ t('auth.login.password') }}</UiLabel>
          <UiInput id="password" v-model="form.password" required type="password" />
        </div>

        <UiAlert v-if="errorMessage" variant="destructive">
          <UiAlertDescription class="flex items-start gap-2">
            <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
            <span>{{ errorMessage }}</span>
          </UiAlertDescription>
        </UiAlert>

        <div class="flex flex-wrap gap-3">
          <UiButton :disabled="pending" type="submit">
            <ArrowLeftOnRectangleIcon aria-hidden="true" class="size-4" />
            {{ pending ? t('auth.login.pending') : t('auth.login.submit') }}
          </UiButton>
          <NuxtLink
            :class="buttonVariants({ variant: 'secondary' })"
            :to="{ path: '/register', query: authEntryQuery }"
          >
            <UserPlusIcon aria-hidden="true" class="size-4" />
            {{ t('auth.login.cta') }}
          </NuxtLink>
        </div>
      </form>
    </UiCard>
  </main>
</template>
