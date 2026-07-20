'use client';

import { useState, useTransition } from 'react';
import { useRouter } from 'next/navigation';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import Modal from '@modelcity/core/components/molecules/Modal';
import LocalizedLink from '@modelcity/core/lib/i18n/LocalizedLink';
import { DefaultLangFields, TranslationSections } from '@modelcity/core/components/molecules/MultilingualFields';
import {
  saveSpaceResource,
  removeSpaceResource,
} from '@modelcity/leisure/lib/actions/spaceResources';

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-outline-variant';
const INPUT_ERROR_CLS =
  'w-full px-md py-sm rounded-md border text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary border-error';

/**
 * Organism: SpaceResourcesPanel
 *
 * Renders the list of bookable resources of a public space.
 * Read-only for citizens; operators/admins additionally get add / edit / delete
 * controls through a single shared modal.
 *
 * Each resource links to its reservations page.
 *
 * @param {{
 *   spaceId:    string | number,
 *   resources:  Array<{ id: number, name: string, description?: string, resourceType: string }>,
 *   canManage:  boolean,
 *   t:          object,   // leisure.resources dictionary
 *   tForm:      object,   // leisure.resourceForm dictionary
 *   tTypes:     Record<string, string>,  // leisure.resourceTypes dictionary
 *   lang:       string,
 * }} props
 */
export default function SpaceResourcesPanel({
  spaceId,
  resources: rawResources,
  canManage,
  t,
  tForm,
  tTypes,
  lang,
}) {
  const resources = Array.isArray(rawResources) ? rawResources : [];
  const router = useRouter();
  const [modal, setModal] = useState(null); // { mode: 'create' | 'edit', resource? } | null
  const [deleting, setDeleting] = useState(null); // resource | null
  const [deleteError, setDeleteError] = useState('');
  const [pending, startTransition] = useTransition();

  function openCreate() {
    setModal({ mode: 'create' });
  }
  function openEdit(resource) {
    setModal({ mode: 'edit', resource });
  }

  function confirmDelete() {
    if (!deleting) return;
    setDeleteError('');
    startTransition(async () => {
      const result = await removeSpaceResource(spaceId, deleting.id);
      if (result?.error) {
        setDeleteError(t.deleteError);
        return;
      }
      setDeleting(null);
      router.refresh();
    });
  }

  return (
    <section
      aria-labelledby="space-resources-heading"
      className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
    >
      <h2
        id="space-resources-heading"
        className="text-h3 text-primary flex items-center gap-sm mb-md pb-xs border-b border-outline-variant"
      >
        <Icon name="sports" size={20} className="text-secondary" />
        {t.title}
      </h2>

      {resources.length === 0 && !canManage ? (
        <p className="text-body-md text-on-surface-variant py-md">{t.empty}</p>
      ) : (
        /* Options group — left-border accent per citizen-services-rules */
        <ul className="pl-md border-l-2 border-secondary flex flex-col gap-sm pr-sm">
          {canManage && (
            <li>
              <button
                type="button"
                onClick={openCreate}
                className="group w-full rounded-md border-2 border-dashed border-secondary/40 bg-surface-container-lowest hover:border-secondary hover:bg-secondary/5 transition-all flex flex-col items-center justify-center gap-sm py-md px-md text-center"
              >
                <span className="w-10 h-10 rounded-full bg-secondary/10 group-hover:bg-secondary/20 flex items-center justify-center transition-colors">
                  <Icon name="add" size={20} className="text-secondary" />
                </span>
                <span className="text-label-md text-secondary font-semibold">{t.addResource}</span>
              </button>
            </li>
          )}
          {resources.map((r) => (
            <li
              key={r.id}
              className="flex flex-wrap items-center justify-between gap-sm rounded-md border border-outline-variant bg-surface px-md py-sm"
            >
              <div className="min-w-0 flex-1">
                <p className="text-label-md text-on-surface font-semibold truncate">{r.name}</p>
                <p className="text-caption text-on-surface-variant flex items-center gap-xs">
                  <Icon name="category" size={14} className="text-secondary" />
                  {tTypes[r.resourceType] ?? r.resourceType}
                </p>
                {r.description && (
                  <p className="text-body-sm text-on-surface-variant line-clamp-2 mt-xs">
                    {r.description}
                  </p>
                )}
              </div>
              <div className="flex items-center gap-xs">
                <LocalizedLink
                  href={`/sports-spaces/${spaceId}/resources/${r.id}`}
                  className="inline-flex items-center gap-xs px-sm py-xs rounded-md text-label-md text-secondary hover:text-primary hover:bg-surface-container transition-colors"
                >
                  <Icon name="event_available" size={16} />
                  {t.viewReservations}
                </LocalizedLink>
                {canManage && (
                  <>
                    <button
                      type="button"
                      onClick={() => openEdit(r)}
                      className="inline-flex items-center gap-xs px-sm py-xs rounded-md text-label-md text-primary hover:bg-primary/10 transition-colors"
                    >
                      <Icon name="edit" size={16} />
                      {t.editResource}
                    </button>
                    <button
                      type="button"
                      onClick={() => { setDeleteError(''); setDeleting(r); }}
                      className="inline-flex items-center gap-xs px-sm py-xs rounded-md text-label-md text-error hover:bg-error/10 transition-colors"
                    >
                      <Icon name="delete" size={16} />
                      {t.deleteResource}
                    </button>
                  </>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}

      {/* Create / edit modal */}
      {modal && (
        <ResourceFormModal
          spaceId={spaceId}
          mode={modal.mode}
          resource={modal.resource}
          t={tForm}
          tTypes={tTypes}
          onClose={() => setModal(null)}
          onSaved={() => {
            setModal(null);
            router.refresh();
          }}
        />
      )}

      {/* Delete confirmation modal */}
      <Modal
        open={!!deleting}
        onClose={() => (pending ? null : setDeleting(null))}
        title={t.deleteConfirmTitle}
        closeLabel={tForm.cancel}
      >
        <div className="flex flex-col gap-md">
          <p className="text-body-md text-on-surface-variant">{t.deleteConfirmBody}</p>
          {deleteError && (
            <div className="flex items-center gap-sm px-md py-sm rounded-md bg-error-container text-on-error-container text-body-sm border border-error/20">
              <Icon name="error" size={18} className="shrink-0" />
              {deleteError}
            </div>
          )}
          <div className="flex items-center justify-end gap-md">
            <Button type="button" variant="outline-error" onClick={() => setDeleting(null)} disabled={pending}>
              {tForm.cancel}
            </Button>
            <Button type="button" variant="error" onClick={confirmDelete} disabled={pending}>
              {pending ? t.deleting : t.deleteResource}
            </Button>
          </div>
        </div>
      </Modal>
    </section>
  );
}

/* ───────────────────────────────────────────────────────────────────────── */

function ResourceFormModal({ spaceId, mode, resource, t, tTypes, onClose, onSaved }) {
  const isEdit = mode === 'edit';

  const initName = () => {
    const tr = resource?.translations?.name;
    return { es: tr?.es ?? resource?.name ?? '', en: tr?.en ?? '', fr: tr?.fr ?? '' };
  };
  const initDesc = () => {
    const tr = resource?.translations?.description;
    return { es: tr?.es ?? resource?.description ?? '', en: tr?.en ?? '', fr: tr?.fr ?? '' };
  };

  const [name, setName] = useState(initName);
  const [resourceType, setResourceType] = useState(resource?.resourceType ?? '');
  const [description, setDescription] = useState(initDesc);
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    const v = {};
    if (!name.es.trim())      v.name         = t.errors.nameRequired;
    if (!resourceType.trim()) v.resourceType = t.errors.typeRequired;
    if (Object.keys(v).length > 0) {
      setErrors(v);
      return;
    }
    setErrors({});
    setSubmitError('');
    setSubmitting(true);

    const payload = {
      name: { es: name.es.trim(), en: name.en.trim(), fr: name.fr.trim() },
      resourceType: resourceType.trim(),
      description: { es: description.es.trim(), en: description.en.trim(), fr: description.fr.trim() },
    };
    const result = await saveSpaceResource(
      spaceId,
      payload,
      isEdit ? resource.id : null,
    );
    setSubmitting(false);
    if (result?.error) {
      setSubmitError(t.errors.serverError);
      return;
    }
    onSaved();
  }

  const translatableFields = [
    {
      key: 'name',
      label: t.nameLabel,
      placeholder: t.namePlaceholder,
      error: errors.name,
      as: 'input',
      value: name,
      onChange: (l, v) => { setName((p) => ({ ...p, [l]: v })); setErrors((p) => ({ ...p, name: '' })); },
    },
    {
      key: 'description',
      label: t.descriptionLabel,
      placeholder: t.descriptionPlaceholder,
      as: 'textarea',
      rows: 2,
      value: description,
      onChange: (l, v) => setDescription((p) => ({ ...p, [l]: v })),
    },
  ];

  return (
    <Modal
      open
      onClose={() => (submitting ? null : onClose())}
      title={isEdit ? t.editTitle : t.createTitle}
      closeLabel={t.cancel}
    >
      <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-md">
        <DefaultLangFields fields={translatableFields} />

        <div className="flex flex-col gap-xs">
          <label className="text-label-md text-on-surface font-medium">{t.typeLabel}</label>
          <div className="relative">
            <select
              value={resourceType}
              onChange={(e) => { setResourceType(e.target.value); setErrors((p) => ({ ...p, resourceType: '' })); }}
              className={`appearance-none pr-xl ${errors.resourceType ? INPUT_ERROR_CLS : INPUT_CLS}`}
            >
              <option value="">{t.typePlaceholderDefault}</option>
              {Object.entries(tTypes).map(([code, label]) => (
                <option key={code} value={code}>{label}</option>
              ))}
            </select>
            <span className="pointer-events-none absolute inset-y-0 right-md flex items-center text-on-surface-variant">
              <Icon name="expand_more" size={20} />
            </span>
          </div>
          {errors.resourceType && (
            <p className="text-caption text-error flex items-center gap-xs">
              <Icon name="error" size={14} />
              {errors.resourceType}
            </p>
          )}
        </div>

        <TranslationSections fields={translatableFields} />

        {submitError && (
          <div className="flex items-center gap-sm px-md py-sm rounded-md bg-error-container text-on-error-container text-body-sm border border-error/20">
            <Icon name="error" size={18} className="shrink-0" />
            {submitError}
          </div>
        )}

        <div className="flex items-center justify-end gap-md">
          <Button type="button" variant="outline-error" onClick={onClose} disabled={submitting}>
            {t.cancel}
          </Button>
          <Button type="submit" variant="primary" disabled={submitting}>
            {submitting ? t.submitting : isEdit ? t.submitEdit : t.submitCreate}
          </Button>
        </div>
      </form>
    </Modal>
  );
}







