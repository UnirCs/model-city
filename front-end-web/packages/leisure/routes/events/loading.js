import {
  PageHeaderSkeleton,
  StatusBarSkeleton,
  ListSkeleton,
} from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/events. */
export default function EventsLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <StatusBarSkeleton />
      <ListSkeleton count={6} contained />
    </div>
  );
}

