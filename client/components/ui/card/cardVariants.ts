import { cva, type VariantProps } from 'class-variance-authority'

export const cardVariants = cva(
  'border transition-[background-color,color,border-color,box-shadow]',
  {
    variants: {
      tone: {
        surface:
          'rounded-[28px] border-[var(--panel-border)] bg-[var(--panel-bg)] shadow-[0_32px_80px_-40px_var(--panel-shadow)] backdrop-blur',
        soft: 'rounded-[24px] border-[var(--panel-border)] bg-[var(--panel-soft-bg)]',
        accent: 'rounded-[24px] border-[var(--brand-border)] bg-[var(--brand-soft)]',
      },
    },
    defaultVariants: {
      tone: 'surface',
    },
  },
)

export type CardVariants = VariantProps<typeof cardVariants>
