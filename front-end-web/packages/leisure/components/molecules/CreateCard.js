import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: CreateCard
 *
 * Dashed "add" card used at the start of catalogue grids (and in empty states)
 * to let staff create a new entity.
 *
 * @param {{ href: string, label: string, hint?: string, className?: string }} props
 */
export default function CreateCard({ href, label, hint, className = '' }) {
  return (
    <Link
      href={href}
      className={`group rounded-md border-2 border-dashed border-secondary/40 bg-surface-container-lowest hover:border-secondary hover:bg-secondary/5 transition-all flex flex-col items-center justify-center gap-sm py-lg px-md text-center min-h-40 ${className}`.trim()}
    >
      <span className="w-12 h-12 rounded-full bg-secondary/10 group-hover:bg-secondary/20 flex items-center justify-center transition-colors">
        <Icon name="add" size={24} className="text-secondary" />
      </span>
      <span className="text-label-md text-secondary font-semibold">{label}</span>
      {hint && <span className="text-caption text-on-surface-variant">{hint}</span>}
    </Link>
  );
}
