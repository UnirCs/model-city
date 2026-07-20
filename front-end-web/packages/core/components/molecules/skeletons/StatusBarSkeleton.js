import Skeleton from '@modelcity/core/components/atoms/Skeleton';

/**
 * StatusBarSkeleton — wide horizontal bar placeholder used for filter
 * bars, status banners and similar in-page strips.
 *
 * @param {object} props
 * @param {number} [props.chips=4]  Number of inline chip placeholders.
 */
export default function StatusBarSkeleton({ chips = 4 }) {
  return (
    <div className="flex flex-wrap items-center gap-sm p-md bg-surface-container-low rounded-md border border-outline-variant">
      <Skeleton className="h-9 flex-1 min-w-[200px]" />
      {Array.from({ length: chips }).map((_, i) => (
        <Skeleton key={i} className="h-9 w-24" rounded="full" />
      ))}
    </div>
  );
}

