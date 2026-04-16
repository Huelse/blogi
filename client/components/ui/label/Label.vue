<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { cn } from '~/lib/utils'

defineProps<{
  class?: HTMLAttributes['class']
}>()
</script>

<template>
  <label :class="cn('mb-2 block text-sm font-medium text-[var(--body)]', $props.class)">
    <slot />
  </label>
</template>
