declare module 'markdown-it' {
  interface MarkdownItOptions {
    html?: boolean
    linkify?: boolean
    breaks?: boolean
  }

  interface MarkdownItInstance {
    render(source: string): string
  }

  interface MarkdownItConstructor {
    new (options?: MarkdownItOptions): MarkdownItInstance
  }

  const MarkdownIt: MarkdownItConstructor

  export default MarkdownIt
}
