import Skeleton from '@modelcity/core/components/atoms/Skeleton';

/**
 * CardSkeleton — placeholder mirroring a generic content card
 * (optional image, title, body lines, footer row).
 *
 * @param {object} props
 * @param {boolean} [props.withImage=true]  Show top image placeholder.
 * @param {number}  [props.lines=2]         Number of body text lines.
 * @param {string}  [props.className]
 */
export default function CardSkeleton({ withImage = true, lines = 2, className = '' }) {
  return (
    <div
      className={`bg-surface-container-lowest rounded-md border border-outline-variant overflow-hidden shadow-sm ${className}`}
    >
      {withImage && <Skeleton className="w-full h-40" rounded="md" />}
      <div className="p-md space-y-sm">
        <Skeleton className="h-5 w-2/3" />
        {Array.from({ length: lines }).map((_, i) => (
          <Skeleton key={i} className={`h-3 ${i === lines - 1 ? 'w-1/2' : 'w-full'}`} />
        ))}
        <div className="flex items-center justify-between pt-sm">
          <Skeleton className="h-6 w-20" rounded="full" />
          <Skeleton className="h-8 w-24" />
        </div>
      </div>
    </div>
  );
}

