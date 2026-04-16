<script setup lang="ts">
import { ExclamationTriangleIcon } from '@heroicons/vue/20/solid'
import type { PostDetail, PostPayload } from '~/types/blogi'
import { getErrorMessage } from '~/utils/errors'

const auth = useAuth()

if (!auth.isAuthenticated.value) {
  await navigateTo('/login?redirect=/posts/new')
}

const api = useApiClient()
const pending = ref(false)
const errorMessage = ref('')

async function save(payload: PostPayload) {
  pending.value = true
  errorMessage.value = ''

  try {
    const post = await api<PostDetail>('/posts', {
      method: 'POST',
      body: payload
    })
    await navigateTo(`/posts/${post.id}`)
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <main class="mx-auto max-w-6xl px-6 py-16">
    <UiAlert v-if="errorMessage" class="mb-6" variant="destructive">
      <UiAlertDescription class="flex items-start gap-2">
        <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
        <span>{{ errorMessage }}</span>
      </UiAlertDescription>
    </UiAlert>

    <PostEditorForm
      description="文章保存后会立即进入公开列表，只有作者本人能继续编辑或删除。"
      :submitting="pending"
      submit-label="发布文章"
      title="New Post"
      @save="save"
    />
  </main>
</template>
