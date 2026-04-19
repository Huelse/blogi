<script setup lang="ts">
import { CheckIcon, QueueListIcon, Squares2X2Icon } from '@heroicons/vue/20/solid'
import type { PostListLayout } from '~/composables/useBlogSettings'

const route = useRoute()
const auth = useAuth()
const blogSettings = useBlogSettings()

if (!auth.isAuthenticated.value) {
  await navigateTo(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
}

const layoutOptions: Array<{
  label: string
  value: PostListLayout
  description: string
}> = [
  {
    label: '列模式',
    value: 'list',
    description: '当前文章列表的展示方式，按时间顺序纵向排列。',
  },
  {
    label: '卡片模式',
    value: 'card',
    description: '以网格卡片展示文章摘要，适合突出多篇内容的浏览入口。',
  },
]
</script>

<template>
  <AdminShell title="博客设置" description="调整博客前台展示相关配置。">
    <section
      class="rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] p-5 backdrop-blur"
    >
      <div class="flex flex-col gap-2 border-b border-[var(--panel-border)] pb-5">
        <h2 class="text-title text-lg font-semibold">博客列表展示方式</h2>
        <p class="text-muted text-sm leading-7">
          选择前台文章列表的展示模式，设置会保存到当前浏览器。
        </p>
      </div>

      <div class="mt-5 grid gap-3 md:grid-cols-2">
        <button
          v-for="option in layoutOptions"
          :key="option.value"
          class="h-[104px] overflow-hidden rounded-[8px] border p-4 text-left transition focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[color-mix(in_srgb,var(--brand)_16%,transparent)]"
          :class="
            blogSettings.postListLayout.value === option.value
              ? 'border-[var(--brand-border)] bg-[var(--brand-soft)]'
              : 'border-[var(--panel-border)] bg-[var(--panel-soft-bg)] hover:bg-[var(--secondary-bg-hover)]'
          "
          type="button"
          @click="blogSettings.setPostListLayout(option.value)"
        >
          <span
            class="grid w-full"
            style="grid-template-columns: 20px minmax(0, 1fr) 20px; column-gap: 12px"
          >
            <span class="flex items-center justify-center" style="width: 20px; height: 20px">
              <QueueListIcon
                v-if="option.value === 'list'"
                aria-hidden="true"
                class="text-[var(--brand)]"
                style="width: 20px; height: 20px"
              />
              <Squares2X2Icon
                v-else
                aria-hidden="true"
                class="text-[var(--brand)]"
                style="width: 20px; height: 20px"
              />
            </span>

            <span
              class="min-w-0"
              style="
                display: flex;
                width: 100%;
                flex-direction: column;
                align-items: flex-start;
                text-align: left;
              "
            >
              <span class="text-title block font-semibold leading-6">{{ option.label }}</span>
              <span class="text-muted mt-2 block text-sm leading-6">
                <span class="block line-clamp-2">{{ option.description }}</span>
              </span>
            </span>

            <span class="flex items-center justify-center" style="width: 20px; height: 20px">
              <CheckIcon
                v-if="blogSettings.postListLayout.value === option.value"
                aria-hidden="true"
                class="text-[var(--brand)]"
                style="width: 20px; height: 20px"
              />
            </span>
          </span>
        </button>
      </div>
    </section>
  </AdminShell>
</template>
