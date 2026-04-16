<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { cn } from '~/lib/utils'
import { alertVariants } from './alertVariants'

const props = withDefaults(defineProps<{
  class?: HTMLAttributes['class']
  variant?: 'default' | 'destructive'
}>(), {
  class: '',
  variant: 'default'
})
</script>

<template>
  <div :class="cn(alertVariants({ variant: props.variant }), 'grid gap-1.5', props.class)" role="alert">
    <slot />
  </div>
</template>
