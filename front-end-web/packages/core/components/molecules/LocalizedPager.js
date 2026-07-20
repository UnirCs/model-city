import LocalizedLink from '@modelcity/core/lib/i18n/LocalizedLink';
import Icon from '@modelcity/core/components/atoms/Icon';

const DISABLED_CLASS =
  'flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-outline opacity-50 text-label-md cursor-not-allowed select-none';
const LINK_CLASS =
  'flex items-center gap-xs px-md py-sm rounded-md border border-outline-variant text-on-surface-variant hover:bg-surface-container transition-colors text-label-md';

/**
 * Molecule: LocalizedPager
 *
 * Previous / page-indicator / next pager built on {@link LocalizedLink} (so the
 * `lang` prefix is added automatically and client-side navigation keeps the
 * default scroll behaviour). Targets default to `${basePath}?page=N`, or use a
 * custom `hrefBuilder` for pages with several paginated sections. Returns
 * `null` when there is a single page.
 *
 * @param {{
 *   basePath?: string,         // lang-relative path, e.g. "/mobility/cars"
 *   hrefBuilder?: (pageNumber: number) => string,  // overrides basePath
 *   page: number,
 *   totalPages: number,
 *   labels: { page: string, of: string, prev: string, next: string },
 *   ariaLabel?: string,
 *   className?: string,        // extra nav classes (e.g. top spacing)
 * }} props
 */
export default function LocalizedPager({ basePath, hrefBuilder, page, totalPages, labels, ariaLabel = 'Pagination', className = 'mt-md' }) {
  if (!totalPages || totalPages <= 1) return null;

  const isFirst = page <= 0;
  const isLast = page >= totalPages - 1;
  const buildHref = hrefBuilder ?? ((n) => `${basePath}?page=${n}`);

  return (
    <nav className={`grid grid-cols-3 items-center gap-sm ${className}`.trim()} aria-label={ariaLabel}>
      <div className="flex justify-start">
        {isFirst ? (
          <span className={DISABLED_CLASS}>
            <Icon name="chevron_left" size={18} />
            <span className="hidden sm:inline">{labels.prev}</span>
          </span>
        ) : (
          <LocalizedLink href={buildHref(page - 1)} className={LINK_CLASS}>
            <Icon name="chevron_left" size={18} />
            <span className="hidden sm:inline">{labels.prev}</span>
          </LocalizedLink>
        )}
      </div>

      <span className="text-body-md text-on-surface-variant text-center">
        {labels.page} {page + 1} {labels.of} {totalPages}
      </span>

      <div className="flex justify-end">
        {isLast ? (
          <span className={DISABLED_CLASS}>
            <span className="hidden sm:inline">{labels.next}</span>
            <Icon name="chevron_right" size={18} />
          </span>
        ) : (
          <LocalizedLink href={buildHref(page + 1)} className={LINK_CLASS}>
            <span className="hidden sm:inline">{labels.next}</span>
            <Icon name="chevron_right" size={18} />
          </LocalizedLink>
        )}
      </div>
    </nav>
  );
}
