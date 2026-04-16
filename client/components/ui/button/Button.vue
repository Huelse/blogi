<script setup lang="ts">
import type { ButtonHTMLAttributes, HTMLAttributes } from 'vue'
import { cn } from '~/lib/utils'
import { buttonVariants } from './buttonVariants'

const props = withDefaults(
  defineProps<{
    class?: HTMLAttributes['class']
    variant?: 'default' | 'secondary' | 'destructive' | 'ghost' | 'link'
    size?: 'default' | 'sm' | 'lg' | 'icon'
    type?: ButtonHTMLAttributes['type']
    disabled?: boolean
  }>(),
  {
    class: '',
    variant: 'default',
    size: 'default',
    type: 'button',
    disabled: false,
  },
)
</script>

<template>
  <button
    :class="cn(buttonVariants({ variant: props.variant, size: props.size }), props.class)"
    :disabled="props.disabled"
    :type="props.type"
  >
    <slot />
  </button>
</template>
