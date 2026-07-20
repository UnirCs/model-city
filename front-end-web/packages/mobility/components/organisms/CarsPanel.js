'use client';

import { useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import CarCard from '@modelcity/mobility/components/molecules/CarCard';
import AddCarModal from '@modelcity/mobility/components/molecules/AddCarModal';
import { formatDateTime } from '@modelcity/mobility/lib/utils/format';

/**
 * Organism: CarsPanel
 *
 * Renders the user's car collection with a CTA to register a new one. Owns
 * the open/close state of the {@link AddCarModal}. Pure client component;
 * the parent page fetches the cars server-side and passes them down.
 *
 * @param {{
 *   cars: Array<object>,
 *   labels: object,
 *   lang: string,
 * }} props
 */
export default function CarsPanel({ cars, labels, lang }) {
  const [modalOpen, setModalOpen] = useState(false);

  return (
    <>
      {cars.length === 0 ? (
        <div className="py-2xl text-center text-on-surface-variant flex flex-col items-center gap-md">
          <Icon name="no_crash" size={48} className="opacity-40" />
          <p className="text-body-lg">{labels.empty}</p>
          <button
            onClick={() => setModalOpen(true)}
            className="group rounded-md border-2 border-dashed border-secondary/40 bg-surface-container-lowest hover:border-secondary hover:bg-secondary/5 transition-all flex flex-col items-center justify-center gap-sm py-lg px-md text-center min-h-40 w-80 max-w-full cursor-pointer"
          >
            <span className="w-12 h-12 rounded-full bg-secondary/10 group-hover:bg-secondary/20 flex items-center justify-center transition-colors">
              <Icon name="add" size={24} className="text-secondary" />
            </span>
            <span className="text-label-md text-secondary font-semibold">{labels.addButton}</span>
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-md">
          {/* ── "Añadir coche" add-card — always first ── */}
          <button
            onClick={() => setModalOpen(true)}
            className="group rounded-md border-2 border-dashed border-secondary/40 bg-surface-container-lowest hover:border-secondary hover:bg-secondary/5 transition-all flex flex-col items-center justify-center gap-sm py-lg px-md text-center min-h-40 cursor-pointer"
          >
            <span className="w-12 h-12 rounded-full bg-secondary/10 group-hover:bg-secondary/20 flex items-center justify-center transition-colors">
              <Icon name="add" size={24} className="text-secondary" />
            </span>
            <span className="text-label-md text-secondary font-semibold">{labels.addButton}</span>
          </button>

          {cars.map((car) => (
            <CarCard
              key={car.id}
              car={car}
              addedAtLabel={labels.addedAt}
              formattedAddedAt={formatDateTime(car.createdAt, lang)}
            />
          ))}
        </div>
      )}

      <AddCarModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        labels={labels}
      />
    </>
  );
}

