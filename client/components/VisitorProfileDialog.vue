<script setup lang="ts">
import { XMarkIcon } from '@heroicons/vue/20/solid'
import type { VisitorProfile } from '~/types/blogi'
import { getErrorMessage } from '~/utils/errors'

const open = defineModel<boolean>('open', { default: false })
const emit = defineEmits<{
  saved: [profile: VisitorProfile]
}>()

const visitor = useVisitorIdentity()
const displayName = ref('')
const email = ref('')
const pending = ref(false)
const errorMessage = ref('')

watch(open, async (isOpen) => {
  if (!isOpen) {
    return
  }

  errorMessage.value = ''
  const profile = visitor.profile.value ?? (await visitor.loadProfile().catch(() => null))
  displayName.value = profile?.displayName ?? ''
  email.value = profile?.email ?? ''
})

async function submitProfile() {
  if (pending.value) {
    return
  }

  pending.value = true
  errorMessage.value = ''

  try {
    const savedProfile = await visitor.saveProfile({
      displayName: displayName.value,
      email: email.value,
    })
    emit('saved', savedProfile)
    open.value = false
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    pending.value = false
  }
}

function closeDialog() {
  open.value = false
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4 py-6 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="visitor-profile-title"
      @keydown.esc="closeDialog"
    >
      <div
        class="w-full max-w-md rounded-[8px] border border-[var(--panel-border)] bg-[var(--app-bg)] p-6 shadow-2xl"
      >
        <div class="flex items-start justify-between gap-4">
          <div>
            <p class="text-subtle text-sm uppercase tracking-[0.24em]">Profile</p>
            <h2 id="visitor-profile-title" class="text-title mt-2 text-2xl font-semibold">
              填写访客资料
            </h2>
          </div>
          <UiButton
            aria-label="关闭"
            class="shrink-0"
            size="icon"
            type="button"
            variant="ghost"
            @click="closeDialog"
          >
            <XMarkIcon aria-hidden="true" class="size-5" />
          </UiButton>
        </div>

        <UiAlert v-if="errorMessage" class="mt-5" variant="destructive">
          <UiAlertDescription>{{ errorMessage }}</UiAlertDescription>
        </UiAlert>

        <form class="mt-6 space-y-4" @submit.prevent="submitProfile">
          <div class="space-y-2">
            <UiLabel for="visitor-display-name">昵称</UiLabel>
            <UiInput
              id="visitor-display-name"
              v-model="displayName"
              autocomplete="name"
              maxlength="40"
              required
            />
          </div>

          <div class="space-y-2">
            <UiLabel for="visitor-email">邮箱</UiLabel>
            <UiInput
              id="visitor-email"
              v-model="email"
              autocomplete="email"
              maxlength="254"
              required
              type="email"
            />
          </div>

          <div class="flex flex-wrap justify-end gap-3 pt-2">
            <UiButton type="button" variant="secondary" @click="closeDialog">取消</UiButton>
            <UiButton :disabled="pending || !displayName.trim() || !email.trim()" type="submit">
              {{ pending ? '保存中...' : '保存并继续' }}
            </UiButton>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>
