import Icon from '@modelcity/core/components/atoms/Icon';
import Link from 'next/link';

/**
 * Molecule: ServiceCard
 * Municipal service card with icon, title, description and link.
 * On mobile the card uses smaller typography and tighter spacing.
 */

/**
 * @param {{ id: string, title: string, description: string, icon: string, href: string }} props
 */
export default function ServiceCard({ title, description, icon, href }) {
  return (
    <Link href={href}>
      <article className="group bg-surface-container-lowest rounded-md p-sm md:p-md border border-outline-variant shadow-[0_4px_12px_color-mix(in_srgb,var(--ds-primary)_4%,transparent)] hover:shadow-[0_8px_24px_color-mix(in_srgb,var(--ds-primary)_8%,transparent)] transition-all flex flex-col h-full cursor-pointer">
        {/* Icon — single instance, responsive container */}
        <div className="w-10 h-10 md:w-12 md:h-12 bg-secondary-container/30 text-secondary rounded-md flex items-center justify-center mb-sm md:mb-md group-hover:bg-secondary group-hover:text-on-secondary transition-colors shrink-0">
          <Icon name={icon} fill size={24} />
        </div>

        {/* Content */}
        <h3 className="text-body-md md:text-h3 text-primary mb-xs font-semibold">{title}</h3>
        <p className="text-caption md:text-body-md text-on-surface-variant flex-1">{description}</p>
      </article>
    </Link>
  );
}
