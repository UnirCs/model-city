import {
  PageHeaderSkeleton,
  StatusBarSkeleton,
  ListSkeleton,
} from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/mobility/my-sanctions. */
export default function MySanctionsLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <StatusBarSkeleton />
      <ListSkeleton count={6} withImage={false} contained />
    </div>
  );
}

