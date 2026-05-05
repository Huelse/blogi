<script setup lang="ts">
import { ExclamationTriangleIcon } from '@heroicons/vue/20/solid'
import type { PostDetail, PostPayload } from '~/types/blogi'
import { getErrorMessage } from '~/utils/errors'

const auth = useAuth()
const route = useRoute()
const api = useApiClient()
const { t } = useI18n()
const postId = computed(() => String(route.params.id))
const pending = ref(false)
const errorMessage = ref('')

const { data: post, error } = await useAsyncData(
  () => `admin-edit-post-${postId.value}`,
  () => api<PostDetail>(`/posts/${postId.value}`),
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
    errorMessage.value = getErrorMessage(saveError, t('common.requestFailed'))
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <AdminShell :title="t('admin.editPost.title')" :description="t('admin.editPost.description')">
    <UiAlert v-if="error || !post" variant="destructive">
      <UiAlertDescription class="flex items-start gap-2">
        <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
        <span>{{ t('admin.editPost.loadError') }}</span>
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
        :back-label="t('admin.nav.posts')"
        back-to="/admin/posts"
        :description="t('admin.editPost.formDescription')"
        :initial-value="post"
        :submitting="pending"
        :submit-label="t('admin.editPost.submit')"
        :title="t('admin.editPost.formTitle')"
        @save="save"
      />
    </template>
  </AdminShell>
</template>
