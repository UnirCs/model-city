import { PageHeaderSkeleton, ListSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/administration/records — event list. */
export default function RecordsLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <ListSkeleton count={6} withImage={false} />
    </div>
  );
}
