<script setup lang="ts">
import type { PostSummary } from '~/types/blogi'
import { formatDateTime } from '~/utils/date'

const api = useApiClient()
const auth = useAuth()

const { data: posts, pending, error, refresh } = await useAsyncData('posts', () => api<PostSummary[]>('/posts'))
</script>

<template>
  <main class="relative">
    <section class="mx-auto max-w-6xl px-6 py-16">
      <div class="panel overflow-hidden">
        <div class="grid gap-8 px-8 py-10 md:grid-cols-[1.4fr_0.8fr] md:px-10">
          <div>
            <p class="text-sm uppercase tracking-[0.38em] text-amber-300">Blogi Notes</p>
            <h1 class="mt-4 max-w-3xl text-4xl font-semibold leading-tight text-white md:text-6xl">
              已接入注册、登录、JWT 和 Markdown 文章流的个人博客骨架
            </h1>
            <p class="mt-6 max-w-2xl text-lg leading-8 text-stone-300">
              现在可以直接注册账号、创建文章、编辑自己的内容，并在详情页查看 Markdown 渲染结果。
            </p>

            <div class="mt-8 flex flex-wrap gap-3">
              <NuxtLink v-if="auth.isAuthenticated.value" class="btn-primary" to="/posts/new">
                发布新文章
              </NuxtLink>
              <NuxtLink v-else class="btn-primary" to="/register">
                创建第一个账号
              </NuxtLink>
              <NuxtLink class="btn-secondary" to="/login">
                {{ auth.isAuthenticated.value ? '切换账号' : '登录后写作' }}
              </NuxtLink>
            </div>
          </div>

          <div class="grid gap-4">
            <div class="rounded-[24px] border border-white/10 bg-black/25 p-6">
              <p class="text-sm uppercase tracking-[0.24em] text-stone-500">Current State</p>
              <p class="mt-4 text-2xl font-semibold text-white">{{ posts?.length ?? 0 }}</p>
              <p class="mt-2 text-sm text-stone-400">篇文章已进入公开列表</p>
            </div>
            <div class="rounded-[24px] border border-amber-300/20 bg-amber-300/10 p-6">
              <p class="text-sm uppercase tracking-[0.24em] text-amber-200">Auth</p>
              <p class="mt-4 text-lg font-medium text-white">
                {{ auth.user.value ? `当前用户：${auth.user.value.displayName}` : '未登录，文章详情仍可公开浏览' }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <section class="mt-10">
        <div class="mb-6 flex items-center justify-between">
          <div>
            <h2 class="text-2xl font-semibold text-white">最新文章</h2>
            <p class="mt-2 text-sm text-stone-400">公开接口：`GET /api/posts`</p>
          </div>
          <button class="btn-secondary" type="button" @click="refresh()">刷新</button>
        </div>

        <div v-if="pending" class="panel px-6 py-8 text-stone-300">文章列表加载中...</div>
        <div v-else-if="error" class="panel px-6 py-8 text-rose-200">文章列表加载失败，请确认后端已启动。</div>
        <div v-else-if="!posts?.length" class="panel px-6 py-8 text-stone-300">
          还没有文章。{{ auth.isAuthenticated.value ? '从右上角开始发布第一篇。' : '注册后即可发布第一篇。' }}
        </div>
        <div v-else class="grid gap-5 md:grid-cols-2">
          <article
            v-for="post in posts"
            :key="post.id"
            class="panel flex h-full flex-col justify-between p-6"
          >
            <div>
              <div class="flex items-center justify-between gap-4 text-xs uppercase tracking-[0.24em] text-stone-500">
                <span>{{ post.author.displayName }}</span>
                <span>{{ formatDateTime(post.updatedAt) }}</span>
              </div>
              <h3 class="mt-5 text-2xl font-semibold text-white">{{ post.title }}</h3>
              <p class="mt-4 text-sm leading-7 text-stone-300">{{ post.summary }}</p>
            </div>
            <NuxtLink :to="`/posts/${post.id}`" class="mt-8 text-sm font-medium text-amber-300 hover:text-amber-200">
              阅读全文
            </NuxtLink>
          </article>
        </div>
      </section>
    </section>
  </main>
</template>
