import {
  PageHeaderSkeleton,
  FormSkeleton,
  MapSkeleton,
} from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/security/alerts/new. */
export default function AlertsNewLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <FormSkeleton fields={5} />
      <MapSkeleton />
    </div>
  );
}

