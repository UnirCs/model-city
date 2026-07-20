import Skeleton from '@modelcity/core/components/atoms/Skeleton';
import { ListSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/home — welcome + services grid. */
export default function HomeLoading() {
  return (
    <>
      <section className="mb-xl space-y-sm">
        <Skeleton className="h-10 w-2/3" />
        <Skeleton className="h-5 w-1/2" />
      </section>
      <section className="mb-xl">
        <div className="flex items-center justify-between mb-md">
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-4 w-24" />
        </div>
        <ListSkeleton count={6} />
      </section>
    </>
  );
}

