<script setup lang="ts">
import { ExclamationTriangleIcon } from '@heroicons/vue/20/solid'
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
  () => `admin-edit-post-${postId.value}`,
  () => api<PostDetail>(`/posts/${postId.value}`),
  {
    immediate: auth.isAuthenticated.value,
  },
)

if (post.value && post.value.author.id !== auth.user.value?.id) {
  await navigateTo('/admin/posts')
}

async function save(payload: PostPayload) {
  pending.value = true
  errorMessage.value = ''

  try {
    const updated = await api<PostDetail>(`/posts/${postId.value}`, {
      method: 'PUT',
      body: payload,
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
  <AdminShell
    title="编辑文章"
    description="更新标题、摘要、分类、标签和 Markdown 正文。保存后会刷新前台详情页展示。"
  >
    <UiAlert v-if="error || !post" variant="destructive">
      <UiAlertDescription class="flex items-start gap-2">
        <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
        <span>无法加载待编辑文章。</span>
      </UiAlertDescription>
    </UiAlert>
    <template v-else>
      <UiAlert v-if="errorMessage" class="mb-6" variant="destructive">
        <UiAlertDescription class="flex items-start gap-2">
          <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
          <span>{{ errorMessage }}</span>
        </UiAlertDescription>
      </UiAlert>

      <PostEditorForm
        back-label="返回文章管理"
        back-to="/admin/posts"
        description="只有作者本人可以更新内容。清空分类或标签后，文章会从对应筛选项中移除。"
        :initial-value="post"
        :submitting="pending"
        submit-label="保存修改"
        title="Edit Post"
        @save="save"
      />
    </template>
  </AdminShell>
</template>
