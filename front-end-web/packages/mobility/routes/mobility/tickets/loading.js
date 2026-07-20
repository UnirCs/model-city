import { PageHeaderSkeleton, TableSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/mobility/tickets — admin table. */
export default function MobilityTicketsLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <TableSkeleton rows={8} columns={5} />
    </div>
  );
}

