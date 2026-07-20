import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: CatalogueCard
 *
 * Generic listing card used across all catalogue sections (events, sports
 * spaces, tourist routes, locations, consultations). The card is entirely
 * service-agnostic: the caller composes the metadata it needs via `extras`.
 *
 * Layout:
 *  - Cover image (fixed height, optional fallback icon)
 *  - Title (text-body-md, 2-line clamp)
 *  - Extras row: up to N {icon, text} items joined by a mid-dot separator,
 *    rendered as a single text-caption line
 *  - Fused "Ver detalles" button flush to the card bottom and sides
 *
 * The card's `overflow-hidden rounded-md` clips the button so it merges
 * seamlessly with the bottom and lateral borders without extra CSS tricks.
 *
 * @param {{
 *   href: string,
 *   imageUrl?: string | null,
 *   imageAlt: string,
 *   fallbackIcon?: string,
 *   title: string,
 *   extras?: Array<{ icon: string, value: string }>,
 *   viewDetailsLabel: string,
 *   secondaryLabel?: string,
 *   onSecondaryAction?: () => void,
 * }} props
 */
export default function CatalogueCard({
  href,
  imageUrl,
  imageAlt,
  fallbackIcon = 'category',
  title,
  extras = [],
  viewDetailsLabel,
  grayscale = false,
  secondaryLabel,
  onSecondaryAction,
}) {
  const hasSecondary = !!secondaryLabel && !!onSecondaryAction;

  const cardBody = (
    <>
      {/* Cover image */}
      <div className="h-36 overflow-hidden bg-surface-container-high relative shrink-0">
        {imageUrl ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img
            src={imageUrl}
            alt={imageAlt}
            className={`w-full h-full object-cover group-hover:scale-105 transition-transform duration-500 ${grayscale ? 'grayscale' : ''}`}
            loading="lazy"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-outline">
            <Icon name={fallbackIcon} size={40} />
          </div>
        )}
      </div>

      {/* Body */}
      <div className="px-md pt-md pb-0 flex-1 flex flex-col gap-xs">
        <h3 className="text-body-md text-primary line-clamp-2 font-semibold">{title}</h3>

        {extras.length > 0 && (
          <p className="text-caption text-on-surface-variant flex items-center gap-xs flex-wrap">
            {extras.map(({ icon, value }, i) => (
              <span key={i} className="inline-flex items-center gap-xs">
                {i > 0 && <span aria-hidden="true" className="opacity-40">·</span>}
                <Icon name={icon} size={13} className="text-secondary shrink-0" />
                <span>{value}</span>
              </span>
            ))}
          </p>
        )}
      </div>
    </>
  );

  if (hasSecondary) {
    return (
      <article className="bg-surface-container-lowest rounded-md shadow-sm hover:shadow-md border border-outline-variant overflow-hidden flex flex-col transition-all duration-200 hover:-translate-y-1 h-full">
        <Link href={href} className="group block flex-1 cursor-pointer">
          {cardBody}
        </Link>

        {/* Fused bottom buttons */}
        <div className="mt-auto pt-sm flex">
          <Link
            href={href}
            className="flex-1 block text-center py-sm text-label-md font-medium bg-primary text-on-primary hover:bg-secondary transition-colors rounded-tl-none rounded-tr-none rounded-bl-md rounded-br-none inline-flex items-center justify-center gap-xs"
          >
            {viewDetailsLabel}
          </Link>
          <button
            onClick={onSecondaryAction}
            className="flex-1 block text-center py-sm text-label-md font-medium bg-secondary text-on-secondary hover:opacity-90 transition-colors rounded-tl-none rounded-tr-none rounded-bl-none rounded-br-md cursor-pointer inline-flex items-center justify-center gap-xs"
          >
            <Icon name="qr_code" size={16} />
            {secondaryLabel}
          </button>
        </div>
      </article>
    );
  }

  return (
    <Link href={href} className="group block h-full">
      <article className="bg-surface-container-lowest rounded-md shadow-sm hover:shadow-md border border-outline-variant overflow-hidden flex flex-col transition-all duration-200 hover:-translate-y-1 cursor-pointer h-full">
        {cardBody}

        {/* Fused bottom button — clipped by card overflow-hidden + rounded-md */}
        <div className="mt-auto pt-sm">
          <span className="block w-full text-center py-sm text-label-md font-medium bg-primary text-on-primary group-hover:bg-secondary transition-colors">
            {viewDetailsLabel}
          </span>
        </div>

      </article>
    </Link>
  );
}
