import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: AdminSection
 *
 * Right-column panel grouping the staff management controls (edit / delete /
 * etc.) of a detail view. The controls are passed as children and stacked
 * vertically. Render this only when the user actually has management rights.
 *
 * @param {{
 *   title: string,
 *   children: React.ReactNode,
 *   headingId?: string,
 * }} props
 */
export default function AdminSection({ title, children, headingId = 'admin-section-heading' }) {
  return (
    <section
      aria-labelledby={headingId}
      className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
    >
      <h2
        id={headingId}
        className="text-h3 text-primary mb-md pb-xs border-b border-outline-variant flex items-center gap-sm"
      >
        <Icon name="admin_panel_settings" fill size={20} className="text-secondary" />
        {title}
      </h2>
      <div className="flex flex-col gap-sm">
        {children}
      </div>
    </section>
  );
}
