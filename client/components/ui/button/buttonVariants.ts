import { cva, type VariantProps } from 'class-variance-authority'

export const buttonVariants = cva(
  [
    'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-full text-sm font-medium transition',
    'focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-[color-mix(in_srgb,var(--brand)_16%,transparent)]',
    'disabled:pointer-events-none disabled:opacity-50',
  ],
  {
    variants: {
      variant: {
        default:
          'bg-[var(--primary-bg)] text-[var(--primary-text)] hover:bg-[var(--primary-bg-hover)]',
        secondary: [
          'border border-[var(--secondary-border)] text-[var(--title)]',
          'hover:border-[var(--secondary-border-hover)] hover:bg-[var(--secondary-bg-hover)]',
        ],
        destructive: [
          'border border-[var(--danger-border)] text-[var(--danger-text)]',
          'hover:border-[var(--danger-border)] hover:bg-[var(--danger-bg)]',
        ],
        ghost: 'text-[var(--muted)] hover:bg-[var(--secondary-bg-hover)] hover:text-[var(--title)]',
        link: 'h-auto rounded-none px-0 py-0 text-[var(--accent-link)] underline-offset-4 hover:underline',
      },
      size: {
        default: 'h-11 px-5 py-3',
        sm: 'h-10 px-4 py-2',
        lg: 'h-12 px-6 py-3.5',
        icon: 'h-11 w-11 p-0',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  },
)

export type ButtonVariants = VariantProps<typeof buttonVariants>
