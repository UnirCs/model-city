import Skeleton from '@modelcity/core/components/atoms/Skeleton';

/**
 * MapSkeleton — large rectangular placeholder for embedded maps.
 *
 * @param {object} props
 * @param {boolean} [props.withLegend=false]  Render a side legend column.
 */
export default function MapSkeleton({ withLegend = false }) {
  if (!withLegend) {
    return <Skeleton className="w-full h-[280px] md:h-[420px]" rounded="md" />;
  }
  return (
    <div className="grid grid-cols-1 lg:grid-cols-[1fr_280px] gap-md">
      <Skeleton className="w-full h-[280px] md:h-[420px]" rounded="md" />
      <div className="space-y-sm">
        {Array.from({ length: 5 }).map((_, i) => (
          <div
            key={i}
            className="flex items-center gap-sm p-sm rounded-md border border-outline-variant"
          >
            <Skeleton className="h-8 w-8" variant="circle" />
            <div className="flex-1 space-y-xs">
              <Skeleton className="h-3 w-3/4" />
              <Skeleton className="h-3 w-1/2" />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

