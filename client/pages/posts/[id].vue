<script setup lang="ts">
import type { PostDetail } from '~/types/blogi'
import { formatDateTime } from '~/utils/date'
import { getErrorMessage } from '~/utils/errors'
import { renderMarkdown } from '~/utils/markdown'

const route = useRoute()
const api = useApiClient()
const auth = useAuth()

const postId = computed(() => String(route.params.id))
const deleting = ref(false)
const actionError = ref('')

const { data: post, error } = await useAsyncData(
  () => `post-${postId.value}`,
  () => api<PostDetail>(`/posts/${postId.value}`)
)

const html = computed(() => renderMarkdown(post.value?.contentMarkdown ?? ''))
const isOwner = computed(() => post.value?.author.id === auth.user.value?.id)

async function removePost() {
  if (!post.value || !import.meta.client) {
    return
  }

  if (!window.confirm(`确定删除《${post.value.title}》吗？`)) {
    return
  }

  deleting.value = true
  actionError.value = ''

  try {
    await api<null>(`/posts/${post.value.id}`, { method: 'DELETE' })
    await navigateTo('/')
  } catch (deleteError) {
    actionError.value = getErrorMessage(deleteError)
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <main class="mx-auto max-w-5xl px-6 py-16">
    <div v-if="error || !post" class="panel px-6 py-8 text-rose-100">文章不存在，或后端尚未启动。</div>
    <article v-else class="panel overflow-hidden">
      <div class="border-b border-white/10 px-8 py-8 md:px-10">
        <div class="flex flex-wrap items-center gap-3 text-xs uppercase tracking-[0.24em] text-stone-500">
          <span>{{ post.author.displayName }}</span>
          <span>{{ formatDateTime(post.updatedAt) }}</span>
        </div>
        <h1 class="mt-5 text-4xl font-semibold tracking-tight text-white md:text-5xl">{{ post.title }}</h1>
        <p class="mt-5 max-w-3xl text-lg leading-8 text-stone-300">{{ post.summary }}</p>

        <div class="mt-8 flex flex-wrap gap-3">
          <NuxtLink class="btn-secondary" to="/">返回列表</NuxtLink>
          <NuxtLink v-if="isOwner" class="btn-primary" :to="`/posts/${post.id}/edit`">编辑文章</NuxtLink>
          <button
            v-if="isOwner"
            class="btn-secondary border-rose-300/30 text-rose-100 hover:border-rose-300/50"
            :disabled="deleting"
            type="button"
            @click="removePost"
          >
            {{ deleting ? '删除中...' : '删除文章' }}
          </button>
        </div>

        <p
          v-if="actionError"
          class="mt-5 rounded-2xl border border-rose-300/20 bg-rose-300/10 px-4 py-3 text-sm text-rose-100"
        >
          {{ actionError }}
        </p>
      </div>

      <div class="px-8 py-10 md:px-10">
        <!-- eslint-disable-next-line vue/no-v-html -->
        <article class="prose-blog" v-html="html" />
      </div>
    </article>
  </main>
</template>
