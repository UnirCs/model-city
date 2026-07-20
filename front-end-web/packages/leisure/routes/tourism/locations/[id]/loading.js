import Skeleton from '@modelcity/core/components/atoms/Skeleton';
import { DetailHeaderSkeleton, ListSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/tourism/locations/[id]. */
export default function TourismLocationDetailLoading() {
  return (
    <div className="space-y-lg">
      <DetailHeaderSkeleton />
      <section className="space-y-sm">
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-11/12" />
        <Skeleton className="h-4 w-9/12" />
      </section>
      <ListSkeleton count={3} />
    </div>
  );
}

