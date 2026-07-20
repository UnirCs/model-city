import Skeleton from '@modelcity/core/components/atoms/Skeleton';

/**
 * DetailHeaderSkeleton — banner placeholder used by detail pages.
 * Includes a tall image area, title, subtitle, meta row and a couple of
 * action buttons.
 */
export default function DetailHeaderSkeleton() {
  return (
    <section className="space-y-md">
      <Skeleton className="w-full h-64 md:h-80" rounded="md" />
      <div className="space-y-sm">
        <Skeleton className="h-8 w-2/3" />
        <Skeleton className="h-4 w-1/2" />
      </div>
      <div className="flex flex-wrap items-center gap-sm">
        <Skeleton className="h-6 w-24" rounded="full" />
        <Skeleton className="h-6 w-28" rounded="full" />
        <Skeleton className="h-6 w-20" rounded="full" />
      </div>
      <div className="flex flex-wrap justify-end gap-sm">
        <Skeleton className="h-10 w-28" />
        <Skeleton className="h-10 w-32" />
      </div>
    </section>
  );
}

