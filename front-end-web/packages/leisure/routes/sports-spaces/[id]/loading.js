import Skeleton from '@modelcity/core/components/atoms/Skeleton';
import { DetailHeaderSkeleton, ListSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/sports-spaces/[id]. */
export default function SportsSpaceDetailLoading() {
  return (
    <div className="space-y-lg">
      <DetailHeaderSkeleton />
      <section className="space-y-sm">
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-10/12" />
      </section>
      <ListSkeleton count={3} />
    </div>
  );
}

