<script setup lang="ts">
import type { PostDetail } from '~/types/blogi'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
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
    <UiAlert v-if="error || !post" variant="destructive">
      <UiAlertDescription>文章不存在，或后端尚未启动。</UiAlertDescription>
    </UiAlert>
    <UiCard v-else class="overflow-hidden">
      <div class="px-8 py-8 md:px-10">
        <div class="meta-row">
          <span>{{ post.author.displayName }}</span>
          <span>{{ formatDateTime(post.updatedAt) }}</span>
        </div>
        <h1 class="text-title mt-5 text-4xl font-semibold tracking-tight md:text-5xl">{{ post.title }}</h1>
        <p class="text-body mt-5 max-w-3xl text-lg leading-8">{{ post.summary }}</p>

        <div class="mt-8 flex flex-wrap gap-3">
          <NuxtLink :class="buttonVariants({ variant: 'secondary' })" to="/">返回列表</NuxtLink>
          <NuxtLink v-if="isOwner" :class="buttonVariants()" :to="`/posts/${post.id}/edit`">编辑文章</NuxtLink>
          <UiButton v-if="isOwner" :disabled="deleting" type="button" variant="destructive" @click="removePost">
            {{ deleting ? '删除中...' : '删除文章' }}
          </UiButton>
        </div>

        <UiAlert v-if="actionError" class="mt-5" variant="destructive">
          <UiAlertDescription>{{ actionError }}</UiAlertDescription>
        </UiAlert>
      </div>

      <UiSeparator />

      <div class="px-8 py-10 md:px-10">
        <!-- eslint-disable-next-line vue/no-v-html -->
        <article class="prose-blog" v-html="html" />
      </div>
    </UiCard>
  </main>
</template>
