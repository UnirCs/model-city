import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Organism: LandingEasyRead
 *
 * Plain-language summary of the portal for users with cognitive disabilities or
 * low literacy (WCAG 2.2 SC 3.1.5).
 *
 * @param {{ t: object, }} props
 *   `t` is the landing dictionary.
 */
export default function LandingEasyRead({ t }) {
  return (
    <section
      className="bg-surface border-t border-outline-variant py-lg px-gutter"
      aria-labelledby="easy-read-heading"
    >
      <div className="max-w-[72ch] mx-auto">
        <h2 id="easy-read-heading" className="text-h2 text-primary mb-md">
          {t.easyRead.title}
        </h2>
        <p className="text-body-md text-on-surface-variant mb-md">
          {t.easyRead.intro}
        </p>
        <ul className="flex flex-col gap-sm list-none p-0 m-0">
          {[t.easyRead.p1, t.easyRead.p2, t.easyRead.p3, t.easyRead.p4].map((line, i) => (
            <li key={i} className="flex items-start gap-sm text-body-md text-on-surface">
              <Icon name="check_circle" size={18} className="text-secondary shrink-0 mt-0.5" />
              <span>{line}</span>
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
