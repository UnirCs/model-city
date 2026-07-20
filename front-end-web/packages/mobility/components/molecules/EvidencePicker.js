'use client';

import { useRef, useState } from 'react';
import Icon from '@modelcity/core/components/atoms/Icon';
import Button from '@modelcity/core/components/atoms/Button';

/** 5 MB hard limit, mirroring the form validation. */
const MAX_BYTES = 5 * 1024 * 1024;

/**
 * Reads a File as a base64 data string (without the `data:` prefix).
 *
 * @param {File} file
 * @returns {Promise<string>}
 */
function readAsBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = reject;
    reader.onload = () => {
      const result = String(reader.result ?? '');
      const comma = result.indexOf(',');
      resolve(comma >= 0 ? result.slice(comma + 1) : result);
    };
    reader.readAsDataURL(file);
  });
}

/**
 * Molecule: EvidencePicker
 *
 * Lets the operator attach a photo from their device — either by picking
 * from disk or by opening the rear camera (when supported). Exposes the
 * resulting base64 payload through `onChange`.
 *
 * @param {{
 *   value: string | null,
 *   onChange: (next: { base64: string, mimeType: string, sizeBytes: number } | null) => void,
 *   labels: {
 *     pick: string,
 *     capture: string,
 *     replace: string,
 *     remove: string,
 *     tooLarge: string,
 *   },
 *   error?: string | null,
 * }} props
 */
export default function EvidencePicker({ value, onChange, labels, error }) {
  const fileInputRef    = useRef(null);
  const cameraInputRef  = useRef(null);

  const [preview, setPreview]       = useState(null); // data URL for the preview
  const [sizeLabel, setSizeLabel]   = useState('');
  const [localError, setLocalError] = useState(null);

  /** File → preview + base64 propagation. */
  async function handleFile(file) {
    if (!file) return;

    if (file.size > MAX_BYTES) {
      setLocalError(labels.tooLarge);
      onChange(null);
      setPreview(null);
      setSizeLabel('');
      return;
    }

    setLocalError(null);
    try {
      const base64 = await readAsBase64(file);
      const dataUrl = `data:${file.type};base64,${base64}`;
      setPreview(dataUrl);
      setSizeLabel(`${(file.size / 1024).toFixed(0)} KB`);
      onChange({ base64, mimeType: file.type, sizeBytes: file.size });
    } catch {
      setLocalError(labels.tooLarge);
      onChange(null);
    }
  }

  function handleRemove() {
    setPreview(null);
    setSizeLabel('');
    setLocalError(null);
    onChange(null);
    if (fileInputRef.current)   fileInputRef.current.value = '';
    if (cameraInputRef.current) cameraInputRef.current.value = '';
  }

  const hasImage = Boolean(value || preview);
  const shownError = error ?? localError;

  return (
    <div className="flex flex-col gap-sm">
      {hasImage ? (
        /* Preview state */
        <div className="relative rounded-md overflow-hidden border border-outline-variant bg-surface-container">
          {preview ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={preview} alt="" className="w-full max-h-80 object-contain" />
          ) : (
            <div className="aspect-video flex items-center justify-center text-on-surface-variant">
              <Icon name="image" size={48} className="opacity-40" />
            </div>
          )}
          {sizeLabel && (
            <span className="absolute top-sm left-sm bg-surface/95 text-primary text-caption px-sm py-xs rounded-full backdrop-blur-sm">
              {sizeLabel}
            </span>
          )}
        </div>
      ) : (
        /* Empty placeholder */
        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          className="rounded-md border-2 border-dashed border-outline-variant bg-surface-container-lowest hover:bg-surface-container transition-colors aspect-video flex flex-col items-center justify-center gap-sm text-on-surface-variant"
        >
          <Icon name="add_a_photo" size={32} className="text-secondary" />
          <span className="text-label-md">{labels.pick}</span>
        </button>
      )}

      <div className="flex flex-wrap gap-sm">
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={() => fileInputRef.current?.click()}
        >
          <Icon name="folder_open" size={16} />
          {hasImage ? labels.replace : labels.pick}
        </Button>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={() => cameraInputRef.current?.click()}
        >
          <Icon name="photo_camera" size={16} />
          {labels.capture}
        </Button>
        {hasImage && (
          <Button
            type="button"
            variant="outline-error"
            size="sm"
            onClick={handleRemove}
          >
            <Icon name="delete" size={16} />
            {labels.remove}
          </Button>
        )}
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={(e) => handleFile(e.target.files?.[0])}
      />
      <input
        ref={cameraInputRef}
        type="file"
        accept="image/*"
        capture="environment"
        className="hidden"
        onChange={(e) => handleFile(e.target.files?.[0])}
      />

      {shownError && (
        <p className="text-caption text-error flex items-center gap-xs" role="alert">
          <Icon name="error" size={14} />
          {shownError}
        </p>
      )}
    </div>
  );
}

