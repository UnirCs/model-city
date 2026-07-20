import { DetailHeaderSkeleton, ListSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/administration/citizens/[id] — detail + events. */
export default function CitizenDetailLoading() {
  return (
    <div className="space-y-lg">
      <DetailHeaderSkeleton />
      <ListSkeleton count={5} withImage={false} />
    </div>
  );
}
