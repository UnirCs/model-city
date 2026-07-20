import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: CitizenOnlyNotice
 *
 * Displayed to staff users (admin, backoffice, operator) in place of
 * actions that are exclusively available to citizens (e.g. voting).
 *
 * @param {{ t: { title: string, message: string } }} props
 */
export default function CitizenOnlyNotice({ t }) {
  return (
    <div className="rounded-md border border-outline-variant bg-surface-container p-lg flex flex-col items-center gap-sm text-center shadow-sm">
      <div className="w-12 h-12 rounded-full bg-tertiary-container flex items-center justify-center shrink-0">
        <Icon name="badge" fill size={28} className="text-on-tertiary-container" />
      </div>
      <p className="text-label-lg font-semibold text-on-surface">{t.title}</p>
      <p className="text-body-md text-on-surface-variant" style={{ maxWidth: '38ch' }}>
        {t.message}
      </p>
    </div>
  );
}

