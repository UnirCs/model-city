import Skeleton from '@modelcity/core/components/atoms/Skeleton';
import CardSkeleton from './CardSkeleton';

/**
 * ListSkeleton — responsive grid of CardSkeletons.
 *
 * @param {object}  props
 * @param {number}  [props.count=6]
 * @param {boolean} [props.withImage=true]
 * @param {string}  [props.className]
 * @param {boolean} [props.contained=false]  Wrap the grid in a surface panel
 *   (matching the boxed look of list/consultation pages), preceded by a
 *   section-heading placeholder.
 */
export default function ListSkeleton({ count = 6, withImage = true, className = '', contained = false }) {
  const grid = (
    <div className={`grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md ${className}`}>
      {Array.from({ length: count }).map((_, i) => (
        <CardSkeleton key={i} withImage={withImage} />
      ))}
    </div>
  );

  if (!contained) return grid;

  return (
    <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
      <Skeleton className="h-6 w-48 mb-md" />
      {grid}
    </div>
  );
}
