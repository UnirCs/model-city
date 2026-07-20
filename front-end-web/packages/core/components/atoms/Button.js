/**
 * Atom: Button
 * Reusable button with design system variants and sizes.
 */

const variants = {
  primary:
    'bg-primary text-on-primary shadow-sm hover:shadow-md hover:opacity-90',
  secondary:
    'bg-secondary text-on-secondary shadow-sm hover:shadow-md hover:opacity-90',
  outline:
    'border border-primary text-primary bg-transparent hover:bg-primary/10',
  ghost:
    'text-primary bg-transparent hover:bg-surface-container',
  error:
    'bg-error text-on-error shadow-sm hover:shadow-md hover:opacity-90',
  'outline-error':
    'border border-error text-error bg-transparent hover:bg-error-container/30',
};

const sizes = {
  sm: 'px-sm py-xs text-label-md rounded-md',
  md: 'px-md py-sm text-label-md rounded-md',
  lg: 'px-lg py-md text-body-md rounded-md',
};

/**
 * @param {{ variant?: keyof variants, size?: keyof sizes, className?: string, children: React.ReactNode } & React.ButtonHTMLAttributes<HTMLButtonElement>} props
 */
export default function Button({
  variant = 'primary',
  size = 'md',
  className = '',
  children,
  ...props
}) {
  return (
    <button
      className={[
        'inline-flex items-center justify-center gap-xs font-semibold transition-all duration-200 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer',
        variants[variant] ?? variants.primary,
        sizes[size] ?? sizes.md,
        className,
      ].join(' ')}
      {...props}
    >
      {children}
    </button>
  );
}
