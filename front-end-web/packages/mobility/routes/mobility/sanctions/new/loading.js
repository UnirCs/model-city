import {
  PageHeaderSkeleton,
  FormSkeleton,
  MapSkeleton,
} from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/mobility/sanctions/new. */
export default function SanctionsNewLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <FormSkeleton fields={5} />
      <MapSkeleton />
    </div>
  );
}

