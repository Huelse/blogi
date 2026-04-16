<script setup lang="ts">
import type { PostDetail, PostPayload } from '~/types/blogi'
import { getErrorMessage } from '~/utils/errors'

const auth = useAuth()
const route = useRoute()

if (!auth.isAuthenticated.value) {
  await navigateTo(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
}

const api = useApiClient()
const postId = computed(() => String(route.params.id))
const pending = ref(false)
const errorMessage = ref('')

const { data: post, error } = await useAsyncData(
  () => `edit-post-${postId.value}`,
  () => api<PostDetail>(`/posts/${postId.value}`)
)

if (post.value && post.value.author.id !== auth.user.value?.id) {
  await navigateTo(`/posts/${postId.value}`)
}

async function save(payload: PostPayload) {
  pending.value = true
  errorMessage.value = ''

  try {
    const updated = await api<PostDetail>(`/posts/${postId.value}`, {
      method: 'PUT',
      body: payload
    })
    await navigateTo(`/posts/${updated.id}`)
  } catch (saveError) {
    errorMessage.value = getErrorMessage(saveError)
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <main class="mx-auto max-w-6xl px-6 py-16">
    <UiAlert v-if="error || !post" variant="destructive">
      <UiAlertDescription>无法加载待编辑文章。</UiAlertDescription>
    </UiAlert>
    <template v-else>
      <UiAlert v-if="errorMessage" class="mb-6" variant="destructive">
        <UiAlertDescription>{{ errorMessage }}</UiAlertDescription>
      </UiAlert>

      <PostEditorForm
        description="只有作者本人可以更新内容。保存后会覆盖原文，并刷新详情页展示。"
        :initial-value="post"
        :submitting="pending"
        submit-label="保存修改"
        title="Edit Post"
        @save="save"
      />
    </template>
  </main>
</template>
