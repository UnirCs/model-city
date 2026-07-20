import { FormSkeleton } from '@modelcity/core/components/molecules/skeletons';

/** Loading skeleton for /{lang}/register — onboarding form. */
export default function RegisterLoading() {
  return (
    <div className="min-h-screen flex items-center justify-center p-md">
      <div className="w-full max-w-2xl">
        <FormSkeleton fields={8} />
      </div>
    </div>
  );
}
