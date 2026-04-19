export type PostListLayout = 'list' | 'card'

export function useBlogSettings() {
  const postListLayout = useCookie<PostListLayout>('blogi_post_list_layout', {
    default: () => 'list',
    sameSite: 'lax',
  })

  function setPostListLayout(layout: PostListLayout) {
    postListLayout.value = layout
  }

  return {
    postListLayout,
    setPostListLayout,
  }
}
