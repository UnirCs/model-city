import { DetailHeaderSkeleton, FormSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/profile — profile card + form. */
export default function ProfileLoading() {
  return (
    <div className="space-y-lg">
      <DetailHeaderSkeleton />
      <FormSkeleton fields={6} />
    </div>
  );
}

