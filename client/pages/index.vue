<script setup lang="ts">
import {
  BookOpenIcon,
  ChatBubbleLeftEllipsisIcon,
  ExclamationTriangleIcon,
  FolderIcon,
  HeartIcon,
  TagIcon,
} from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type { PostCategory, PostSummary, PostTag } from '~/types/blogi'
import { formatDateTime } from '~/utils/date'

const route = useRoute()
const api = useApiClient()
const blogSettings = useBlogSettings()
const { locale, t } = useI18n()
const selectedCategorySlug = computed(() =>
  typeof route.query.category === 'string' ? route.query.category : '',
)
const selectedTagSlug = computed(() => (typeof route.query.tag === 'string' ? route.query.tag : ''))
const postsQuery = computed(() => ({
  ...(selectedCategorySlug.value ? { category: selectedCategorySlug.value } : {}),
  ...(selectedTagSlug.value ? { tag: selectedTagSlug.value } : {}),
}))

const {
  data: posts,
  pending,
  error,
} = await useAsyncData(
  () => `posts-${selectedCategorySlug.value || 'all'}-${selectedTagSlug.value || 'all'}`,
  () => api<PostSummary[]>('/posts', { query: postsQuery.value }),
  { watch: [selectedCategorySlug, selectedTagSlug] },
)
const { data: categories } = await useAsyncData('post-categories', () =>
  api<PostCategory[]>('/posts/categories'),
)
const { data: tags } = await useAsyncData('post-tags', () => api<PostTag[]>('/posts/tags'))
const sortedPosts = computed(() =>
  [...(posts.value ?? [])].sort(
    (left, right) => new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime(),
  ),
)
const isCardLayout = computed(() => blogSettings.postListLayout.value === 'card')
const hasActiveFilter = computed(() => Boolean(selectedCategorySlug.value || selectedTagSlug.value))

function filterTarget(filters: { category?: string | null; tag?: string | null }) {
  const category =
    filters.category === undefined ? selectedCategorySlug.value : (filters.category ?? '')
  const tag = filters.tag === undefined ? selectedTagSlug.value : (filters.tag ?? '')
  const query = {
    ...(category ? { category } : {}),
    ...(tag ? { tag } : {}),
  }

  return { path: '/', query }
}
</script>

<template>
  <main class="relative">
    <section class="mx-auto max-w-6xl px-6 py-12 md:py-16">
      <UiAlert v-if="error" class="px-6 py-5" variant="destructive">
        <UiAlertDescription class="flex items-start gap-2">
          <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
          <span>{{ t('home.loadError') }}</span>
        </UiAlertDescription>
      </UiAlert>

      <template v-else>
        <div
          v-if="categories?.length || tags?.length || hasActiveFilter"
          class="mb-8 rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] p-5"
        >
          <div class="flex flex-wrap items-center gap-2">
            <NuxtLink
              :class="
                buttonVariants({ variant: hasActiveFilter ? 'secondary' : 'default', size: 'sm' })
              "
              :to="{ path: '/' }"
            >
              <BookOpenIcon aria-hidden="true" class="size-4" />
              {{ t('home.allPosts') }}
            </NuxtLink>
          </div>

          <div v-if="categories?.length" class="mt-4 flex flex-wrap items-center gap-2">
            <span class="text-subtle inline-flex items-center gap-1.5 text-xs">
              <FolderIcon aria-hidden="true" class="size-4" />
              {{ t('home.categories') }}
            </span>
            <NuxtLink
              v-for="category in categories"
              :key="category.id"
              class="h-auto min-h-10 max-w-full !whitespace-normal break-words text-left"
              :class="
                buttonVariants({
                  variant: selectedCategorySlug === category.slug ? 'default' : 'secondary',
                  size: 'sm',
                })
              "
              :to="filterTarget({ category: category.slug })"
            >
              {{ category.name }}
            </NuxtLink>
          </div>

          <div v-if="tags?.length" class="mt-4 flex flex-wrap items-center gap-2">
            <span class="text-subtle inline-flex items-center gap-1.5 text-xs">
              <TagIcon aria-hidden="true" class="size-4" />
              {{ t('home.tags') }}
            </span>
            <NuxtLink
              v-for="tag in tags"
              :key="tag.id"
              class="h-auto min-h-10 max-w-full !whitespace-normal break-words text-left"
              :class="
                buttonVariants({
                  variant: selectedTagSlug === tag.slug ? 'default' : 'secondary',
                  size: 'sm',
                })
              "
              :to="filterTarget({ tag: tag.slug })"
            >
              {{ tag.name }}
            </NuxtLink>
          </div>
        </div>

        <div
          v-if="pending"
          class="border-t border-[var(--panel-border)] py-8 text-sm text-[var(--body)]"
        >
          {{ t('home.loading') }}
        </div>

        <div
          v-else-if="!sortedPosts.length"
          class="border-t border-[var(--panel-border)] py-8 text-sm leading-7 text-[var(--body)]"
        >
          {{ t('home.empty') }}
        </div>

        <div v-else-if="isCardLayout" class="grid gap-4 md:grid-cols-2">
          <NuxtLink
            v-for="post in sortedPosts"
            :key="post.id"
            class="flex min-h-[260px] flex-col overflow-hidden rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] transition hover:border-[var(--secondary-border-hover)] hover:bg-[var(--secondary-bg-hover)] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[color-mix(in_srgb,var(--brand)_16%,transparent)]"
            :to="`/posts/${post.id}`"
          >
            <img
              v-if="post.coverUrl"
              :alt="post.title"
              class="h-44 w-full border-b border-[var(--panel-border)] object-cover"
              :src="post.coverUrl"
            />
            <div class="p-5">
            <h2 class="text-title line-clamp-2 text-2xl font-semibold tracking-tight">
              {{ post.title }}
            </h2>
            <p class="text-body mt-4 line-clamp-3 text-sm leading-7">{{ post.summary }}</p>

            <div v-if="post.category || post.tags?.length" class="mt-4 flex flex-wrap gap-2">
              <UiBadge v-if="post.category" class="gap-1.5 px-3 py-1 text-xs" variant="muted">
                <FolderIcon aria-hidden="true" class="size-3.5" />
                {{ post.category.name }}
              </UiBadge>
              <UiBadge
                v-for="tag in post.tags"
                :key="tag.id"
                class="gap-1.5 px-3 py-1 text-xs"
                variant="outline"
              >
                <TagIcon aria-hidden="true" class="size-3.5" />
                {{ tag.name }}
              </UiBadge>
            </div>

            <div
              class="mt-auto flex flex-col gap-4 pt-6 sm:flex-row sm:items-end sm:justify-between"
            >
              <div class="meta-row order-2 sm:order-1">
                <span>{{ post.author.displayName }}</span>
                <span>{{ formatDateTime(post.updatedAt, locale) }}</span>
                <span class="inline-flex items-center gap-1.5">
                  <ChatBubbleLeftEllipsisIcon aria-hidden="true" class="size-4" />
                  {{ post.commentCount }}
                </span>
                <span class="inline-flex items-center gap-1.5">
                  <HeartIcon aria-hidden="true" class="size-4" />
                  {{ post.likeCount }}
                </span>
              </div>
              <span
                :class="buttonVariants({ variant: 'secondary', size: 'sm' })"
                aria-hidden="true"
                class="order-1 self-start sm:order-2 sm:self-auto"
              >
                <BookOpenIcon aria-hidden="true" class="size-4" />
                {{ t('home.readMore') }}
              </span>
            </div>
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
              <img
                v-if="post.coverUrl"
                :alt="post.title"
                class="mb-5 max-h-64 w-full rounded-md border border-[var(--panel-border)] object-cover"
                :src="post.coverUrl"
              />
              <div class="meta-row">
                <span>{{ post.author.displayName }}</span>
                <span>{{ formatDateTime(post.updatedAt, locale) }}</span>
                <span class="inline-flex items-center gap-1.5">
                  <ChatBubbleLeftEllipsisIcon aria-hidden="true" class="size-4" />
                  {{ post.commentCount }}
                </span>
                <span class="inline-flex items-center gap-1.5">
                  <HeartIcon aria-hidden="true" class="size-4" />
                  {{ post.likeCount }}
                </span>
              </div>
              <h2 class="text-title mt-4 text-2xl font-semibold tracking-tight md:text-3xl">
                {{ post.title }}
              </h2>
              <p class="text-body mt-4 max-w-3xl text-sm leading-7 md:text-base">
                {{ post.summary }}
              </p>

              <div v-if="post.category || post.tags?.length" class="mt-4 flex flex-wrap gap-2">
                <UiBadge v-if="post.category" class="gap-1.5 px-3 py-1 text-xs" variant="muted">
                  <FolderIcon aria-hidden="true" class="size-3.5" />
                  {{ post.category.name }}
                </UiBadge>
                <UiBadge
                  v-for="tag in post.tags"
                  :key="tag.id"
                  class="gap-1.5 px-3 py-1 text-xs"
                  variant="outline"
                >
                  <TagIcon aria-hidden="true" class="size-3.5" />
                  {{ tag.name }}
                </UiBadge>
              </div>
            </div>

            <div class="flex items-start md:items-center">
              <span
                :class="buttonVariants({ variant: 'secondary', size: 'sm' })"
                aria-hidden="true"
              >
                <BookOpenIcon aria-hidden="true" class="size-4" />
                {{ t('home.readMore') }}
              </span>
            </div>
          </NuxtLink>
        </div>
      </template>
    </section>

    <SiteFooter />
  </main>
</template>
