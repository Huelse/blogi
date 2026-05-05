<script setup lang="ts">
import { PhotoIcon, XMarkIcon } from '@heroicons/vue/20/solid'
import type { UploadResponse, VisitorProfile } from '~/types/blogi'
import { getErrorMessage } from '~/utils/errors'

const open = defineModel<boolean>('open', { default: false })
const emit = defineEmits<{
  saved: [profile: VisitorProfile]
}>()

const visitor = useVisitorIdentity()
const api = useApiClient()
const { t } = useI18n()
const displayName = ref('')
const email = ref('')
const avatarUrl = ref('')
const pending = ref(false)
const avatarPending = ref(false)
const errorMessage = ref('')

watch(open, async (isOpen) => {
  if (!isOpen) {
    return
  }

  errorMessage.value = ''
  const profile = visitor.profile.value ?? (await visitor.loadProfile().catch(() => null))
  displayName.value = profile?.displayName ?? ''
  email.value = profile?.email ?? ''
  avatarUrl.value = profile?.avatarUrl ?? ''
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
      avatarUrl: avatarUrl.value,
    })
    emit('saved', savedProfile)
    open.value = false
  } catch (error) {
    errorMessage.value = getErrorMessage(error, t('common.requestFailed'))
  } finally {
    pending.value = false
  }
}

function closeDialog() {
  open.value = false
}

async function uploadAvatar(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || avatarPending.value) {
    return
  }

  avatarPending.value = true
  errorMessage.value = ''

  try {
    const fingerprintHash = await visitor.ensureFingerprintHash()
    const formData = new FormData()
    formData.append('usage', 'VISITOR_AVATAR')
    formData.append('fingerprintHash', fingerprintHash)
    formData.append('file', file)
    const upload = await api<UploadResponse>('/files/upload', {
      method: 'POST',
      body: formData,
    })
    avatarUrl.value = upload.url
  } catch (error) {
    errorMessage.value = getErrorMessage(error, t('common.requestFailed'))
  } finally {
    avatarPending.value = false
    input.value = ''
  }
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
            <p class="text-subtle text-sm uppercase tracking-[0.24em]">
              {{ t('visitorProfile.eyebrow') }}
            </p>
            <h2 id="visitor-profile-title" class="text-title mt-2 text-2xl font-semibold">
              {{ t('visitorProfile.title') }}
            </h2>
          </div>
          <UiButton
            :aria-label="t('visitorProfile.close')"
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
            <UiLabel for="visitor-avatar-upload">{{ t('visitorProfile.avatar') }}</UiLabel>
            <div class="flex items-center gap-3">
              <img
                v-if="avatarUrl"
                :alt="t('visitorProfile.avatarPreview')"
                class="size-12 rounded-full border border-[var(--panel-border)] object-cover"
                :src="avatarUrl"
              >
              <div
                v-else
                class="flex size-12 items-center justify-center rounded-full border border-dashed border-[var(--panel-border)] text-[var(--muted)]"
              >
                <PhotoIcon aria-hidden="true" class="size-5" />
              </div>

              <input
                id="visitor-avatar-upload"
                accept="image/png,image/jpeg,image/webp,image/gif"
                class="max-w-[260px] rounded-2xl border border-[var(--panel-border)] bg-[var(--field-bg)] px-4 py-3 text-sm text-[var(--title)] transition file:mr-3 file:rounded-md file:border-0 file:bg-[var(--secondary-bg)] file:px-3 file:py-1.5 file:text-xs file:font-medium file:text-[var(--title)] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[color-mix(in_srgb,var(--brand)_16%,transparent)] disabled:cursor-not-allowed disabled:opacity-50"
                :disabled="avatarPending || pending"
                type="file"
                @change="uploadAvatar"
              >
            </div>
            <p v-if="avatarPending" class="text-muted text-xs">
              {{ t('visitorProfile.uploading') }}
            </p>
          </div>

          <div class="space-y-2">
            <UiLabel for="visitor-display-name">{{ t('visitorProfile.displayName') }}</UiLabel>
            <UiInput
              id="visitor-display-name"
              v-model="displayName"
              autocomplete="name"
              maxlength="40"
              required
            />
          </div>

          <div class="space-y-2">
            <UiLabel for="visitor-email">{{ t('visitorProfile.email') }}</UiLabel>
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
            <UiButton type="button" variant="secondary" @click="closeDialog">
              {{ t('visitorProfile.cancel') }}
            </UiButton>
            <UiButton :disabled="pending || !displayName.trim() || !email.trim()" type="submit">
              {{ pending ? t('visitorProfile.pending') : t('visitorProfile.submit') }}
            </UiButton>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>
