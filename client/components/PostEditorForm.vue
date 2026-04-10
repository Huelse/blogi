<script setup lang="ts">
import type { PostPayload } from '~/types/blogi'
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
  <section class="panel p-8">
    <div class="max-w-2xl">
      <p class="text-sm uppercase tracking-[0.3em] text-amber-300">{{ title }}</p>
      <p class="mt-4 text-sm leading-7 text-stone-400">{{ description }}</p>
    </div>

    <form class="mt-8 grid gap-8 lg:grid-cols-[1.05fr_0.95fr]" @submit.prevent="submit">
      <div class="space-y-5">
        <div>
          <label class="mb-2 block text-sm text-stone-300" for="title">标题</label>
          <input id="title" v-model="form.title" class="field" maxlength="120" required type="text">
        </div>

        <div>
          <label class="mb-2 block text-sm text-stone-300" for="summary">摘要</label>
          <textarea
            id="summary"
            v-model="form.summary"
            class="field min-h-[110px]"
            maxlength="280"
            placeholder="可选，不填时后端会从正文自动生成摘要"
          />
        </div>

        <div>
          <label class="mb-2 block text-sm text-stone-300" for="content">Markdown 正文</label>
          <textarea
            id="content"
            v-model="form.contentMarkdown"
            class="field min-h-[380px] font-mono text-[13px] leading-7"
            placeholder="# 标题"
            required
          />
        </div>

        <div class="flex flex-wrap gap-3">
          <button class="btn-primary" :disabled="submitting" type="submit">
            {{ submitting ? '提交中...' : submitLabel }}
          </button>
          <NuxtLink class="btn-secondary" to="/">返回列表</NuxtLink>
        </div>
      </div>

      <div class="rounded-[24px] border border-white/10 bg-black/25 p-6">
        <p class="text-sm uppercase tracking-[0.24em] text-stone-500">Preview</p>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <article class="prose-blog mt-6" v-html="previewHtml" />
      </div>
    </form>
  </section>
</template>
