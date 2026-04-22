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
    backLabel: '返回列表',
    submitting: false,
  },
)

const emit = defineEmits<{
  save: [payload: PostPayload]
}>()

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
  renderMarkdown(form.contentMarkdown || '## 预览\n\n在这里输入 Markdown 正文。'),
)

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
          <UiLabel for="title">标题</UiLabel>
          <UiInput id="title" v-model="form.title" maxlength="120" required type="text" />
        </div>

        <div>
          <UiLabel for="summary">摘要</UiLabel>
          <UiTextarea
            id="summary"
            v-model="form.summary"
            class="min-h-[110px]"
            maxlength="280"
            placeholder="可选，不填时后端会从正文自动生成摘要"
          />
        </div>

        <div class="grid gap-5 md:grid-cols-2">
          <div>
            <UiLabel for="category">分类</UiLabel>
            <UiInput
              id="category"
              v-model="form.category"
              maxlength="40"
              placeholder="例如：工程日志"
              type="text"
            />
          </div>

          <div>
            <UiLabel for="tags">标签</UiLabel>
            <UiInput
              id="tags"
              v-model="tagText"
              maxlength="280"
              placeholder="用逗号分隔，例如：Nuxt, Spring"
              type="text"
            />
          </div>
        </div>

        <div>
          <UiLabel for="content">Markdown 正文</UiLabel>
          <UiTextarea
            id="content"
            v-model="form.contentMarkdown"
            class="min-h-[380px] font-mono text-[13px] leading-7"
            placeholder="# 标题"
            required
          />
        </div>

        <div class="flex flex-wrap gap-3">
          <UiButton :disabled="submitting" type="submit">
            <PencilSquareIcon aria-hidden="true" class="size-4" />
            {{ submitting ? '提交中...' : submitLabel }}
          </UiButton>
          <NuxtLink :class="buttonVariants({ variant: 'secondary' })" :to="backTo">
            <ArrowLeftIcon aria-hidden="true" class="size-4" />
            {{ backLabel }}
          </NuxtLink>
        </div>
      </div>

      <div class="rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-soft-bg)] p-6">
        <p class="text-subtle text-sm uppercase tracking-[0.24em]">Preview</p>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <article class="prose-blog mt-6" v-html="previewHtml" />
      </div>
    </form>
  </section>
</template>
