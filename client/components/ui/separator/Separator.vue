<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { cn } from '~/lib/utils'

const props = withDefaults(
  defineProps<{
    class?: HTMLAttributes['class']
    orientation?: 'horizontal' | 'vertical'
  }>(),
  {
    class: '',
    orientation: 'horizontal',
  },
)
</script>

<template>
  <div
    :class="cn(
      'shrink-0 bg-[var(--panel-border)]',
      props.orientation === 'horizontal' ? 'h-px w-full' : 'h-full w-px',
      props.class
    )"
    aria-hidden="true"
  />
</template>
