import { PageHeaderSkeleton, ListSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/mobility/cars. */
export default function CarsLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <ListSkeleton count={4} withImage={false} contained />
    </div>
  );
}

