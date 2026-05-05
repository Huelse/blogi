<script setup lang="ts">
import { ArrowLeftIcon, PencilSquareIcon, SparklesIcon } from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type {
  PostAiSummaryResponse,
  PostCategory,
  PostPayload,
  PostTag,
  UploadResponse,
} from '~/types/blogi'
import { getErrorMessage } from '~/utils/errors'
import { renderMarkdown } from '~/utils/markdown'

type PostEditorInitialValue = Partial<Omit<PostPayload, 'category' | 'tags'>> & {
  category?: string | PostCategory | null
  tags?: Array<string | PostTag>
}

const props = withDefaults(
  defineProps<{
    initialValue?: PostEditorInitialValue
    title: string
    description: string
    submitLabel: string
    backTo?: string
    backLabel?: string
    submitting?: boolean
  }>(),
  {
    initialValue: () => ({}),
    backTo: '/',
    backLabel: '',
    submitting: false,
  },
)

const emit = defineEmits<{
  save: [payload: PostPayload]
}>()
const { t } = useI18n()
const api = useApiClient()

const form = reactive<PostPayload>({
  title: '',
  summary: '',
  coverUrl: '',
  contentMarkdown: '',
  category: '',
  tags: [],
})
const tagText = ref('')
const coverUploading = ref(false)
const coverUploadError = ref('')
const aiSummaryPending = ref(false)
const aiSummaryError = ref('')
const aiSummaryHint = ref('')

watch(
  () => props.initialValue,
  (value) => {
    form.title = value.title ?? ''
    form.summary = value.summary ?? ''
    form.coverUrl = value.coverUrl ?? ''
    form.contentMarkdown = value.contentMarkdown ?? ''
    form.category = normalizeInitialCategory(value.category)
    tagText.value = normalizeInitialTags(value.tags).join(', ')
  },
  { immediate: true, deep: true },
)

const previewHtml = computed(() =>
  renderMarkdown(form.contentMarkdown || t('postEditor.previewFallback')),
)
const resolvedBackLabel = computed(() => props.backLabel || t('common.backToList'))

function submit() {
  emit('save', {
    title: form.title.trim(),
    summary: form.summary.trim(),
    coverUrl: form.coverUrl.trim(),
    contentMarkdown: form.contentMarkdown,
    category: form.category.trim(),
    tags: parseTags(tagText.value),
  })
}

async function uploadCover(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || coverUploading.value) {
    return
  }

  coverUploading.value = true
  coverUploadError.value = ''

  try {
    const formData = new FormData()
    formData.append('usage', 'POST_COVER')
    formData.append('file', file)
    const upload = await api<UploadResponse>('/files/upload', {
      method: 'POST',
      body: formData,
    })
    form.coverUrl = upload.url
  } catch (error) {
    coverUploadError.value = getErrorMessage(error, t('common.requestFailed'))
  } finally {
    coverUploading.value = false
    input.value = ''
  }
}

async function generateAiSummary() {
  if (aiSummaryPending.value || submitting.value) {
    return
  }

  aiSummaryError.value = ''
  aiSummaryHint.value = ''
  const contentMarkdown = form.contentMarkdown.trim()
  if (!contentMarkdown) {
    aiSummaryError.value = t('postEditor.aiSummaryRequiresContent')
    return
  }

  aiSummaryPending.value = true
  try {
    const response = await api<PostAiSummaryResponse>('/posts/summary/generate', {
      method: 'POST',
      body: {
        title: form.title.trim(),
        contentMarkdown,
      },
    })
    form.summary = response.summary
    aiSummaryHint.value = response.generatedByAi
      ? t('postEditor.aiSummaryApplied')
      : t('postEditor.aiSummaryFallbackNotice')
  } catch (error) {
    aiSummaryError.value = getErrorMessage(error, t('postEditor.aiSummaryFailed'))
  } finally {
    aiSummaryPending.value = false
  }
}

function normalizeInitialCategory(category: PostEditorInitialValue['category']) {
  if (!category) {
    return ''
  }
  return typeof category === 'string' ? category : category.name
}

function normalizeInitialTags(tags: PostEditorInitialValue['tags']) {
  if (!tags) {
    return []
  }
  return tags
    .map((tag) => (typeof tag === 'string' ? tag : tag.name))
    .map((tag) => tag.trim())
    .filter(Boolean)
}

function parseTags(value: string) {
  const tagsByKey = new Map<string, string>()

  for (const rawTag of value.split(/[,，\n]/)) {
    const tag = rawTag.trim()
    if (!tag) {
      continue
    }
    tagsByKey.set(tag.toLowerCase(), tag)
  }

  return [...tagsByKey.values()]
}

const submitting = computed(() => props.submitting)
</script>

<template>
  <section
    class="rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] p-6 backdrop-blur md:p-8"
  >
    <div class="max-w-2xl">
      <p class="text-brand text-sm uppercase tracking-[0.3em]">{{ title }}</p>
      <p class="text-muted mt-4 text-sm leading-7">{{ description }}</p>
    </div>

    <form class="mt-8 grid gap-8 lg:grid-cols-[1.05fr_0.95fr]" @submit.prevent="submit">
      <div class="space-y-5">
        <div>
          <UiLabel for="title">{{ t('postEditor.title') }}</UiLabel>
          <UiInput id="title" v-model="form.title" maxlength="120" required type="text" />
        </div>

        <div>
          <div class="flex flex-wrap items-center justify-between gap-2">
            <UiLabel for="summary">{{ t('postEditor.summary') }}</UiLabel>
            <UiButton
              :disabled="submitting || aiSummaryPending"
              size="sm"
              type="button"
              variant="secondary"
              @click="generateAiSummary"
            >
              <SparklesIcon aria-hidden="true" class="size-4" />
              {{ aiSummaryPending ? t('postEditor.aiSummaryGenerating') : t('postEditor.aiSummary') }}
            </UiButton>
          </div>
          <UiTextarea
            id="summary"
            v-model="form.summary"
            class="min-h-[110px]"
            maxlength="280"
            :placeholder="t('postEditor.summaryPlaceholder')"
          />
          <p v-if="aiSummaryHint" class="text-muted mt-2 text-xs">{{ aiSummaryHint }}</p>
          <p v-if="aiSummaryError" class="mt-2 text-xs text-red-500">{{ aiSummaryError }}</p>
        </div>

        <div class="space-y-2">
          <UiLabel for="coverUrl">{{ t('postEditor.coverUrl') }}</UiLabel>
          <UiInput
            id="coverUrl"
            v-model="form.coverUrl"
            maxlength="1024"
            :placeholder="t('postEditor.coverUrlPlaceholder')"
            type="url"
          />
          <input
            id="coverUpload"
            accept="image/png,image/jpeg,image/webp,image/gif"
            class="flex h-12 w-full rounded-2xl border border-[var(--panel-border)] bg-[var(--field-bg)] px-4 py-3 text-sm text-[var(--title)] transition file:mr-3 file:rounded-md file:border-0 file:bg-[var(--secondary-bg)] file:px-3 file:py-1.5 file:text-xs file:font-medium file:text-[var(--title)] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[color-mix(in_srgb,var(--brand)_16%,transparent)] disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="coverUploading || submitting"
            type="file"
            @change="uploadCover"
          >
          <p v-if="coverUploading" class="text-muted text-xs">
            {{ t('postEditor.uploadingCover') }}
          </p>
          <p v-if="coverUploadError" class="text-sm text-red-500">{{ coverUploadError }}</p>
          <img
            v-if="form.coverUrl"
            :alt="t('postEditor.coverPreview')"
            class="max-h-56 w-full rounded-md border border-[var(--panel-border)] object-cover"
            :src="form.coverUrl"
          >
        </div>

        <div class="grid gap-5 md:grid-cols-2">
          <div>
            <UiLabel for="category">{{ t('postEditor.category') }}</UiLabel>
            <UiInput
              id="category"
              v-model="form.category"
              maxlength="40"
              :placeholder="t('postEditor.categoryPlaceholder')"
              type="text"
            />
          </div>

          <div>
            <UiLabel for="tags">{{ t('postEditor.tags') }}</UiLabel>
            <UiInput
              id="tags"
              v-model="tagText"
              maxlength="280"
              :placeholder="t('postEditor.tagsPlaceholder')"
              type="text"
            />
          </div>
        </div>

        <div>
          <UiLabel for="content">{{ t('postEditor.content') }}</UiLabel>
          <UiTextarea
            id="content"
            v-model="form.contentMarkdown"
            class="min-h-[380px] font-mono text-[13px] leading-7"
            :placeholder="t('postEditor.contentPlaceholder')"
            required
          />
        </div>

        <div class="flex flex-wrap gap-3">
          <UiButton :disabled="submitting" type="submit">
            <PencilSquareIcon aria-hidden="true" class="size-4" />
            {{ submitting ? t('common.saving') : submitLabel }}
          </UiButton>
          <NuxtLink :class="buttonVariants({ variant: 'secondary' })" :to="backTo">
            <ArrowLeftIcon aria-hidden="true" class="size-4" />
            {{ resolvedBackLabel }}
          </NuxtLink>
        </div>
      </div>

      <div class="rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-soft-bg)] p-6">
        <p class="text-subtle text-sm uppercase tracking-[0.24em]">{{ t('common.preview') }}</p>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <article class="prose-blog mt-6" v-html="previewHtml" />
      </div>
    </form>
  </section>
</template>
