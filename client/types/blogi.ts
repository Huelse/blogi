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
  likeCount: number
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
  fingerprintHash: string
  content: string
}

export interface VisitorProfile {
  id: number
  fingerprintHash: string
  displayName: string
  email: string
  createdAt: string
  updatedAt: string
}

export interface VisitorProfilePayload {
  displayName: string
  email: string
}

export interface VisitorProfileRequest extends VisitorProfilePayload {
  fingerprintHash: string
}

export interface VisitorResolvePayload {
  fingerprintHash: string
}

export interface PostLikePayload {
  fingerprintHash: string
}

export interface PostLikeState {
  likeCount: number
  liked: boolean
}
