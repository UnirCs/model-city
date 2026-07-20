import { PageHeaderSkeleton, FormSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/tourism/locations/[id]/edit. */
export default function TourismLocationEditLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <FormSkeleton fields={6} />
    </div>
  );
}

