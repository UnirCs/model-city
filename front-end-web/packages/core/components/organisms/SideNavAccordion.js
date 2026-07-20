'use client';

import { useState, useEffect } from 'react';
import { usePathname } from 'next/navigation';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';
import NavGroup from '@modelcity/core/components/molecules/NavGroup';

/**
 * Organism: SideNavAccordion
 * Client wrapper that renders sidebar NavGroups with mutual-exclusion accordion
 * behaviour: opening one group automatically collapses all others.
 *
 * It also auto-expands the group whose sectionPath (or any of its
 * sectionPaths) matches the current URL, and keeps it open as long as
 * navigation stays inside that section.
 *
 * @param {{
 *   groups: Array<{
 *     sectionPath: string,
 *     sectionPaths?: string[],   // extra paths that should also activate this group
 *     icon: string,
 *     label: string,
 *     items: Array<{ href: string | null, label: string }>,
 *   }>
 * }} props
 */
export default function SideNavAccordion({ groups }) {
  const { lang } = useTranslations();
  const pathname = usePathname();

  // Find which group (if any) is currently active based on the URL.
  // A group matches when the current pathname starts with any of its
  // sectionPaths (primary + extras).
  const activeSection =
    groups.find((g) => {
      const paths = [g.sectionPath, ...(g.sectionPaths ?? [])];
      return paths.some((p) => pathname.startsWith(`/${lang}${p}`));
    })?.sectionPath ?? null;

  const [openSection, setOpenSection] = useState(activeSection);

  // When navigating to a different section, auto-expand it.
  useEffect(() => {
    if (activeSection) setOpenSection(activeSection);
  }, [activeSection]);

  const handleToggle = (sectionPath) => {
    setOpenSection((prev) => (prev === sectionPath ? null : sectionPath));
  };

  return (
    <>
      {groups.map((group) => (
        <NavGroup
          key={group.sectionPath}
          sectionPath={group.sectionPath}
          icon={group.icon}
          label={group.label}
          items={group.items}
          isOpen={openSection === group.sectionPath}
          isActive={activeSection === group.sectionPath}
          onToggle={() => handleToggle(group.sectionPath)}
        />
      ))}
    </>
  );
}

