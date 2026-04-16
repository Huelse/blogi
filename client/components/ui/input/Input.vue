<script setup lang="ts">
import type { HTMLAttributes, InputTypeHTMLAttribute } from 'vue'
import { cn } from '~/lib/utils'

defineOptions({
  inheritAttrs: false
})

const props = defineProps<{
  class?: HTMLAttributes['class']
  type?: InputTypeHTMLAttribute
}>()

const modelValue = defineModel<string | number>()
const attrs = useAttrs()

const classes = computed(() =>
  cn(
    'flex h-12 w-full rounded-2xl border border-[var(--panel-border)] bg-[var(--field-bg)] px-4 py-3 text-sm text-[var(--title)] transition',
    'placeholder:text-[var(--field-placeholder)] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[color-mix(in_srgb,var(--brand)_16%,transparent)]',
    'focus-visible:border-[color-mix(in_srgb,var(--brand)_55%,transparent)] disabled:cursor-not-allowed disabled:opacity-50',
    attrs.class as HTMLAttributes['class'],
    props.class
  )
)
</script>

<template>
  <input
    v-model="modelValue"
    :class="classes"
    :type="props.type"
    v-bind="$attrs"
  >
</template>
