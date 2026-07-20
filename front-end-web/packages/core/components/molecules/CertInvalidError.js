'use client';

import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: CertInvalidError
 *
 * Informational error panel shown when the browser cannot present a valid
 * FNMT digital certificate. Includes a link to the FNMT guide and a second
 * paragraph for the wrong-certificate scenario.
 *
 * Designed to be dropped inside any flow that requires certificate
 * verification (voting, document signing, etc.).
 *
 * @param {{
 *   t: {
 *     certInvalid:         string,
 *     certInvalidLine2:    string,
 *     certInvalidLink:     string,
 *     certInvalidLinkLabel: string,
 *   }
 * }} props
 */
export default function CertInvalidError({ t }) {
  const [before, after] = t.certInvalid.split(t.certInvalidLinkLabel);

  return (
    <div className="bg-error-container border-t border-outline-variant px-md py-md flex flex-col items-center gap-md text-center">
      <Icon name="gpp_bad" fill size={36} className="text-error" />

      <p className="text-body-md text-on-error-container">
        {before}
        <a
          href={t.certInvalidLink}
          target="_blank"
          rel="noopener noreferrer"
          className="underline font-semibold inline-flex items-center gap-xs hover:opacity-80"
        >
          {t.certInvalidLinkLabel}
          <Icon name="open_in_new" size={14} />
        </a>
        {after}
      </p>

      <p className="text-body-md text-on-error-container">{t.certInvalidLine2}</p>
    </div>
  );
}

