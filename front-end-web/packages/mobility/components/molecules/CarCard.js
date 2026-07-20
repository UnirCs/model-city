import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: CarCard
 * Compact card showing one of the user's registered cars. Pure presentational.
 *
 * @param {{
 *   car: {
 *     id: number,
 *     licensePlate: string,
 *     nickname?: string | null,
 *     brand?: string | null,
 *     model?: string | null,
 *     createdAt?: string,
 *   },
 *   addedAtLabel: string,
 *   formattedAddedAt?: string,
 * }} props
 */
export default function CarCard({ car, addedAtLabel, formattedAddedAt }) {
  const subtitle = [car.brand, car.model].filter(Boolean).join(' ');

  return (
    <article className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md flex flex-col gap-sm">
      <header className="flex items-start gap-sm">
        <div className="w-10 h-10 rounded-md bg-primary/10 text-primary flex items-center justify-center shrink-0">
          <Icon name="directions_car" fill size={22} />
        </div>
        <div className="flex-1 min-w-0">
          <h3 className="text-body-lg lg:text-h3 text-primary tracking-wide font-semibold">{car.licensePlate}</h3>
          {car.nickname && (
            <p className="text-body-md text-on-surface truncate">{car.nickname}</p>
          )}
          {subtitle && (
            <p className="text-caption text-on-surface-variant truncate">{subtitle}</p>
          )}
        </div>
      </header>

      {formattedAddedAt && (
        <footer className="text-caption text-on-surface-variant flex items-center gap-xs pt-sm border-t border-outline-variant">
          <Icon name="event_available" size={14} />
          <span>
            {addedAtLabel}: {formattedAddedAt}
          </span>
        </footer>
      )}
    </article>
  );
}

