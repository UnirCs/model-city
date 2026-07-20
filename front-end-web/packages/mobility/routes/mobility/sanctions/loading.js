import { PageHeaderSkeleton, TableSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/mobility/sanctions — admin table. */
export default function SanctionsLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <TableSkeleton rows={8} columns={5} />
    </div>
  );
}

