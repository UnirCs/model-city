import {
  PageHeaderSkeleton,
  ListSkeleton,
} from '@modelcity/core/components/molecules/skeletons';

/**
 * Generic fallback for the authenticated (app) group. Used when no more
 * specific loading.js exists below this segment.
 */
export default function AppGroupLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <ListSkeleton count={6} />
    </div>
  );
}
