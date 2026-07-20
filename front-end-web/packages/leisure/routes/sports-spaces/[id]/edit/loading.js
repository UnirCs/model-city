import { PageHeaderSkeleton, FormSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/sports-spaces/[id]/edit. */
export default function SportsSpaceEditLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <FormSkeleton fields={6} />
    </div>
  );
}

