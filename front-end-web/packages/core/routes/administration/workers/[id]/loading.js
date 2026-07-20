import { DetailHeaderSkeleton, ListSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/administration/workers/[id] — detail + events. */
export default function WorkerDetailLoading() {
  return (
    <div className="space-y-lg">
      <DetailHeaderSkeleton />
      <ListSkeleton count={5} withImage={false} />
    </div>
  );
}
