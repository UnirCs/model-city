import {
  PageHeaderSkeleton,
  StatusBarSkeleton,
  ListSkeleton,
} from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/security/alerts. */
export default function AlertsLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <StatusBarSkeleton />
      <ListSkeleton count={6} withImage={false} contained />
    </div>
  );
}

