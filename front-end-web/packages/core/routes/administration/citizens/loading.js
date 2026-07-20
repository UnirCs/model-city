import { PageHeaderSkeleton, CardSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/administration/citizens — card grid. */
export default function CitizensLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-md">
        {Array.from({ length: 8 }).map((_, i) => (
          <CardSkeleton key={i} withImage={false} lines={3} />
        ))}
      </div>
    </div>
  );
}
