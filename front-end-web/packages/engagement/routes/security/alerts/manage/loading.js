import {
  PageHeaderSkeleton,
  MapSkeleton,
  TableSkeleton,
} from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/security/alerts/manage. */
export default function AlertsManageLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <MapSkeleton withLegend />
      <TableSkeleton rows={6} columns={4} />
    </div>
  );
}

