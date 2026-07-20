import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';

const DISABLED_CLASS =
  'flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-outline opacity-50 text-label-md cursor-not-allowed select-none';
const LINK_CLASS =
  'flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-on-surface-variant hover:bg-surface-container transition-colors text-label-md';

/**
 * Molecule: SectionPager
 *
 * Server-rendered previous / page-indicator / next pager for SSR list pages.
 * Navigation is a plain `<Link scroll={false}>` whose target is produced by
 * `hrefBuilder(pageNumber)`, so the same component serves single-section pages
 * and multi-section pages (one pager per section, each with its own builder).
 * Returns `null` when there is a single page.
 *
 * @param {{
 *   currentPage: number,
 *   totalPages: number,
 *   hrefBuilder: (pageNumber: number) => string,
 *   labels: { page: string, of: string, prev: string, next: string },
 *   ariaLabel?: string,
 *   className?: string,        // extra nav classes (e.g. top spacing)
 * }} props
 */
export default function SectionPager({ currentPage, totalPages, hrefBuilder, labels, ariaLabel = 'Pagination', className = 'mt-md' }) {
  if (!totalPages || totalPages <= 1) return null;

  const isFirst = currentPage <= 0;
  const isLast = currentPage >= totalPages - 1;

  return (
    <nav className={`grid grid-cols-3 items-center gap-sm ${className}`.trim()} aria-label={ariaLabel}>
      <div className="flex justify-start">
        {isFirst ? (
          <span className={DISABLED_CLASS}>
            <Icon name="chevron_left" size={18} />
            <span className="hidden sm:inline">{labels.prev}</span>
          </span>
        ) : (
          <Link href={hrefBuilder(currentPage - 1)} scroll={false} className={LINK_CLASS}>
            <Icon name="chevron_left" size={18} />
            <span className="hidden sm:inline">{labels.prev}</span>
          </Link>
        )}
      </div>

      <span className="text-body-md text-on-surface-variant text-center">
        {labels.page} {currentPage + 1} {labels.of} {totalPages}
      </span>

      <div className="flex justify-end">
        {isLast ? (
          <span className={DISABLED_CLASS}>
            <span className="hidden sm:inline">{labels.next}</span>
            <Icon name="chevron_right" size={18} />
          </span>
        ) : (
          <Link href={hrefBuilder(currentPage + 1)} scroll={false} className={LINK_CLASS}>
            <span className="hidden sm:inline">{labels.next}</span>
            <Icon name="chevron_right" size={18} />
          </Link>
        )}
      </div>
    </nav>
  );
}
