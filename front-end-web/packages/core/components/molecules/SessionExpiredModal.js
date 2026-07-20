'use client';

import Modal from '@modelcity/core/components/molecules/Modal';
import Button from '@modelcity/core/components/atoms/Button';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';

/**
 * Molecule: SessionExpiredModal
 *
 * Modal shown when the user's session token has expired.
 * Displays a message and redirects to logout when the user confirms.
 *
 * @param {{ open: boolean, lang: string }} props
 */
export default function SessionExpiredModal({ open, lang }) {
  const { dict } = useTranslations();
  const t = dict?.auth ?? {};

  const handleLogout = () => {
    window.location.href = '/auth/logout';
  };

  return (
    <Modal
      open={open}
      onClose={handleLogout}
      title={t.sessionExpiredTitle ?? 'Session expired'}
      description={t.sessionExpiredDescription ?? 'Your session has expired. Please log in again.'}
    >
      <Button
        variant="primary"
        size="lg"
        onClick={handleLogout}
        className="w-full"
      >
        {t.sessionExpiredButton ?? 'Log in again'}
      </Button>
    </Modal>
  );
}
