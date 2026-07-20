import {
  PageHeaderSkeleton,
  StatusBarSkeleton,
  ListSkeleton,
} from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/mobility/my-stays. */
export default function MyStaysLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <StatusBarSkeleton />
      <ListSkeleton count={6} withImage={false} contained />
    </div>
  );
}

