import { describe, expect, it } from 'vitest'
import { renderMarkdown } from '../../utils/markdown'

describe('renderMarkdown', () => {
  it('renders headings, links, and line breaks', () => {
    const html = renderMarkdown('# Blogi\n\nvisit https://example.com\nnext line')

    expect(html).toContain('<h1>Blogi</h1>')
    expect(html).toContain('<a href="https://example.com">https://example.com</a>')
    expect(html).toContain('<br>')
  })

  it('escapes raw html by default', () => {
    const html = renderMarkdown('<script>alert(1)</script>')

    expect(html).toContain('&lt;script&gt;alert(1)&lt;/script&gt;')
    expect(html).not.toContain('<script>')
  })
})
