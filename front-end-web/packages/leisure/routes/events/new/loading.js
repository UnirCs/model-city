import { PageHeaderSkeleton, FormSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/events/new. */
export default function EventNewLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <FormSkeleton fields={7} />
    </div>
  );
}

