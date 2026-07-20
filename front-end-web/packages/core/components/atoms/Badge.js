/**
 * Atom: Badge / Status chip
 * Status label with semantic design system colors.
 */

const statusMap = {
  active:   { label: 'Activo',    cls: 'bg-secondary-container text-on-secondary-container' },
  pending:  { label: 'Pendiente', cls: 'bg-tertiary-container text-on-tertiary-container' },
  inactive: { label: 'Inactivo',  cls: 'bg-surface-container-high text-on-surface-variant' },
  error:    { label: 'Error',     cls: 'bg-error-container text-on-error-container' },
};

/**
 * @param {{ status?: string, label?: string, className?: string }} props
 */
export default function Badge({ status = 'active', label, className = '' }) {
  const config = statusMap[status] ?? statusMap.inactive;
  return (
    <span
      className={[
        'inline-flex items-center px-sm py-xs text-caption font-semibold rounded-full tracking-wide',
        config.cls,
        className,
      ].join(' ')}
    >
      {label ?? config.label}
    </span>
  );
}

