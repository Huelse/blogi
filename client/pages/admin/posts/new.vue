<script setup lang="ts">
import { ExclamationTriangleIcon } from '@heroicons/vue/20/solid'
import type { PostDetail, PostPayload } from '~/types/blogi'
import { getErrorMessage } from '~/utils/errors'

const api = useApiClient()
const { t } = useI18n()
const pending = ref(false)
const errorMessage = ref('')

async function save(payload: PostPayload) {
  pending.value = true
  errorMessage.value = ''

  try {
    const post = await api<PostDetail>('/posts', {
      method: 'POST',
      body: payload,
    })
    await navigateTo(`/posts/${post.id}`)
  } catch (error) {
    errorMessage.value = getErrorMessage(error, t('common.requestFailed'))
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <AdminShell :title="t('admin.newPost.title')" :description="t('admin.newPost.description')">
    <UiAlert v-if="errorMessage" class="mb-6" variant="destructive">
      <UiAlertDescription class="flex items-start gap-2">
        <ExclamationTriangleIcon aria-hidden="true" class="mt-0.5 size-4 shrink-0" />
        <span>{{ errorMessage }}</span>
      </UiAlertDescription>
    </UiAlert>

    <PostEditorForm
      :back-label="t('admin.nav.posts')"
      back-to="/admin/posts"
      :description="t('admin.newPost.formDescription')"
      :submitting="pending"
      :submit-label="t('admin.newPost.submit')"
      :title="t('admin.newPost.formTitle')"
      @save="save"
    />
  </AdminShell>
</template>
