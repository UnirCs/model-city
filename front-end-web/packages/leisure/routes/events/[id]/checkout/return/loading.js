import { PageHeaderSkeleton, CardSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/events/[id]/checkout/return. */
export default function EventCheckoutReturnLoading() {
  return (
    <div className="space-y-lg">
      <PageHeaderSkeleton />
      <div className="max-w-2xl mx-auto w-full">
        <CardSkeleton withImage={false} lines={5} />
      </div>
    </div>
  );
}

