'use client';

import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';
import { usePathname } from 'next/navigation';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';

/**
 * Molecule: NavItem
 * Sidebar navigation item with icon and label.
 * Supports active state (detected via pathname) and danger variant (logout).
 * Automatically prefixes the href with the current language.
 *
 * @param {{ href: string, icon: string, label: string, danger?: boolean }} props
 */
export default function NavItem({ href, icon, label, danger = false }) {
  const { lang } = useTranslations();
  const pathname = usePathname();

  const localizedHref = `/${lang}${href}`;
  const active = pathname === localizedHref || pathname.startsWith(`${localizedHref}/`);

  const base = 'flex items-center gap-md px-sm py-sm rounded-md transition-colors duration-150 text-body-md';

  const colorClass = danger
    ? 'text-error hover:bg-error-container/40'
    : active
    ? 'bg-secondary-container/40 text-secondary font-semibold'
    : 'text-on-surface-variant hover:bg-surface-variant/50';

  return (
    <Link
      href={localizedHref}
      className={[base, colorClass].join(' ')}
      aria-current={active ? 'page' : undefined}
    >
      <Icon name={icon} fill={active} size={22} />
      <span>{label}</span>
    </Link>
  );
}
