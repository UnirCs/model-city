'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useLocalizedBack } from '@modelcity/core/lib/nav/useLocalizedBack';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';
import FormField from '@modelcity/core/components/atoms/FormField';
import LicensePlateInput from '@modelcity/mobility/components/atoms/LicensePlateInput';
import LocationPickerMapClient from '@modelcity/core/components/molecules/LocationPickerMapClient';
import EvidencePicker from '@modelcity/mobility/components/molecules/EvidencePicker';
import { issueSanction } from '@modelcity/mobility/lib/actions/mobility';

/**
 * Organism: CreateSanctionForm
 *
 * Two-column form to issue a new sanction: license plate + evidence on the
 * left, map picker on the right. The operator can be deep-linked from the
 * tickets table with the plate / coordinates pre-filled (initial props).
 *
 * @param {{
 *   labels: object,
 *   lang: string,
 *   initial?: {
 *     licensePlate?: string,
 *     latitude?: number | null,
 *     longitude?: number | null,
 *   },
 * }} props
 */
export default function CreateSanctionForm({ labels, lang, initial = {} }) {
  const router = useRouter();
  const goBack = useLocalizedBack();

  const [licensePlate, setLicensePlate] = useState(initial.licensePlate ?? '');
  const [latitude, setLatitude]   = useState(initial.latitude ?? null);
  const [longitude, setLongitude] = useState(initial.longitude ?? null);
  const [image, setImage]         = useState(null); // { base64, mimeType, sizeBytes }

  const [submitting, setSubmitting] = useState(false);
  const [errors, setErrors]         = useState({});
  const [serverError, setServerError] = useState('');

  function handleMapChange({ latitude: lat, longitude: lng }) {
    setLatitude(lat);
    setLongitude(lng);
    setErrors((p) => ({ ...p, location: '' }));
  }

  function validate() {
    const e = {};
    if (!licensePlate.trim())                 e.licensePlate = labels.errors.licensePlateRequired;
    if (latitude == null || longitude == null) e.location     = labels.errors.locationRequired;
    if (!image?.base64)                       e.image        = labels.errors.imageRequired;
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
      latitude,
      longitude,
      imageBase64: image.base64,
    };

    const result = await issueSanction(payload);
    setSubmitting(false);

    if (result?.error) {
      setServerError(labels.errors.serverError);
      return;
    }

    router.push(`/${lang}/mobility/sanctions`);
    router.refresh();
  }

  return (
    <form
      onSubmit={handleSubmit}
      noValidate
      className="grid grid-cols-1 lg:grid-cols-5 gap-md"
    >
      {/* LEFT — plate + evidence (40%) */}
      <div className="lg:col-span-2 flex flex-col gap-md">
        <FormField
          id="lp"
          label={labels.licensePlateLabel}
          required
          error={errors.licensePlate}
        >
          <LicensePlateInput
            id="lp"
            value={licensePlate}
            onChange={(next) => {
              setLicensePlate(next);
              setErrors((p) => ({ ...p, licensePlate: '' }));
            }}
            placeholder={labels.licensePlatePlaceholder}
            error={Boolean(errors.licensePlate)}
            aria-label={labels.licensePlateLabel}
          />
        </FormField>

        <div className="flex flex-col gap-sm">
          <h3 className="text-h3 text-primary flex items-center gap-sm">
            <Icon name="photo_camera" fill size={20} className="text-secondary" />
            {labels.evidenceTitle}
          </h3>
          <p className="text-caption text-on-surface-variant">{labels.evidenceHint}</p>
          <EvidencePicker
            value={image?.base64 ?? null}
            onChange={(next) => {
              setImage(next);
              setErrors((p) => ({ ...p, image: '' }));
            }}
            labels={{
              pick:     labels.evidencePick,
              capture:  labels.evidenceCapture,
              replace:  labels.evidenceReplace,
              remove:   labels.evidenceRemove,
              tooLarge: labels.errors.imageTooLarge,
            }}
            error={errors.image}
          />
        </div>
      </div>

      {/* RIGHT — map picker (60%) */}
      <div className="lg:col-span-3 flex flex-col gap-sm">
        <h3 className="text-h3 text-primary flex items-center gap-sm">
          <Icon name="my_location" fill size={20} className="text-secondary" />
          {labels.mapPickerTitle}
        </h3>
        <p className="text-caption text-on-surface-variant">{labels.coordinatesHint}</p>

        <LocationPickerMapClient
          latitude={latitude}
          longitude={longitude}
          onChange={handleMapChange}
          mapTitle={labels.mapPickerTitle}
        />

        {errors.location && (
          <p className="text-caption text-error flex items-center gap-xs" role="alert">
            <Icon name="error" size={14} />
            {errors.location}
          </p>
        )}
      </div>

      {/* Footer spanning all columns */}
      <div className="lg:col-span-5 flex flex-col gap-md">
        {serverError && (
          <div className="p-sm bg-error-container text-on-error-container rounded-md flex items-center gap-xs text-caption">
            <Icon name="error_outline" size={16} />
            <span>{serverError}</span>
          </div>
        )}

        <div className="flex justify-end gap-sm pt-sm border-t border-outline-variant">
          <Button type="button" variant="outline" onClick={goBack} disabled={submitting}>
            {labels.cancel}
          </Button>
          <Button type="submit" variant="error" disabled={submitting}>
            <Icon name="gavel" size={18} />
            {submitting ? labels.submitting : labels.submit}
          </Button>
        </div>
      </div>
    </form>
  );
}

