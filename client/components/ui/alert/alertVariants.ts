import { cva, type VariantProps } from 'class-variance-authority'

export const alertVariants = cva('relative w-full rounded-[24px] border px-4 py-3 text-sm', {
  variants: {
    variant: {
      default: 'border-[var(--panel-border)] bg-[var(--panel-soft-bg)] text-[var(--body)]',
      destructive: 'border-[var(--danger-border)] bg-[var(--danger-bg)] text-[var(--danger-text)]',
    },
  },
  defaultVariants: {
    variant: 'default',
  },
})

export type AlertVariants = VariantProps<typeof alertVariants>
