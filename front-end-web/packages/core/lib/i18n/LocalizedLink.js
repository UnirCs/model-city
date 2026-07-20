'use client';

import Link from 'next/link';
import { useTranslations } from './TranslationsProvider';

/**
 * LocalizedLink
 * Drop-in replacement for Next.js <Link> that automatically prepends the
 * current language prefix to the href.
 *
 * @example
 * <LocalizedLink href="/about">About Us</LocalizedLink>
 * // In a French context renders as: /fr/about
 *
 * @param {{ href: string, children: React.ReactNode } & import('next/link').LinkProps} props
 */
export default function LocalizedLink({ href, children, ...props }) {
  const { lang } = useTranslations();

  // Avoid double-prefixing if the href already starts with a lang prefix.
  const normalized = href.startsWith('/') ? href : `/${href}`;
  const localizedHref = `/${lang}${normalized}`;

  return (
    <Link href={localizedHref} {...props}>
      {children}
    </Link>
  );
}

