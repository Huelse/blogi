import { cva, type VariantProps } from 'class-variance-authority'

export const badgeVariants = cva(
  'inline-flex items-center rounded-full border px-4 py-2 text-sm font-medium transition',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-[var(--primary-bg)] text-[var(--primary-text)]',
        muted: 'border-[var(--secondary-border)] bg-[color-mix(in_srgb,var(--panel-bg)_70%,transparent)] text-[var(--muted)]',
        outline: 'border-[var(--secondary-border)] text-[var(--title)]'
      }
    },
    defaultVariants: {
      variant: 'default'
    }
  }
)

export type BadgeVariants = VariantProps<typeof badgeVariants>
