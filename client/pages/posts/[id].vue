<script setup lang="ts">
import {
  ArrowLeftIcon,
  ChatBubbleLeftEllipsisIcon,
  ExclamationTriangleIcon,
  EyeIcon,
  FolderIcon,
  HeartIcon,
  PaperAirplaneIcon,
  PencilSquareIcon,
  TagIcon,
  TrashIcon,
} from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type {
  CommentPayload,
  PostComment,
  PostDetail,
  PostLikePayload,
  PostLikeState,
  PostViewPayload,
  PostViewState,
} from '~/types/blogi'
import { formatDateTime } from '~/utils/date'
import { getErrorMessage } from '~/utils/errors'
import { renderMarkdown } from '~/utils/markdown'

const route = useRoute()
const api = useApiClient()
const auth = useAuth()
const visitor = useVisitorIdentity()
const { locale, t } = useI18n()

const postId = computed(() => String(route.params.id))
const commentContent = ref('')
const commentPending = ref(false)
const commentError = ref('')
const deletingCommentId = ref<number | null>(null)
const likeState = ref<PostLikeState | null>(null)
const likePending = ref(false)
const likeError = ref('')
const profileDialogOpen = ref(false)
const pendingVisitorAction = ref<'comment' | 'like' | null>(null)

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
const likeCount = computed(() => likeState.value?.likeCount ?? post.value?.likeCount ?? 0)
const viewCount = computed(() => post.value?.viewCount ?? 0)
const liked = computed(() => likeState.value?.liked ?? false)

onMounted(async () => {
  await Promise.allSettled([visitor.loadProfile(), refreshLikeState(), trackView()])
})

async function submitComment() {
  const content = commentContent.value.trim()
  if (!content) {
    return
  }

  if (!(await ensureVisitorProfile('comment'))) {
    return
  }

  commentPending.value = true
  commentError.value = ''

  try {
    const fingerprintHash = await visitor.ensureFingerprintHash()
    await api<PostComment>(`/posts/${postId.value}/comments`, {
      method: 'POST',
      body: { fingerprintHash, content } satisfies CommentPayload,
    })
    commentContent.value = ''
    await refreshComments()
    syncCommentCount()
  } catch (saveError) {
    commentError.value = getErrorMessage(saveError, t('common.requestFailed'))
  } finally {
    commentPending.value = false
  }
}

async function toggleLike() {
  if (likePending.value) {
    return
  }

  if (!(await ensureVisitorProfile('like'))) {
    return
  }

  likePending.value = true
  likeError.value = ''

  try {
    const fingerprintHash = await visitor.ensureFingerprintHash()
    const state = await api<PostLikeState>(`/posts/${postId.value}/likes`, {
      method: liked.value ? 'DELETE' : 'POST',
      body: { fingerprintHash } satisfies PostLikePayload,
    })
    applyLikeState(state)
  } catch (error) {
    likeError.value = getErrorMessage(error, t('common.requestFailed'))
  } finally {
    likePending.value = false
  }
}

async function removeComment(comment: PostComment) {
  if (!import.meta.client) {
    return
  }

  if (!window.confirm(t('post.deleteCommentConfirm'))) {
    return
  }

  deletingCommentId.value = comment.id
  commentError.value = ''

  try {
    await api<null>(`/posts/${postId.value}/comments/${comment.id}`, { method: 'DELETE' })
    await refreshComments()
    syncCommentCount()
  } catch (deleteError) {
    commentError.value = getErrorMessage(deleteError, t('common.requestFailed'))
  } finally {
    deletingCommentId.value = null
  }
}

function canDeleteComment(comment: PostComment) {
  const currentUserId = auth.user.value?.id
  const isVisitorComment = comment.author.username.startsWith('visitor-')
  return (
    currentUserId === post.value?.author.id ||
    (!isVisitorComment && currentUserId === comment.author.id)
  )
}

function syncCommentCount() {
  if (post.value) {
    post.value.commentCount = comments.value?.length ?? post.value.commentCount
  }
}

async function refreshLikeState() {
  if (!import.meta.client) {
    return
  }

  const fingerprintHash = await visitor.ensureFingerprintHash()
  const state = await api<PostLikeState>(`/posts/${postId.value}/likes`, {
    query: { fingerprintHash },
  })
  applyLikeState(state)
}

async function trackView() {
  if (!post.value || !import.meta.client) {
    return
  }

  const fingerprintHash = await visitor.ensureFingerprintHash()
  const state = await api<PostViewState>(`/posts/${postId.value}/views`, {
    method: 'POST',
    body: { fingerprintHash } satisfies PostViewPayload,
  })
  post.value.viewCount = state.viewCount
}

function applyLikeState(state: PostLikeState) {
  likeState.value = state
  if (post.value) {
    post.value.likeCount = state.likeCount
  }
}

async function ensureVisitorProfile(action: 'comment' | 'like') {
  try {
    const profile = await visitor.loadProfile(true)
    if (profile) {
      return true
    }

    pendingVisitorAction.value = action
    profileDialogOpen.value = true
    return false
  } catch (error) {
    const message = getErrorMessage(error, t('common.requestFailed'))
    if (action === 'comment') {
      commentError.value = message
    } else {
      likeError.value = message
    }
    return false
  }
}

async function handleVisitorProfileSaved() {
  const action = pendingVisitorAction.value
  pendingVisitorAction.value = null

  if (action === 'comment') {
    await submitComment()
    return
  }

  if (action === 'like') {
    await toggleLike()
  }
}
</script>

<template>
  <main class="relative">
    <section class="mx-auto max-w-5xl px-6 py-16">
      <UiAlert v-if="error || !post" variant="destructive">
        <UiAlertDescription class="flex items-start gap-2">
          <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
          <span>{{ t('post.loadError') }}</span>
        </UiAlertDescription>
      </UiAlert>
      <UiCard v-else class="overflow-hidden">
        <div class="px-8 py-8 md:px-10">
          <div class="meta-row">
            <span>{{ post.author.displayName }}</span>
            <span>{{ formatDateTime(post.updatedAt, locale) }}</span>
            <span class="inline-flex items-center gap-1.5">
              <ChatBubbleLeftEllipsisIcon aria-hidden="true" class="size-4" />
              {{ post.commentCount }}
            </span>
            <span class="inline-flex items-center gap-1.5">
              <HeartIcon aria-hidden="true" class="size-4" />
              {{ likeCount }}
            </span>
            <span class="inline-flex items-center gap-1.5">
              <EyeIcon aria-hidden="true" class="size-4" />
              {{ viewCount }}
            </span>
          </div>
          <h1 class="text-title mt-5 text-4xl font-semibold tracking-tight md:text-5xl">
            {{ post.title }}
          </h1>
          <p class="text-body mt-5 max-w-3xl text-lg leading-8">{{ post.summary }}</p>
          <img
            v-if="post.coverUrl"
            :alt="post.title"
            class="mt-6 max-h-[360px] w-full rounded-md border border-[var(--panel-border)] object-cover"
            :src="post.coverUrl"
          />

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
              {{ t('post.backToList') }}
            </NuxtLink>
            <UiButton
              :disabled="likePending"
              :variant="liked ? 'default' : 'secondary'"
              type="button"
              @click="toggleLike"
            >
              <HeartIcon aria-hidden="true" class="size-4" />
              {{ likePending ? t('post.liking') : liked ? t('post.liked') : t('post.like') }}
            </UiButton>
            <NuxtLink v-if="isOwner" :class="buttonVariants()" :to="`/admin/posts/${post.id}/edit`">
              <PencilSquareIcon aria-hidden="true" class="size-4" />
              {{ t('post.editInAdmin') }}
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
              <p class="text-subtle text-sm uppercase tracking-[0.24em]">
                {{ t('post.comments.eyebrow') }}
              </p>
              <h2 class="text-title mt-2 text-2xl font-semibold">{{ t('post.comments.title') }}</h2>
            </div>
            <UiBadge variant="muted">
              {{ t('post.comments.count', { count: comments?.length ?? post.commentCount }) }}
            </UiBadge>
          </div>

          <UiAlert
            v-if="commentsError || commentError || likeError"
            class="mt-6"
            variant="destructive"
          >
            <UiAlertDescription class="flex items-start gap-2">
              <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
              <span>{{ commentError || likeError || t('post.comments.error') }}</span>
            </UiAlertDescription>
          </UiAlert>

          <form class="mt-6 space-y-3" @submit.prevent="submitComment">
            <UiLabel for="comment">{{ t('post.comments.write') }}</UiLabel>
            <UiTextarea
              id="comment"
              v-model="commentContent"
              class="min-h-[120px]"
              maxlength="1200"
              :placeholder="t('post.commentPlaceholder')"
              required
            />
            <UiButton :disabled="commentPending || !commentContent.trim()" type="submit">
              <PaperAirplaneIcon aria-hidden="true" class="size-4" />
              {{ commentPending ? t('post.commentSubmitting') : t('post.commentSubmit') }}
            </UiButton>
          </form>

          <div
            v-if="!comments?.length"
            class="mt-8 border-t border-[var(--panel-border)] pt-6 text-sm text-[var(--muted)]"
          >
            {{ t('post.comments.empty') }}
          </div>

          <div v-else class="mt-8 divide-y divide-[var(--panel-border)]">
            <article v-for="comment in comments" :key="comment.id" class="py-5">
              <div class="flex flex-wrap items-start justify-between gap-3">
                <div class="meta-row flex items-center gap-2">
                  <img
                    v-if="comment.author.avatarUrl"
                    :alt="comment.author.displayName"
                    class="size-7 rounded-full border border-[var(--panel-border)] object-cover"
                    :src="comment.author.avatarUrl"
                  />
                  <span>{{ comment.author.displayName }}</span>
                  <span>{{ formatDateTime(comment.createdAt, locale) }}</span>
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
                  {{ deletingCommentId === comment.id ? t('common.deleting') : t('common.delete') }}
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

    <VisitorProfileDialog v-model:open="profileDialogOpen" @saved="handleVisitorProfileSaved" />
    <SiteFooter />
  </main>
</template>
