<script setup lang="ts">
import {
  ArrowLeftOnRectangleIcon,
  ArrowPathIcon,
  BookOpenIcon,
  ExclamationTriangleIcon,
  PencilSquareIcon,
  UserPlusIcon,
} from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'
import type { PostSummary } from '~/types/blogi'
import { formatDateTime } from '~/utils/date'

const api = useApiClient()
const auth = useAuth()

const {
  data: posts,
  pending,
  error,
  refresh,
} = await useAsyncData('posts', () => api<PostSummary[]>('/posts'))
const sortedPosts = computed(() =>
  [...(posts.value ?? [])].sort(
    (left, right) => new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime(),
  ),
)
const featuredPost = computed(() => sortedPosts.value[0] ?? null)
const remainingPosts = computed(() => sortedPosts.value.slice(1))
</script>

<template>
  <main class="relative">
    <section class="mx-auto max-w-6xl px-6 py-12 md:py-16">
      <div class="grid gap-6 lg:grid-cols-[1.45fr_0.9fr]">
        <UiCard v-if="featuredPost" class="flex h-full flex-col justify-between p-8 md:p-10">
          <div>
            <p class="text-brand text-sm uppercase tracking-[0.38em]">Featured Post</p>
            <h1 class="text-title mt-5 max-w-3xl text-4xl font-semibold leading-tight md:text-6xl">
              {{ featuredPost.title }}
            </h1>
            <div class="meta-row mt-5">
              <span>{{ featuredPost.author.displayName }}</span>
              <span>{{ formatDateTime(featuredPost.updatedAt) }}</span>
            </div>
            <p class="text-body mt-6 max-w-2xl text-base leading-8 md:text-lg">
              {{ featuredPost.summary }}
            </p>
          </div>

          <div class="mt-8 flex flex-wrap gap-3">
            <NuxtLink :class="buttonVariants()" :to="`/posts/${featuredPost.id}`">
              <BookOpenIcon aria-hidden="true" class="size-4" />
              阅读全文
            </NuxtLink>
            <NuxtLink
              v-if="auth.isAuthenticated.value"
              :class="buttonVariants({ variant: 'secondary' })"
              to="/posts/new"
            >
              <PencilSquareIcon aria-hidden="true" class="size-4" />
              发布文章
            </NuxtLink>
            <NuxtLink v-else :class="buttonVariants({ variant: 'secondary' })" to="/register">
              <UserPlusIcon aria-hidden="true" class="size-4" />
              创建账号
            </NuxtLink>
          </div>
        </UiCard>

        <UiCard v-else class="flex h-full flex-col justify-between p-8 md:p-10">
          <div>
            <p class="text-brand text-sm uppercase tracking-[0.38em]">Blogi Home</p>
            <h1 class="text-title mt-5 max-w-3xl text-4xl font-semibold leading-tight md:text-6xl">
              首页默认展示最新文章，回到真正的博客列表首页
            </h1>
            <p class="text-body mt-6 max-w-2xl text-base leading-8 md:text-lg">
              登录入口已经固定在右上角。等第一篇文章发布后，这里会优先展示最近更新的内容。
            </p>
          </div>

          <div class="mt-8 flex flex-wrap gap-3">
            <NuxtLink v-if="auth.isAuthenticated.value" :class="buttonVariants()" to="/posts/new">
              <PencilSquareIcon aria-hidden="true" class="size-4" />
              发布第一篇
            </NuxtLink>
            <NuxtLink v-else :class="buttonVariants()" to="/login">
              <ArrowLeftOnRectangleIcon aria-hidden="true" class="size-4" />
              立即登录
            </NuxtLink>
            <NuxtLink
              v-if="!auth.isAuthenticated.value"
              :class="buttonVariants({ variant: 'secondary' })"
              to="/register"
            >
              <UserPlusIcon aria-hidden="true" class="size-4" />
              创建账号
            </NuxtLink>
          </div>
        </UiCard>

        <div class="grid gap-4">
          <UiCard class="p-6" tone="soft">
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-subtle text-sm uppercase tracking-[0.24em]">Latest Feed</p>
                <p class="text-title mt-3 text-3xl font-semibold">{{ sortedPosts.length }}</p>
                <p class="text-muted mt-2 text-sm">篇文章正在首页按更新时间展示</p>
              </div>
              <UiButton size="sm" variant="secondary" type="button" @click="refresh()">
                <ArrowPathIcon aria-hidden="true" class="size-4" />
                刷新
              </UiButton>
            </div>
          </UiCard>

          <UiCard class="p-6" tone="accent">
            <p class="text-brand text-sm uppercase tracking-[0.24em]">Account</p>
            <p class="text-title mt-4 text-lg font-medium">
              {{ auth.user.value ? `当前用户：${auth.user.value.displayName}` : '未登录时也可以直接浏览公开文章' }}
            </p>
            <p class="text-muted mt-3 text-sm leading-7">
              {{ auth.isAuthenticated.value ? '你可以从右上角直接写文章。' : '登录按钮固定在右上角，登录后即可开始发布。' }}
            </p>
          </UiCard>
        </div>
      </div>

      <section class="mt-10">
        <div class="mb-6">
          <div>
            <p class="text-brand text-sm uppercase tracking-[0.3em]">Articles</p>
            <h2 class="text-title mt-3 text-2xl font-semibold">默认文章列表</h2>
            <p class="text-muted mt-2 text-sm">按最近更新时间排序，优先展示最新内容。</p>
          </div>
        </div>

        <UiCard v-if="pending" class="px-6 py-8">文章列表加载中...</UiCard>
        <UiAlert v-else-if="error" class="px-6 py-5" variant="destructive">
          <UiAlertDescription class="flex items-start gap-2">
            <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
            <span>文章列表加载失败，请确认后端已启动。</span>
          </UiAlertDescription>
        </UiAlert>
        <UiCard v-else-if="!sortedPosts.length" class="px-6 py-8">
          还没有文章。{{ auth.isAuthenticated.value ? '从右上角开始发布第一篇。' : '注册后即可发布第一篇。' }}
        </UiCard>
        <div v-else-if="remainingPosts.length" class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
          <UiCard
            v-for="post in remainingPosts"
            :key="post.id"
            class="flex h-full flex-col justify-between p-6"
          >
            <div>
              <div class="meta-row justify-between gap-4">
                <span>{{ post.author.displayName }}</span>
                <span>{{ formatDateTime(post.updatedAt) }}</span>
              </div>
              <h3 class="text-title mt-5 text-2xl font-semibold">{{ post.title }}</h3>
              <p class="text-body mt-4 text-sm leading-7">{{ post.summary }}</p>
            </div>
            <NuxtLink :to="`/posts/${post.id}`" class="accent-link mt-8 text-sm font-medium">
              <BookOpenIcon aria-hidden="true" class="mr-1 inline size-4 align-text-bottom" />
              阅读全文
            </NuxtLink>
          </UiCard>
        </div>
        <UiCard v-else class="px-6 py-8">目前只有一篇文章，已在上方作为首页头条展示。</UiCard>
      </section>
    </section>
  </main>
</template>
