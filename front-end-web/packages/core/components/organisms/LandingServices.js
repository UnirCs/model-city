import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Organism: LandingServices
 *
 * Theme-aware strip below the landing banner summarising the six citizen
 * services in a compact 3×2 grid.
 *
 * @param {{ t: object }} props  `t` is the landing dictionary.
 */
export default function LandingServices({ t }) {
  const services = [
    { icon: 'event',          label: t.services.s1label, desc: t.services.s1desc },
    { icon: 'map',            label: t.services.s2label, desc: t.services.s2desc },
    { icon: 'campaign',       label: t.services.s3label, desc: t.services.s3desc },
    { icon: 'emergency',      label: t.services.s4label, desc: t.services.s4desc },
    { icon: 'book_online',    label: t.services.s5label, desc: t.services.s5desc },
    { icon: 'directions_car', label: t.services.s6label, desc: t.services.s6desc },
  ];

  return (
    <section
      className="bg-surface-container-low border-t border-outline-variant py-lg px-gutter"
      aria-labelledby="services-heading"
    >
      <div className="max-w-container-max mx-auto">
        {/* Section label */}
        <p
          id="services-heading"
          className="text-on-surface-variant uppercase tracking-widest text-center mb-md"
          style={{ fontSize: '0.68rem', letterSpacing: '0.18em' }}
        >
          {t.services.title}
        </p>

        {/* 3 × 2 compact grid */}
        <div className="grid grid-cols-2 md:grid-cols-3 gap-sm">
          {services.map((s) => (
            <div
              key={s.label}
              className="flex items-start gap-sm p-sm rounded-md bg-surface-container-lowest border border-outline-variant hover:border-secondary/40 transition-colors"
            >
              <span className="w-9 h-9 rounded-md bg-secondary-container/25 flex items-center justify-center shrink-0 mt-px">
                <Icon name={s.icon} fill size={17} className="text-secondary" />
              </span>
              <div className="min-w-0">
                <p className="text-label-md text-primary font-semibold leading-tight">{s.label}</p>
                <p className="text-caption text-on-surface-variant mt-px leading-snug">{s.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
