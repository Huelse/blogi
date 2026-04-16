<script setup lang="ts">
import { BookOpenIcon, ExclamationTriangleIcon } from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type { PostSummary } from '~/types/blogi'
import { formatDateTime } from '~/utils/date'

const api = useApiClient()

const {
  data: posts,
  pending,
  error,
} = await useAsyncData('posts', () => api<PostSummary[]>('/posts'))
const sortedPosts = computed(() =>
  [...(posts.value ?? [])].sort(
    (left, right) => new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime(),
  ),
)
</script>

<template>
  <main class="relative">
    <section class="mx-auto max-w-6xl px-6 py-12 md:py-16">
      <section>
        <UiAlert v-if="error" class="px-6 py-5" variant="destructive">
          <UiAlertDescription class="flex items-start gap-2">
            <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
            <span>文章列表加载失败，请确认后端已启动。</span>
          </UiAlertDescription>
        </UiAlert>

        <div
          v-else-if="pending"
          class="border-t border-[var(--panel-border)] py-8 text-sm text-[var(--body)]"
        >
          文章列表加载中...
        </div>

        <div
          v-else-if="!sortedPosts.length"
          class="border-t border-[var(--panel-border)] py-8 text-sm leading-7 text-[var(--body)]"
        >
          还没有文章。
        </div>

        <div v-else class="border-t border-[var(--panel-border)]">
          <article
            v-for="post in sortedPosts"
            :key="post.id"
            class="grid gap-4 border-b border-[var(--panel-border)] py-8 md:grid-cols-[minmax(0,1fr)_auto] md:gap-8"
          >
            <div>
              <div class="meta-row">
                <span>{{ post.author.displayName }}</span>
                <span>{{ formatDateTime(post.updatedAt) }}</span>
              </div>
              <h2 class="text-title mt-4 text-2xl font-semibold tracking-tight md:text-3xl">
                {{ post.title }}
              </h2>
              <p class="text-body mt-4 max-w-3xl text-sm leading-7 md:text-base">
                {{ post.summary }}
              </p>
            </div>

            <div class="flex items-start md:items-center">
              <NuxtLink
                :class="buttonVariants({ variant: 'secondary', size: 'sm' })"
                :to="`/posts/${post.id}`"
              >
                <BookOpenIcon aria-hidden="true" class="size-4" />
                阅读全文
              </NuxtLink>
            </div>
          </article>
        </div>
      </section>
    </section>
  </main>
</template>
