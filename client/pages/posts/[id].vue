<script setup lang="ts">
import { ArrowLeftIcon, ExclamationTriangleIcon, PencilSquareIcon } from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type { PostDetail } from '~/types/blogi'
import { formatDateTime } from '~/utils/date'
import { renderMarkdown } from '~/utils/markdown'

const route = useRoute()
const api = useApiClient()
const auth = useAuth()

const postId = computed(() => String(route.params.id))

const { data: post, error } = await useAsyncData(
  () => `post-${postId.value}`,
  () => api<PostDetail>(`/posts/${postId.value}`),
)

const html = computed(() => renderMarkdown(post.value?.contentMarkdown ?? ''))
const isOwner = computed(() => post.value?.author.id === auth.user.value?.id)
</script>

<template>
  <main class="mx-auto max-w-5xl px-6 py-16">
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
        </div>
        <h1 class="text-title mt-5 text-4xl font-semibold tracking-tight md:text-5xl">
          {{ post.title }}
        </h1>
        <p class="text-body mt-5 max-w-3xl text-lg leading-8">{{ post.summary }}</p>

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
    </UiCard>

    <SiteFooter class="mt-10" />
  </main>
</template>
