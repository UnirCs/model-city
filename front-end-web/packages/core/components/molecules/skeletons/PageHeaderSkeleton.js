import Skeleton from '@modelcity/core/components/atoms/Skeleton';

/**
 * PageHeaderSkeleton — back-button + title + subtitle row,
 * used at the top of inner pages.
 */
export default function PageHeaderSkeleton() {
  return (
    <div className="space-y-md">
      <Skeleton className="h-8 w-24" />
      <div className="flex flex-wrap items-end justify-between gap-md">
        <div className="space-y-sm flex-1 min-w-[240px]">
          <Skeleton className="h-9 w-2/3" />
          <Skeleton className="h-4 w-1/2" />
        </div>
        <Skeleton className="h-10 w-36" />
      </div>
    </div>
  );
}

