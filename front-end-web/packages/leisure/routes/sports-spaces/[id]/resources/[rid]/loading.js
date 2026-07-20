import {
  PageHeaderSkeleton,
  DetailHeaderSkeleton,
  MapSkeleton,
  FormSkeleton,
} from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/sports-spaces/[id]/resources/[rid]. */
export default function SportsResourceLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <DetailHeaderSkeleton />
      <MapSkeleton />
      <FormSkeleton fields={3} />
    </div>
  );
}

