import {
  PageHeaderSkeleton,
  FormSkeleton,
  CardSkeleton,
} from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/events/[id]/checkout. */
export default function EventCheckoutLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-lg">
        <div className="lg:col-span-2">
          <FormSkeleton fields={5} />
        </div>
        <CardSkeleton withImage={false} lines={4} />
      </div>
    </div>
  );
}

