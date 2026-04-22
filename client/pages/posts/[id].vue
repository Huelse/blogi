<script setup lang="ts">
import {
  ArrowLeftIcon,
  ChatBubbleLeftEllipsisIcon,
  ExclamationTriangleIcon,
  FolderIcon,
  PaperAirplaneIcon,
  PencilSquareIcon,
  TagIcon,
  TrashIcon,
} from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type { CommentPayload, PostComment, PostDetail } from '~/types/blogi'
import { formatDateTime } from '~/utils/date'
import { getErrorMessage } from '~/utils/errors'
import { renderMarkdown } from '~/utils/markdown'

const route = useRoute()
const api = useApiClient()
const auth = useAuth()

const postId = computed(() => String(route.params.id))
const commentContent = ref('')
const commentPending = ref(false)
const commentError = ref('')
const deletingCommentId = ref<number | null>(null)

const { data: post, error } = await useAsyncData(
  () => `post-${postId.value}`,
  () => api<PostDetail>(`/posts/${postId.value}`),
)
const {
  data: comments,
  error: commentsError,
  refresh: refreshComments,
} = await useAsyncData(
  () => `post-${postId.value}-comments`,
  () => api<PostComment[]>(`/posts/${postId.value}/comments`),
)

const html = computed(() => renderMarkdown(post.value?.contentMarkdown ?? ''))
const isOwner = computed(() => post.value?.author.id === auth.user.value?.id)
const loginTarget = computed(() => `/login?redirect=${encodeURIComponent(route.fullPath)}`)

async function submitComment() {
  const content = commentContent.value.trim()
  if (!content) {
    return
  }

  commentPending.value = true
  commentError.value = ''

  try {
    await api<PostComment>(`/posts/${postId.value}/comments`, {
      method: 'POST',
      body: { content } satisfies CommentPayload,
    })
    commentContent.value = ''
    await refreshComments()
    syncCommentCount()
  } catch (saveError) {
    commentError.value = getErrorMessage(saveError)
  } finally {
    commentPending.value = false
  }
}

async function removeComment(comment: PostComment) {
  if (!import.meta.client) {
    return
  }

  if (!window.confirm('确定删除这条评论吗？')) {
    return
  }

  deletingCommentId.value = comment.id
  commentError.value = ''

  try {
    await api<null>(`/posts/${postId.value}/comments/${comment.id}`, { method: 'DELETE' })
    await refreshComments()
    syncCommentCount()
  } catch (deleteError) {
    commentError.value = getErrorMessage(deleteError)
  } finally {
    deletingCommentId.value = null
  }
}

function canDeleteComment(comment: PostComment) {
  const currentUserId = auth.user.value?.id
  return currentUserId === comment.author.id || currentUserId === post.value?.author.id
}

function syncCommentCount() {
  if (post.value) {
    post.value.commentCount = comments.value?.length ?? post.value.commentCount
  }
}
</script>

<template>
  <main class="relative">
    <section class="mx-auto max-w-5xl px-6 py-16">
      <UiAlert v-if="error || !post" variant="destructive">
        <UiAlertDescription class="flex items-start gap-2">
          <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
          <span>文章不存在，或后端尚未启动。</span>
        </UiAlertDescription>
      </UiAlert>
      <UiCard v-else class="overflow-hidden">
        <div class="px-8 py-8 md:px-10">
          <div class="meta-row">
            <span>{{ post.author.displayName }}</span>
            <span>{{ formatDateTime(post.updatedAt) }}</span>
            <span class="inline-flex items-center gap-1.5">
              <ChatBubbleLeftEllipsisIcon aria-hidden="true" class="size-4" />
              {{ post.commentCount }}
            </span>
          </div>
          <h1 class="text-title mt-5 text-4xl font-semibold tracking-tight md:text-5xl">
            {{ post.title }}
          </h1>
          <p class="text-body mt-5 max-w-3xl text-lg leading-8">{{ post.summary }}</p>

          <div v-if="post.category || post.tags?.length" class="mt-6 flex flex-wrap gap-2">
            <NuxtLink
              v-if="post.category"
              class="h-auto min-h-10 max-w-full !whitespace-normal break-words text-left"
              :class="buttonVariants({ variant: 'secondary', size: 'sm' })"
              :to="{ path: '/', query: { category: post.category.slug } }"
            >
              <FolderIcon aria-hidden="true" class="size-4" />
              {{ post.category.name }}
            </NuxtLink>
            <NuxtLink
              v-for="tag in post.tags"
              :key="tag.id"
              class="h-auto min-h-10 max-w-full !whitespace-normal break-words text-left"
              :class="buttonVariants({ variant: 'secondary', size: 'sm' })"
              :to="{ path: '/', query: { tag: tag.slug } }"
            >
              <TagIcon aria-hidden="true" class="size-4" />
              {{ tag.name }}
            </NuxtLink>
          </div>

          <div class="mt-8 flex flex-wrap gap-3">
            <NuxtLink :class="buttonVariants({ variant: 'secondary' })" to="/">
              <ArrowLeftIcon aria-hidden="true" class="size-4" />
              返回列表
            </NuxtLink>
            <NuxtLink v-if="isOwner" :class="buttonVariants()" :to="`/admin/posts/${post.id}/edit`">
              <PencilSquareIcon aria-hidden="true" class="size-4" />
              后台编辑
            </NuxtLink>
          </div>
        </div>

        <UiSeparator />

        <div class="px-8 py-10 md:px-10">
          <!-- eslint-disable-next-line vue/no-v-html -->
          <article class="prose-blog" v-html="html" />
        </div>

        <UiSeparator />

        <div class="px-8 py-10 md:px-10">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p class="text-subtle text-sm uppercase tracking-[0.24em]">Comments</p>
              <h2 class="text-title mt-2 text-2xl font-semibold">评论</h2>
            </div>
            <UiBadge variant="muted">{{ comments?.length ?? post.commentCount }} 条</UiBadge>
          </div>

          <UiAlert v-if="commentsError || commentError" class="mt-6" variant="destructive">
            <UiAlertDescription class="flex items-start gap-2">
              <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
              <span>{{ commentError || '评论加载失败，请稍后重试。' }}</span>
            </UiAlertDescription>
          </UiAlert>

          <form
            v-if="auth.isAuthenticated.value"
            class="mt-6 space-y-3"
            @submit.prevent="submitComment"
          >
            <UiLabel for="comment">发表评论</UiLabel>
            <UiTextarea
              id="comment"
              v-model="commentContent"
              class="min-h-[120px]"
              maxlength="1200"
              placeholder="写下你的评论"
              required
            />
            <UiButton :disabled="commentPending || !commentContent.trim()" type="submit">
              <PaperAirplaneIcon aria-hidden="true" class="size-4" />
              {{ commentPending ? '发布中...' : '发布评论' }}
            </UiButton>
          </form>

          <div
            v-else
            class="mt-6 rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-soft-bg)] px-5 py-4 text-sm text-[var(--body)]"
          >
            <NuxtLink :class="buttonVariants({ variant: 'link' })" :to="loginTarget">
              登录
            </NuxtLink>
            后参与评论。
          </div>

          <div
            v-if="!comments?.length"
            class="mt-8 border-t border-[var(--panel-border)] pt-6 text-sm text-[var(--muted)]"
          >
            暂无评论。
          </div>

          <div v-else class="mt-8 divide-y divide-[var(--panel-border)]">
            <article v-for="comment in comments" :key="comment.id" class="py-5">
              <div class="flex flex-wrap items-start justify-between gap-3">
                <div class="meta-row">
                  <span>{{ comment.author.displayName }}</span>
                  <span>{{ formatDateTime(comment.createdAt) }}</span>
                </div>
                <UiButton
                  v-if="canDeleteComment(comment)"
                  :disabled="deletingCommentId === comment.id"
                  size="sm"
                  type="button"
                  variant="destructive"
                  @click="removeComment(comment)"
                >
                  <TrashIcon aria-hidden="true" class="size-4" />
                  {{ deletingCommentId === comment.id ? '删除中...' : '删除' }}
                </UiButton>
              </div>
              <p class="text-body mt-3 whitespace-pre-wrap text-sm leading-7">
                {{ comment.content }}
              </p>
            </article>
          </div>
        </div>
      </UiCard>
    </section>

    <SiteFooter />
  </main>
</template>
