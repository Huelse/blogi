<script setup lang="ts">
import {
  ArrowLeftOnRectangleIcon,
  Cog6ToothIcon,
  DocumentTextIcon,
  HomeIcon,
  PencilSquareIcon,
} from '@heroicons/vue/20/solid'
import { buttonVariants } from '~/components/ui/button/buttonVariants'

defineProps<{
  title: string
  description?: string
}>()

const route = useRoute()
const auth = useAuth()
const { t } = useI18n()

const adminNav = computed(() => [
  {
    label: t('admin.nav.overview'),
    to: '/admin',
    icon: HomeIcon,
    exact: true,
  },
  {
    label: t('admin.nav.posts'),
    to: '/admin/posts',
    icon: DocumentTextIcon,
    exact: true,
  },
  {
    label: t('admin.nav.newPost'),
    to: '/admin/posts/new',
    icon: PencilSquareIcon,
  },
  {
    label: t('admin.nav.settings'),
    to: '/admin/settings',
    icon: Cog6ToothIcon,
  },
])

function isActive(item: { to: string; exact?: boolean }) {
  return item.exact ? route.path === item.to : route.path.startsWith(item.to)
}

async function logout() {
  auth.clearSession()
  await navigateTo('/')
}
</script>

<template>
  <main class="mx-auto grid max-w-7xl gap-6 px-6 py-8 lg:grid-cols-[260px_minmax(0,1fr)]">
    <aside
      class="rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] p-4 backdrop-blur"
    >
      <div class="border-b border-[var(--panel-border)] pb-4">
        <p class="text-brand text-xs font-semibold uppercase tracking-[0.3em]">
          {{ t('app.admin') }}
        </p>
        <p v-if="auth.user.value" class="text-title mt-3 text-lg font-semibold">
          {{ auth.user.value.displayName }}
        </p>
        <p class="text-muted mt-1 text-sm">{{ t('admin.shell.subtitle') }}</p>
      </div>

      <nav class="mt-4 space-y-1">
        <NuxtLink
          v-for="item in adminNav"
          :key="item.to"
          :to="item.to"
          class="flex h-11 items-center gap-3 rounded-[6px] px-3 text-sm font-medium transition"
          :class="
            isActive(item)
              ? 'bg-[var(--brand-soft)] text-[var(--title)]'
              : 'text-[var(--muted)] hover:bg-[var(--secondary-bg-hover)] hover:text-[var(--title)]'
          "
        >
          <component :is="item.icon" aria-hidden="true" class="size-4" />
          {{ item.label }}
        </NuxtLink>
      </nav>

      <div class="mt-6 border-t border-[var(--panel-border)] pt-4">
        <UiButton class="w-full justify-start" type="button" variant="ghost" @click="logout">
          <ArrowLeftOnRectangleIcon aria-hidden="true" class="size-4" />
          {{ t('app.logout') }}
        </UiButton>
      </div>
    </aside>

    <section class="min-w-0">
      <div
        class="mb-6 rounded-[8px] border border-[var(--panel-border)] bg-[var(--panel-bg)] px-6 py-5 backdrop-blur"
      >
        <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <div
              class="flex items-center gap-2 text-brand text-xs font-semibold uppercase tracking-[0.24em]"
            >
              <Cog6ToothIcon aria-hidden="true" class="size-4" />
              {{ t('admin.shell.eyebrow') }}
            </div>
            <h1 class="text-title mt-3 text-3xl font-semibold tracking-tight">{{ title }}</h1>
            <p v-if="description" class="text-muted mt-2 max-w-3xl text-sm leading-7">
              {{ description }}
            </p>
          </div>

          <NuxtLink :class="buttonVariants({ size: 'sm' })" to="/admin/posts/new">
            <PencilSquareIcon aria-hidden="true" class="size-4" />
            {{ t('admin.nav.newPost') }}
          </NuxtLink>
        </div>
      </div>

      <slot />
    </section>
  </main>
</template>
