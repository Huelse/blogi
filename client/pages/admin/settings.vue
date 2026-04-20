<script setup lang="ts">
import { CheckIcon, QueueListIcon, Squares2X2Icon } from '@heroicons/vue/20/solid'
import type { PostListLayout } from '~/composables/useBlogSettings'
import type { BlogSettings } from '~/types/blogi'
import { getErrorMessage } from '~/utils/errors'

const route = useRoute()
const auth = useAuth()
const api = useApiClient()
const blogSettings = useBlogSettings()
const footerHtml = ref('')
const footerError = ref('')
const footerSaved = ref(false)
const savingFooter = ref(false)

if (!auth.isAuthenticated.value) {
  await navigateTo(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
}

const { data: siteSettings, error: siteSettingsError } = await useAsyncData(
  'blog-settings',
  () => api<BlogSettings>('/settings'),
  {
    default: () => ({ footerHtml: '' }),
  },
)

footerHtml.value = siteSettings.value.footerHtml

const footerPreviewHtml = computed(() => footerHtml.value.trim())

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

async function saveFooterSettings() {
  footerError.value = ''
  footerSaved.value = false
  savingFooter.value = true

  try {
    const updated = await api<BlogSettings>('/settings', {
      method: 'PUT',
      body: {
        footerHtml: footerHtml.value,
      },
    })
    siteSettings.value = updated
    footerHtml.value = updated.footerHtml
    footerSaved.value = true
  } catch (error) {
    footerError.value = getErrorMessage(error)
  } finally {
    savingFooter.value = false
  }
}

function clearFooterSettings() {
  footerHtml.value = ''
  footerSaved.value = false
  footerError.value = ''
}
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

    <section
      class="mt-6 rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] p-5 backdrop-blur"
    >
      <div class="flex flex-col gap-2 border-b border-[var(--panel-border)] pb-5">
        <h2 class="text-title text-lg font-semibold">页脚设置</h2>
        <p class="text-muted text-sm leading-7">
          自定义前台页脚 HTML，保存后会显示在首页和文章详情页底部。
        </p>
      </div>

      <UiAlert v-if="siteSettingsError" class="mt-5 px-5 py-4" variant="destructive">
        <UiAlertDescription>页脚设置加载失败，请确认后端已启动。</UiAlertDescription>
      </UiAlert>

      <div class="mt-5 grid gap-5">
        <div>
          <UiLabel for="footer-html">HTML 模板</UiLabel>
          <UiTextarea
            id="footer-html"
            v-model="footerHtml"
            class="min-h-48 font-mono text-xs leading-6"
            placeholder="<p>© 2026 Blogi. Built with Nuxt and Spring Boot.</p>"
            spellcheck="false"
          />
          <p class="text-muted mt-2 text-xs leading-6">
            该内容会作为受信任 HTML
            渲染，请只保存你自己编写或确认安全的模板；清空并保存后前台不会显示页脚。
          </p>
        </div>

        <div
          class="rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-soft-bg)] p-4"
        >
          <div class="text-muted mb-3 text-xs font-semibold uppercase tracking-[0.24em]">预览</div>
          <!-- eslint-disable-next-line vue/no-v-html -->
          <div v-if="footerPreviewHtml" class="site-footer-html" v-html="footerPreviewHtml" />
          <p v-else class="text-muted text-sm leading-7">页脚为空，前台页面不会显示页脚区域。</p>
        </div>

        <div class="flex flex-wrap items-center gap-3">
          <UiButton :disabled="savingFooter" type="button" @click="saveFooterSettings">
            {{ savingFooter ? '保存中...' : '保存页脚设置' }}
          </UiButton>
          <UiButton
            :disabled="savingFooter"
            type="button"
            variant="secondary"
            @click="clearFooterSettings"
          >
            清空编辑内容
          </UiButton>
          <span v-if="footerSaved" class="text-brand text-sm">已保存</span>
          <span v-if="footerError" class="text-danger text-sm">{{ footerError }}</span>
        </div>
      </div>
    </section>
  </AdminShell>
</template>
