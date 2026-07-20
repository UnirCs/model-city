import { PageHeaderSkeleton, ListSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/events/my-tickets. */
export default function MyTicketsLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <ListSkeleton count={4} withImage={false} contained />
    </div>
  );
}

