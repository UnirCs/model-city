'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Modal from '@modelcity/core/components/molecules/Modal';
import FormField from '@modelcity/core/components/atoms/FormField';
import Button from '@modelcity/core/components/atoms/Button';
import LicensePlateInput from '@modelcity/mobility/components/atoms/LicensePlateInput';
import Icon from '@modelcity/core/components/atoms/Icon';
import { addUserCar } from '@modelcity/mobility/lib/actions/mobility';
import { useAnnounce } from '@modelcity/core/lib/a11y/AnnouncerProvider';

const INPUT_CLS =
  'w-full px-md py-sm rounded-md border border-outline-variant text-body-md bg-surface-container-lowest text-on-surface transition-colors focus:border-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary';

/**
 * Molecule: AddCarModal
 * Modal hosting the "register a new car" form. Calls the `addUserCar` server
 * action; on success refreshes the surrounding page so the new car shows up.
 *
 * @param {{
 *   open: boolean,
 *   onClose: () => void,
 *   labels: object,
 * }} props
 */
export default function AddCarModal({ open, onClose, labels }) {
  const router = useRouter();
  const announce = useAnnounce();

  const [licensePlate, setLicensePlate] = useState('');
  const [nickname, setNickname]         = useState('');
  const [brand, setBrand]               = useState('');
  const [model, setModel]               = useState('');

  const [submitting, setSubmitting]   = useState(false);
  const [errors, setErrors]           = useState({});
  const [serverError, setServerError] = useState('');

  function reset() {
    setLicensePlate('');
    setNickname('');
    setBrand('');
    setModel('');
    setErrors({});
    setServerError('');
    setSubmitting(false);
  }

  function handleClose() {
    if (submitting) return;
    reset();
    onClose();
  }

  function validate() {
    const e = {};
    const trimmed = licensePlate.trim();
    if (!trimmed) e.licensePlate = labels.errors.licensePlateRequired;
    else if (trimmed.length > 32) e.licensePlate = labels.errors.licensePlateLength;
    return e;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const v = validate();
    if (Object.keys(v).length > 0) {
      setErrors(v);
      return;
    }
    setErrors({});
    setServerError('');
    setSubmitting(true);

    const payload = {
      licensePlate: licensePlate.trim(),
      nickname: nickname.trim() || undefined,
      brand:    brand.trim()    || undefined,
      model:    model.trim()    || undefined,
    };

    const result = await addUserCar(payload);
    setSubmitting(false);

    if (result?.error) {
      setServerError(labels.errors.serverError);
      announce(labels.errors.serverError, 'assertive');
      return;
    }

    reset();
    onClose();
    announce(labels.submitAdd, 'polite');
    router.refresh();
  }

  return (
    <Modal open={open} onClose={handleClose} title={labels.modalTitle} closeLabel={labels.cancel}>
      <form onSubmit={handleSubmit} className="flex flex-col gap-md" noValidate>
        <FormField id="lp" label={labels.fields.licensePlate} error={errors.licensePlate} required>
          <LicensePlateInput
            id="lp"
            value={licensePlate}
            onChange={(next) => {
              setLicensePlate(next);
              setErrors((p) => ({ ...p, licensePlate: '' }));
            }}
            placeholder={labels.fields.licensePlatePlaceholder}
            error={Boolean(errors.licensePlate)}
            aria-label={labels.fields.licensePlate}
          />
        </FormField>

        <FormField id="nickname" label={labels.fields.nickname}>
          <input
            id="nickname"
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            placeholder={labels.fields.nicknamePlaceholder}
            maxLength={128}
            autoComplete="off"
            className={INPUT_CLS}
          />
        </FormField>

        <div className="grid grid-cols-2 gap-md">
          <FormField id="brand" label={labels.fields.brand}>
            <input
              id="brand"
              type="text"
              value={brand}
              onChange={(e) => setBrand(e.target.value)}
              placeholder={labels.fields.brandPlaceholder}
              maxLength={128}
              autoComplete="off"
              className={INPUT_CLS}
            />
          </FormField>
          <FormField id="model" label={labels.fields.model}>
            <input
              id="model"
              type="text"
              value={model}
              onChange={(e) => setModel(e.target.value)}
              placeholder={labels.fields.modelPlaceholder}
              maxLength={128}
              autoComplete="off"
              className={INPUT_CLS}
            />
          </FormField>
        </div>

        {serverError && (
          <div className="p-sm bg-error-container text-on-error-container rounded-md flex items-center gap-xs text-caption">
            <Icon name="error_outline" size={16} />
            <span>{serverError}</span>
          </div>
        )}

        <div className="flex justify-end gap-sm pt-sm border-t border-outline-variant">
          <Button type="button" variant="outline-error" onClick={handleClose} disabled={submitting}>
            {labels.cancel}
          </Button>
          <Button type="submit" variant="primary" disabled={submitting}>
            {submitting ? labels.submitting : labels.submitAdd}
          </Button>
        </div>
      </form>
    </Modal>
  );
}

