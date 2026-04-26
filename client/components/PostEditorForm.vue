<script setup lang="ts">
import { ArrowLeftIcon, PencilSquareIcon } from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type { PostCategory, PostPayload, PostTag } from '~/types/blogi'
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

const form = reactive<PostPayload>({
  title: '',
  summary: '',
  contentMarkdown: '',
  category: '',
  tags: [],
})
const tagText = ref('')

watch(
  () => props.initialValue,
  (value) => {
    form.title = value.title ?? ''
    form.summary = value.summary ?? ''
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
    contentMarkdown: form.contentMarkdown,
    category: form.category.trim(),
    tags: parseTags(tagText.value),
  })
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
          <UiLabel for="summary">{{ t('postEditor.summary') }}</UiLabel>
          <UiTextarea
            id="summary"
            v-model="form.summary"
            class="min-h-[110px]"
            maxlength="280"
            :placeholder="t('postEditor.summaryPlaceholder')"
          />
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
