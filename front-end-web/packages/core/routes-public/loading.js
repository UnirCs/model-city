import Skeleton from '@modelcity/core/components/atoms/Skeleton';

/**
 * Minimal full-locale loading fallback. Used while the [lang] layout
 * and its children resolve. Mirrors a top bar + a content block.
 */
export default function LangLoading() {
  return (
    <div className="min-h-screen flex flex-col">
      <Skeleton className="h-14 w-full" rounded="md" />
      <div className="flex-1 max-w-container-max mx-auto w-full p-md space-y-md">
        <Skeleton className="h-9 w-1/3" />
        <Skeleton className="h-4 w-1/2" />
        <Skeleton className="h-64 w-full" rounded="md" />
      </div>
    </div>
  );
}

