export interface ApiEnvelope<T> {
  success: boolean
  data: T
  message: string
}

export interface UserProfile {
  id: number
  username: string
  displayName: string
}

export interface AuthSession {
  token: string
  user: UserProfile
}

export interface PostAuthor {
  id: number
  username: string
  displayName: string
}

export interface PostCategory {
  id: number
  name: string
  slug: string
}

export interface PostTag {
  id: number
  name: string
  slug: string
}

export interface PostSummary {
  id: number
  title: string
  summary: string
  createdAt: string
  updatedAt: string
  author: PostAuthor
  category: PostCategory | null
  tags: PostTag[]
  commentCount: number
}

export interface PostDetail extends PostSummary {
  contentMarkdown: string
}

export interface PostPayload {
  title: string
  summary: string
  contentMarkdown: string
  category: string
  tags: string[]
}

export interface BlogSettings {
  footerHtml: string
}

export interface PostComment {
  id: number
  postId: number
  content: string
  createdAt: string
  updatedAt: string
  author: PostAuthor
}

export interface CommentPayload {
  content: string
}
