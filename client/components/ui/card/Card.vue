<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { cn } from '~/lib/utils'
import { cardVariants } from './cardVariants'

const props = withDefaults(defineProps<{
  class?: HTMLAttributes['class']
  tone?: 'surface' | 'soft' | 'accent'
}>(), {
  class: '',
  tone: 'surface'
})
</script>

<template>
  <section :class="cn(cardVariants({ tone: props.tone }), props.class)">
    <slot />
  </section>
</template>
