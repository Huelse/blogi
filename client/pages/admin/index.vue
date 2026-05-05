<script setup lang="ts">
import { DocumentTextIcon, PencilSquareIcon } from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type { PostSummary } from '~/types/blogi'
import { formatDateTime } from '~/utils/date'

const api = useApiClient()
const { locale, t } = useI18n()

const { data: posts, pending } = await useAsyncData('admin-overview-posts', () =>
  api<PostSummary[]>('/posts/mine'),
)

const sortedPosts = computed(() =>
  [...(posts.value ?? [])].sort(
    (left, right) => new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime(),
  ),
)

const latestPost = computed(() => sortedPosts.value[0])
</script>

<template>
  <AdminShell :title="t('admin.overview.title')" :description="t('admin.overview.description')">
    <section class="grid gap-4 md:grid-cols-2">
      <div
        class="rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] p-5 backdrop-blur"
      >
        <div class="flex items-center justify-between gap-4">
          <div>
            <p class="text-muted text-sm">{{ t('admin.overview.totalPosts') }}</p>
            <p class="text-title mt-3 text-4xl font-semibold">
              {{ pending ? '-' : sortedPosts.length }}
            </p>
          </div>
          <DocumentTextIcon aria-hidden="true" class="size-8 text-[var(--brand)]" />
        </div>
      </div>

      <div
        class="rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] p-5 backdrop-blur"
      >
        <p class="text-muted text-sm">{{ t('admin.overview.latestLabel') }}</p>
        <p class="text-title mt-3 truncate text-xl font-semibold">
          {{ latestPost?.title ?? t('admin.overview.latestEmpty') }}
        </p>
        <p class="text-muted mt-2 text-sm">
          {{ latestPost
              ? formatDateTime(latestPost.updatedAt, locale)
              : t('admin.overview.latestPlaceholder') }}
        </p>
      </div>
    </section>

    <section
      class="mt-5 rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] p-5 backdrop-blur"
    >
      <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h2 class="text-title text-lg font-semibold">{{ t('admin.overview.quickActions') }}</h2>
          <p class="text-muted mt-1 text-sm">{{ t('admin.overview.quickActionsDescription') }}</p>
        </div>
        <div class="flex flex-wrap gap-3">
          <NuxtLink :class="buttonVariants({ variant: 'secondary' })" to="/admin/posts">
            <DocumentTextIcon aria-hidden="true" class="size-4" />
            {{ t('admin.nav.posts') }}
          </NuxtLink>
          <NuxtLink :class="buttonVariants()" to="/admin/posts/new">
            <PencilSquareIcon aria-hidden="true" class="size-4" />
            {{ t('admin.nav.newPost') }}
          </NuxtLink>
        </div>
      </div>
    </section>
  </AdminShell>
</template>
