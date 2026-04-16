<script setup lang="ts">
import type { HTMLAttributes } from 'vue'
import { cn } from '~/lib/utils'

defineOptions({
  inheritAttrs: false
})

const props = defineProps<{
  class?: HTMLAttributes['class']
}>()

const modelValue = defineModel<string>()
const attrs = useAttrs()

const classes = computed(() =>
  cn(
    'flex min-h-24 w-full rounded-2xl border border-[var(--panel-border)] bg-[var(--field-bg)] px-4 py-3 text-sm text-[var(--title)] transition',
    'placeholder:text-[var(--field-placeholder)] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[color-mix(in_srgb,var(--brand)_16%,transparent)]',
    'focus-visible:border-[color-mix(in_srgb,var(--brand)_55%,transparent)] disabled:cursor-not-allowed disabled:opacity-50',
    attrs.class as HTMLAttributes['class'],
    props.class
  )
)
</script>

<template>
  <textarea
    v-model="modelValue"
    :class="classes"
    v-bind="$attrs"
  />
</template>
