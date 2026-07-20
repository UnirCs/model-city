import Skeleton from '@modelcity/core/components/atoms/Skeleton';

/**
 * FormSkeleton — title + N field placeholders + submit/cancel row.
 *
 * @param {object} props
 * @param {number} [props.fields=5]
 * @param {string} [props.className]
 */
export default function FormSkeleton({ fields = 5, className = '' }) {
  return (
    <section
      className={`bg-surface-container-lowest rounded-md border border-outline-variant p-md md:p-lg shadow-sm space-y-md ${className}`}
    >
      <Skeleton className="h-7 w-1/3" />
      <div className="space-y-md">
        {Array.from({ length: fields }).map((_, i) => (
          <div key={i} className="space-y-xs">
            <Skeleton className="h-3 w-24" />
            <Skeleton className="h-10 w-full" />
          </div>
        ))}
      </div>
      <div className="flex justify-end gap-sm pt-sm">
        <Skeleton className="h-10 w-24" />
        <Skeleton className="h-10 w-32" />
      </div>
    </section>
  );
}

