<script setup lang="ts">
import {
  BookOpenIcon,
  ExclamationTriangleIcon,
  PencilSquareIcon,
  PlusIcon,
  TrashIcon,
} from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type { PostSummary } from '~/types/blogi'
import { formatDateTime } from '~/utils/date'
import { getErrorMessage } from '~/utils/errors'

const route = useRoute()
const auth = useAuth()

if (!auth.isAuthenticated.value) {
  await navigateTo(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
}

const api = useApiClient()
const deletingId = ref<number | null>(null)
const actionError = ref('')

const {
  data: posts,
  pending,
  error,
  refresh,
} = await useAsyncData('admin-posts', () => api<PostSummary[]>('/posts/mine'), {
  immediate: auth.isAuthenticated.value,
})

const sortedPosts = computed(() =>
  [...(posts.value ?? [])].sort(
    (left, right) => new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime(),
  ),
)

async function removePost(post: PostSummary) {
  if (!import.meta.client) {
    return
  }

  if (!window.confirm(`确定删除《${post.title}》吗？`)) {
    return
  }

  deletingId.value = post.id
  actionError.value = ''

  try {
    await api<null>(`/posts/${post.id}`, { method: 'DELETE' })
    await refresh()
  } catch (deleteError) {
    actionError.value = getErrorMessage(deleteError)
  } finally {
    deletingId.value = null
  }
}

async function openPostEditor(post: PostSummary) {
  await navigateTo(`/admin/posts/${post.id}/edit`)
}
</script>

<template>
  <AdminShell title="文章管理" description="管理已发布文章，进入编辑器更新内容，或创建新的文章。">
    <UiAlert v-if="error || actionError" class="mb-5" variant="destructive">
      <UiAlertDescription class="flex items-start gap-2">
        <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
        <span>{{ actionError || '文章列表加载失败，请确认后端已启动。' }}</span>
      </UiAlertDescription>
    </UiAlert>

    <section
      class="rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] backdrop-blur"
    >
      <div
        class="flex flex-col gap-3 border-b border-[var(--panel-border)] px-5 py-4 md:flex-row md:items-center md:justify-between"
      >
        <div>
          <h2 class="text-title text-lg font-semibold">我的文章</h2>
          <p class="text-muted mt-1 text-sm">共 {{ sortedPosts.length }} 篇文章</p>
        </div>
        <NuxtLink :class="buttonVariants({ size: 'sm' })" to="/admin/posts/new">
          <PlusIcon aria-hidden="true" class="size-4" />
          新增文章
        </NuxtLink>
      </div>

      <div v-if="pending" class="px-5 py-8 text-sm text-[var(--body)]">文章加载中...</div>

      <div v-else-if="!sortedPosts.length" class="px-5 py-8">
        <p class="text-title text-base font-medium">还没有文章。</p>
        <p class="text-muted mt-2 text-sm">可以从左侧菜单或右上角按钮开始写第一篇。</p>
      </div>

      <div v-else class="divide-y divide-[var(--panel-border)]">
        <article
          v-for="post in sortedPosts"
          :key="post.id"
          class="grid cursor-pointer gap-4 px-5 py-5 transition hover:bg-[var(--secondary-bg-hover)] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[color-mix(in_srgb,var(--brand)_16%,transparent)] md:grid-cols-[minmax(0,1fr)_auto] md:items-center"
          role="link"
          tabindex="0"
          @click="openPostEditor(post)"
          @keydown.enter="openPostEditor(post)"
          @keydown.space.prevent="openPostEditor(post)"
        >
          <div class="min-w-0">
            <div class="meta-row"><span>{{ formatDateTime(post.updatedAt) }}</span></div>
            <h3 class="text-title mt-2 truncate text-xl font-semibold">{{ post.title }}</h3>
            <p class="text-body mt-2 line-clamp-2 text-sm leading-7">{{ post.summary }}</p>
          </div>

          <div class="flex flex-wrap gap-2">
            <NuxtLink
              :class="buttonVariants({ variant: 'secondary', size: 'sm' })"
              :to="`/posts/${post.id}`"
              @click.stop
            >
              <BookOpenIcon aria-hidden="true" class="size-4" />
              查看
            </NuxtLink>
            <NuxtLink
              :class="buttonVariants({ size: 'sm' })"
              :to="`/admin/posts/${post.id}/edit`"
              @click.stop
            >
              <PencilSquareIcon aria-hidden="true" class="size-4" />
              编辑
            </NuxtLink>
            <UiButton
              :disabled="deletingId === post.id"
              size="sm"
              type="button"
              variant="destructive"
              @click.stop="removePost(post)"
            >
              <TrashIcon aria-hidden="true" class="size-4" />
              {{ deletingId === post.id ? '删除中...' : '删除' }}
            </UiButton>
          </div>
        </article>
      </div>
    </section>
  </AdminShell>
</template>
