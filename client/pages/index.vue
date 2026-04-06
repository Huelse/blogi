<script setup lang="ts">
const config = useRuntimeConfig()

const features = [
  'JWT 登录与文章 CRUD',
  '评论、分类与文件上传',
  'AI 摘要、主题和插件机制'
]
</script>

<template>
  <main class="min-h-screen bg-stone-950 text-stone-100">
    <section class="mx-auto flex min-h-screen max-w-6xl flex-col justify-center px-6 py-24">
      <p class="mb-4 text-sm uppercase tracking-[0.4em] text-amber-400">Blogi</p>
      <h1 class="max-w-4xl text-5xl font-semibold leading-tight md:text-7xl">
        一个为个性表达而设计的博客底座
      </h1>
      <p class="mt-6 max-w-2xl text-lg leading-8 text-stone-300">
        基于 Nuxt 4 与 Spring Boot 4 的博客工程骨架已经初始化完成，前后端可继续按 README 的 TODO 迭代。
      </p>

      <div class="mt-10 grid gap-4 md:grid-cols-3">
        <article
          v-for="item in features"
          :key="item"
          class="rounded-3xl border border-white/10 bg-white/5 p-6 backdrop-blur"
        >
          <p class="text-base text-stone-100">{{ item }}</p>
        </article>
      </div>

      <div class="mt-10 rounded-3xl border border-amber-400/30 bg-amber-400/10 p-6 text-sm text-amber-100">
        API Base: {{ config.public.apiBase }}
      </div>
    </section>
  </main>
</template>
