<script setup lang="ts">
import { CheckIcon, QueueListIcon, Squares2X2Icon } from '@heroicons/vue/20/solid'
import type { PostListLayout } from '~/composables/useBlogSettings'
import type { BlogSettings, UploadResponse, UserProfile } from '~/types/blogi'
import { getErrorMessage } from '~/utils/errors'

const api = useApiClient()
const auth = useAuth()
const blogSettings = useBlogSettings()
const { t } = useI18n()
const profileDisplayName = ref(auth.user.value?.displayName ?? '')
const profileAvatarUrl = ref(auth.user.value?.avatarUrl ?? '')
const profileError = ref('')
const profileSaved = ref(false)
const savingProfile = ref(false)
const uploadingProfileAvatar = ref(false)
const footerHtml = ref('')
const footerError = ref('')
const footerSaved = ref(false)
const savingFooter = ref(false)

const { data: siteSettings, error: siteSettingsError } = await useAsyncData(
  'blog-settings',
  () => api<BlogSettings>('/settings'),
  {
    default: () => ({ footerHtml: '' }),
  },
)

footerHtml.value = siteSettings.value.footerHtml

const footerPreviewHtml = computed(() => footerHtml.value.trim())
const layoutOptions = computed<
  Array<{
    label: string
    value: PostListLayout
    description: string
  }>
>(() => [
  {
    label: t('admin.settings.layout.list.label'),
    value: 'list',
    description: t('admin.settings.layout.list.description'),
  },
  {
    label: t('admin.settings.layout.card.label'),
    value: 'card',
    description: t('admin.settings.layout.card.description'),
  },
])

async function saveFooterSettings() {
  footerError.value = ''
  footerSaved.value = false
  savingFooter.value = true

  try {
    const updated = await api<BlogSettings>('/settings', {
      method: 'PUT',
      body: {
        footerHtml: footerHtml.value,
      },
    })
    siteSettings.value = updated
    footerHtml.value = updated.footerHtml
    footerSaved.value = true
  } catch (error) {
    footerError.value = getErrorMessage(error, t('common.requestFailed'))
  } finally {
    savingFooter.value = false
  }
}

function clearFooterSettings() {
  footerHtml.value = ''
  footerSaved.value = false
  footerError.value = ''
}

async function uploadProfileAvatar(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || uploadingProfileAvatar.value) {
    return
  }

  profileError.value = ''
  uploadingProfileAvatar.value = true

  try {
    const formData = new FormData()
    formData.append('usage', 'USER_AVATAR')
    formData.append('file', file)
    const upload = await api<UploadResponse>('/files/upload', {
      method: 'POST',
      body: formData,
    })
    profileAvatarUrl.value = upload.url
  } catch (error) {
    profileError.value = getErrorMessage(error, t('common.requestFailed'))
  } finally {
    uploadingProfileAvatar.value = false
    input.value = ''
  }
}

async function saveProfileSettings() {
  profileError.value = ''
  profileSaved.value = false
  savingProfile.value = true

  try {
    const updated = await api<UserProfile>('/auth/me', {
      method: 'PUT',
      body: {
        displayName: profileDisplayName.value,
        avatarUrl: profileAvatarUrl.value,
      },
    })
    auth.user.value = updated
    profileDisplayName.value = updated.displayName
    profileAvatarUrl.value = updated.avatarUrl ?? ''
    profileSaved.value = true
  } catch (error) {
    profileError.value = getErrorMessage(error, t('common.requestFailed'))
  } finally {
    savingProfile.value = false
  }
}
</script>

<template>
  <AdminShell :title="t('admin.settings.title')" :description="t('admin.settings.description')">
    <section
      class="mb-6 rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] p-5 backdrop-blur"
    >
      <div class="flex flex-col gap-2 border-b border-[var(--panel-border)] pb-5">
        <h2 class="text-title text-lg font-semibold">{{ t('admin.settings.profile.title') }}</h2>
        <p class="text-muted text-sm leading-7">{{ t('admin.settings.profile.description') }}</p>
      </div>

      <div class="mt-5 grid gap-5">
        <div class="flex items-center gap-3">
          <img
            v-if="profileAvatarUrl"
            :alt="t('admin.settings.profile.avatarPreview')"
            class="size-14 rounded-full border border-[var(--panel-border)] object-cover"
            :src="profileAvatarUrl"
          />
          <div
            v-else
            class="flex size-14 items-center justify-center rounded-full border border-dashed border-[var(--panel-border)] text-xs text-[var(--muted)]"
          >
            {{ t('admin.settings.profile.noAvatar') }}
          </div>

          <input
            accept="image/png,image/jpeg,image/webp,image/gif"
            class="max-w-[320px] rounded-2xl border border-[var(--panel-border)] bg-[var(--field-bg)] px-4 py-3 text-sm text-[var(--title)] transition file:mr-3 file:rounded-md file:border-0 file:bg-[var(--secondary-bg)] file:px-3 file:py-1.5 file:text-xs file:font-medium file:text-[var(--title)] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[color-mix(in_srgb,var(--brand)_16%,transparent)] disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="uploadingProfileAvatar || savingProfile"
            type="file"
            @change="uploadProfileAvatar"
          />
        </div>

        <div>
          <UiLabel for="profile-display-name">{{ t('admin.settings.profile.displayName') }}</UiLabel>
          <UiInput
            id="profile-display-name"
            v-model="profileDisplayName"
            maxlength="40"
            required
            type="text"
          />
        </div>

        <div class="flex flex-wrap items-center gap-3">
          <UiButton
            :disabled="savingProfile || uploadingProfileAvatar || !profileDisplayName.trim()"
            type="button"
            @click="saveProfileSettings"
          >
            {{ savingProfile ? t('common.saving') : t('admin.settings.profile.save') }}
          </UiButton>
          <span v-if="uploadingProfileAvatar" class="text-muted text-sm">
            {{ t('admin.settings.profile.uploading') }}
          </span>
          <span v-if="profileSaved" class="text-brand text-sm">
            {{ t('admin.settings.profile.saved') }}
          </span>
          <span v-if="profileError" class="text-danger text-sm">{{ profileError }}</span>
        </div>
      </div>
    </section>

    <section
      class="rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] p-5 backdrop-blur"
    >
      <div class="flex flex-col gap-2 border-b border-[var(--panel-border)] pb-5">
        <h2 class="text-title text-lg font-semibold">{{ t('admin.settings.layout.title') }}</h2>
        <p class="text-muted text-sm leading-7">{{ t('admin.settings.layout.description') }}</p>
      </div>

      <div class="mt-5 grid gap-3 md:grid-cols-2">
        <button
          v-for="option in layoutOptions"
          :key="option.value"
          class="h-[104px] overflow-hidden rounded-[8px] border p-4 text-left transition focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[color-mix(in_srgb,var(--brand)_16%,transparent)]"
          :class="
            blogSettings.postListLayout.value === option.value
              ? 'border-[var(--brand-border)] bg-[var(--brand-soft)]'
              : 'border-[var(--panel-border)] bg-[var(--panel-soft-bg)] hover:bg-[var(--secondary-bg-hover)]'
          "
          type="button"
          @click="blogSettings.setPostListLayout(option.value)"
        >
          <span
            class="grid w-full"
            style="grid-template-columns: 20px minmax(0, 1fr) 20px; column-gap: 12px"
          >
            <span class="flex items-center justify-center" style="width: 20px; height: 20px">
              <QueueListIcon
                v-if="option.value === 'list'"
                aria-hidden="true"
                class="text-[var(--brand)]"
                style="width: 20px; height: 20px"
              />
              <Squares2X2Icon
                v-else
                aria-hidden="true"
                class="text-[var(--brand)]"
                style="width: 20px; height: 20px"
              />
            </span>

            <span
              class="min-w-0"
              style="
                display: flex;
                width: 100%;
                flex-direction: column;
                align-items: flex-start;
                text-align: left;
              "
            >
              <span class="text-title block font-semibold leading-6">{{ option.label }}</span>
              <span class="text-muted mt-2 block text-sm leading-6">
                <span class="block line-clamp-2">{{ option.description }}</span>
              </span>
            </span>

            <span class="flex items-center justify-center" style="width: 20px; height: 20px">
              <CheckIcon
                v-if="blogSettings.postListLayout.value === option.value"
                aria-hidden="true"
                class="text-[var(--brand)]"
                style="width: 20px; height: 20px"
              />
            </span>
          </span>
        </button>
      </div>
    </section>

    <section
      class="mt-6 rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] p-5 backdrop-blur"
    >
      <div class="flex flex-col gap-2 border-b border-[var(--panel-border)] pb-5">
        <h2 class="text-title text-lg font-semibold">{{ t('admin.settings.footer.title') }}</h2>
        <p class="text-muted text-sm leading-7">{{ t('admin.settings.footer.description') }}</p>
      </div>

      <UiAlert v-if="siteSettingsError" class="mt-5 px-5 py-4" variant="destructive">
        <UiAlertDescription>{{ t('admin.settings.footer.loadError') }}</UiAlertDescription>
      </UiAlert>

      <div class="mt-5 grid gap-5">
        <div>
          <UiLabel for="footer-html">{{ t('admin.settings.footer.label') }}</UiLabel>
          <UiTextarea
            id="footer-html"
            v-model="footerHtml"
            class="min-h-48 font-mono text-xs leading-6"
            :placeholder="t('admin.settings.footer.placeholder')"
            spellcheck="false"
          />
          <p class="text-muted mt-2 text-xs leading-6">{{ t('admin.settings.footer.hint') }}</p>
        </div>

        <div
          class="rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-soft-bg)] p-4"
        >
          <div class="text-muted mb-3 text-xs font-semibold uppercase tracking-[0.24em]">
            {{ t('common.preview') }}
          </div>
          <!-- eslint-disable-next-line vue/no-v-html -->
          <div v-if="footerPreviewHtml" class="site-footer-html" v-html="footerPreviewHtml" />
          <p v-else class="text-muted text-sm leading-7">{{ t('admin.settings.footer.empty') }}</p>
        </div>

        <div class="flex flex-wrap items-center gap-3">
          <UiButton :disabled="savingFooter" type="button" @click="saveFooterSettings">
            {{ savingFooter ? t('common.saving') : t('admin.settings.footer.save') }}
          </UiButton>
          <UiButton
            :disabled="savingFooter"
            type="button"
            variant="secondary"
            @click="clearFooterSettings"
          >
            {{ t('admin.settings.footer.clear') }}
          </UiButton>
          <span v-if="footerSaved" class="text-brand text-sm"
            >{{ t('admin.settings.footer.saved') }}</span
          >
          <span v-if="footerError" class="text-danger text-sm">{{ footerError }}</span>
        </div>
      </div>
    </section>
  </AdminShell>
</template>
