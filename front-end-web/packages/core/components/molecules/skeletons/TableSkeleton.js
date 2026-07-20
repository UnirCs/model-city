import Skeleton from '@modelcity/core/components/atoms/Skeleton';

/**
 * TableSkeleton — placeholder for tabular admin views.
 * Renders a toolbar, a header row and N data rows of M columns.
 *
 * @param {object} props
 * @param {number} [props.rows=8]
 * @param {number} [props.columns=5]
 */
export default function TableSkeleton({ rows = 8, columns = 5 }) {
  return (
    <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm overflow-hidden">
      <div className="flex items-center justify-between p-md border-b border-outline-variant gap-md">
        <Skeleton className="h-9 w-64" />
        <div className="flex gap-sm">
          <Skeleton className="h-9 w-24" />
          <Skeleton className="h-9 w-32" />
        </div>
      </div>

      <div className="p-md space-y-sm">
        <div
          className="grid gap-md pb-sm border-b border-outline-variant"
          style={{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}
        >
          {Array.from({ length: columns }).map((_, c) => (
            <Skeleton key={c} className="h-4 w-3/4" />
          ))}
        </div>

        {Array.from({ length: rows }).map((_, r) => (
          <div
            key={r}
            className="grid gap-md py-sm border-b border-outline-variant/60 last:border-b-0"
            style={{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}
          >
            {Array.from({ length: columns }).map((_, c) => (
              <Skeleton key={c} className="h-4 w-full" />
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

