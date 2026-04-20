<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { cn } from '~/lib/utils'
import type { BlogSettings } from '~/types/blogi'

const props = defineProps<{
  class?: HTMLAttributes['class']
}>()

const api = useApiClient()

const { data: settings } = await useAsyncData(
  'blog-settings',
  () => api<BlogSettings>('/settings'),
  {
    default: () => ({ footerHtml: '' }),
  },
)

const footerHtml = computed(() => settings.value.footerHtml.trim())
</script>

<template>
  <footer
    v-if="footerHtml"
    :class="
      cn(
        'site-footer rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] px-6 py-5 backdrop-blur',
        props.class,
      )
    "
  >
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div class="site-footer-html" v-html="footerHtml" />
  </footer>
</template>
