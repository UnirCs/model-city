import { PageHeaderSkeleton, FormSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/tourism/routes/[id]/edit. */
export default function TourismRouteEditLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <FormSkeleton fields={6} />
    </div>
  );
}

