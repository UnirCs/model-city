import {
  PageHeaderSkeleton,
  MapSkeleton,
  FormSkeleton,
} from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/mobility/reserve — map + reservation form. */
export default function ReserveLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <MapSkeleton withLegend />
      <FormSkeleton fields={3} />
    </div>
  );
}

