import { PageHeaderSkeleton, ListSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/events/[id]/tickets. */
export default function EventTicketsLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <ListSkeleton count={4} withImage={false} />
    </div>
  );
}

