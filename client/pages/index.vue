<script setup lang="ts">
import { BookOpenIcon, ExclamationTriangleIcon } from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type { PostSummary } from '~/types/blogi'
import { formatDateTime } from '~/utils/date'

const api = useApiClient()
const blogSettings = useBlogSettings()

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
const isCardLayout = computed(() => blogSettings.postListLayout.value === 'card')
</script>

<template>
  <main class="relative">
    <section class="mx-auto max-w-6xl px-6 py-12 md:py-16">
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

      <div v-else-if="isCardLayout" class="grid gap-4 md:grid-cols-2">
        <NuxtLink
          v-for="post in sortedPosts"
          :key="post.id"
          class="flex min-h-[260px] flex-col rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] p-5 transition hover:border-[var(--secondary-border-hover)] hover:bg-[var(--secondary-bg-hover)] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[color-mix(in_srgb,var(--brand)_16%,transparent)]"
          :to="`/posts/${post.id}`"
        >
          <h2 class="text-title line-clamp-2 text-2xl font-semibold tracking-tight">
            {{ post.title }}
          </h2>
          <p class="text-body mt-4 line-clamp-3 text-sm leading-7">{{ post.summary }}</p>

          <div class="mt-auto flex flex-col gap-4 pt-6 sm:flex-row sm:items-end sm:justify-between">
            <div class="meta-row order-2 sm:order-1">
              <span>{{ post.author.displayName }}</span>
              <span>{{ formatDateTime(post.updatedAt) }}</span>
            </div>
            <span
              :class="buttonVariants({ variant: 'secondary', size: 'sm' })"
              aria-hidden="true"
              class="order-1 self-start sm:order-2 sm:self-auto"
            >
              <BookOpenIcon aria-hidden="true" class="size-4" />
              阅读全文
            </span>
          </div>
        </NuxtLink>
      </div>

      <div v-else class="border-t border-[var(--panel-border)]">
        <NuxtLink
          v-for="post in sortedPosts"
          :key="post.id"
          class="-mx-3 grid gap-4 border-b border-[var(--panel-border)] px-3 py-8 transition hover:bg-[var(--secondary-bg-hover)] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[color-mix(in_srgb,var(--brand)_16%,transparent)] md:grid-cols-[minmax(0,1fr)_auto] md:gap-8"
          :to="`/posts/${post.id}`"
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
            <span :class="buttonVariants({ variant: 'secondary', size: 'sm' })" aria-hidden="true">
              <BookOpenIcon aria-hidden="true" class="size-4" />
              阅读全文
            </span>
          </div>
        </NuxtLink>
      </div>
    </section>

    <SiteFooter />
  </main>
</template>
