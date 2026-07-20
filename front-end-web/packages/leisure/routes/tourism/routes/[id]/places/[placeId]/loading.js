import {
  PageHeaderSkeleton,
  FormSkeleton,
  MapSkeleton,
  DetailHeaderSkeleton,
} from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/tourism/routes/[id]/places/[placeId]. */
export default function TourismRoutePlaceLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <DetailHeaderSkeleton />
      <MapSkeleton />
      <FormSkeleton fields={3} />
    </div>
  );
}

