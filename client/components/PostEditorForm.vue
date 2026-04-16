<script setup lang="ts">
import { ArrowLeftIcon, PencilSquareIcon } from '@heroicons/vue/20/solid'
import type { PostPayload } from '~/types/blogi'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import { renderMarkdown } from '~/utils/markdown'

const props = withDefaults(defineProps<{
  initialValue?: Partial<PostPayload>
  title: string
  description: string
  submitLabel: string
  submitting?: boolean
}>(), {
  initialValue: () => ({}),
  submitting: false
})

const emit = defineEmits<{
  save: [payload: PostPayload]
}>()

const form = reactive<PostPayload>({
  title: '',
  summary: '',
  contentMarkdown: ''
})

watch(() => props.initialValue, (value) => {
  form.title = value.title ?? ''
  form.summary = value.summary ?? ''
  form.contentMarkdown = value.contentMarkdown ?? ''
}, { immediate: true, deep: true })

const previewHtml = computed(() => renderMarkdown(form.contentMarkdown || '## 预览\n\n在这里输入 Markdown 正文。'))

function submit() {
  emit('save', {
    title: form.title.trim(),
    summary: form.summary.trim(),
    contentMarkdown: form.contentMarkdown
  })
}
</script>

<template>
  <UiCard class="p-8">
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
          <NuxtLink :class="buttonVariants({ variant: 'secondary' })" to="/">
            <ArrowLeftIcon aria-hidden="true" class="size-4" />
            返回列表
          </NuxtLink>
        </div>
      </div>

      <UiCard class="p-6" tone="soft">
        <p class="text-subtle text-sm uppercase tracking-[0.24em]">Preview</p>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <article class="prose-blog mt-6" v-html="previewHtml" />
      </UiCard>
    </form>
  </UiCard>
</template>
