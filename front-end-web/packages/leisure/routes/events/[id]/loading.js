import Skeleton from '@modelcity/core/components/atoms/Skeleton';
import { DetailHeaderSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/events/[id] — event detail. */
export default function EventDetailLoading() {
  return (
    <div className="space-y-lg">
      <DetailHeaderSkeleton />
      <div className="grid grid-cols-1 lg:grid-cols-10 gap-lg">
        <div className="lg:col-span-6 space-y-md">
          <section className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm space-y-sm">
            <Skeleton className="h-6 w-1/3 mb-sm" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-11/12" />
            <Skeleton className="h-4 w-10/12" />
          </section>
          <Skeleton className="h-48 w-full" rounded="md" />
        </div>
        <aside className="lg:col-span-4 space-y-md">
          <Skeleton className="h-40 w-full" rounded="md" />
          <Skeleton className="h-40 w-full" rounded="md" />
          <Skeleton className="h-36 w-full" rounded="md" />
        </aside>
      </div>
    </div>
  );
}

