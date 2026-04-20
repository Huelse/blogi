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

export interface PostSummary {
  id: number
  title: string
  summary: string
  createdAt: string
  updatedAt: string
  author: PostAuthor
}

export interface PostDetail extends PostSummary {
  contentMarkdown: string
}

export interface PostPayload {
  title: string
  summary: string
  contentMarkdown: string
}

export interface BlogSettings {
  footerHtml: string
}
