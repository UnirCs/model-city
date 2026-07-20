'use client';

import { useRouter, useSearchParams, usePathname } from 'next/navigation';
import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: PaginationBar
 *
 * Generic prev / next pager that mutates the `page` query parameter in the
 * current URL without touching the rest of the params. Hidden when there is
 * a single page of results.
 *
 * @param {{
 *   page: number,
 *   totalPages: number,
 *   labels: { page: string, of: string, prev: string, next: string },
 * }} props
 */
export default function PaginationBar({ page, totalPages, labels }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  if (!totalPages || totalPages <= 1) return null;

  function goTo(next) {
    const params = new URLSearchParams(searchParams.toString());
    if (next <= 0) params.delete('page');
    else params.set('page', String(next));
    const search = params.toString();
    router.push(`${pathname}${search ? `?${search}` : ''}`);
  }

  const isFirst = page <= 0;
  const isLast  = page >= totalPages - 1;

  return (
    <nav className="grid grid-cols-3 items-center gap-sm py-sm" aria-label="Pagination">
      {/* Left: previous */}
      <div className="flex justify-start">
        <button
          type="button"
          onClick={() => goTo(page - 1)}
          disabled={isFirst}
          className="inline-flex items-center gap-xs px-md py-xs rounded-md border border-outline-variant text-on-surface-variant hover:bg-surface-variant disabled:opacity-40 disabled:cursor-not-allowed text-label-md"
        >
          <Icon name="chevron_left" size={18} />
          <span className="hidden sm:inline">{labels.prev}</span>
        </button>
      </div>
      {/* Centre: page info */}
      <span className="text-caption text-on-surface-variant tabular-nums text-center">
        {labels.page} {page + 1} {labels.of} {totalPages}
      </span>
      {/* Right: next */}
      <div className="flex justify-end">
        <button
          type="button"
          onClick={() => goTo(page + 1)}
          disabled={isLast}
          className="inline-flex items-center gap-xs px-md py-xs rounded-md border border-outline-variant text-on-surface-variant hover:bg-surface-variant disabled:opacity-40 disabled:cursor-not-allowed text-label-md"
        >
          <span className="hidden sm:inline">{labels.next}</span>
          <Icon name="chevron_right" size={18} />
        </button>
      </div>
    </nav>
  );
}

