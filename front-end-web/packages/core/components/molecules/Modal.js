'use client';

import * as Dialog from '@radix-ui/react-dialog';
import Icon from '@modelcity/core/components/atoms/Icon';
import VisuallyHidden from '@modelcity/core/components/atoms/VisuallyHidden';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';

/**
 * Molecule: Modal
 *
 * Accessible modal dialog backed by @radix-ui/react-dialog. Provides
 * out-of-the-box focus trap, ESC-to-close, return-focus on close, scroll
 * lock, backdrop click, and ARIA wiring (role="dialog",
 * aria-modal="true", aria-labelledby).
 *
 * Drop-in compatible with the previous custom Modal API. Callers control
 * visibility via { open, onClose }; the title is wired automatically.
 *
 * WCAG 2.2 — 2.1.1 Keyboard, 2.1.2 No Keyboard Trap, 2.4.3 Focus Order,
 * 2.4.7 Focus Visible, 4.1.2 Name/Role/Value.
 *
 * @param {{
 *   open: boolean,
 *   onClose: () => void,
 *   title: string,
 *   description?: string,
 *   closeLabel?: string,
 *   panelClassName?: string,
 *   bodyClassName?: string,
 *   children: React.ReactNode,
 * }} props
 */
export default function Modal({
  open,
  onClose,
  title,
  description,
  closeLabel,
  panelClassName,
  bodyClassName,
  children,
}) {
  const { dict } = useTranslations();
  const a11yLabel = closeLabel ?? dict?.a11y?.closeDialog ?? 'Close dialog';

  return (
    <Dialog.Root open={open} onOpenChange={(next) => { if (!next) onClose(); }}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm" />

        <Dialog.Content
          className={[
            'fixed left-1/2 top-1/2 z-50 -translate-x-1/2 -translate-y-1/2',
            'rounded-md bg-surface shadow-xl border border-outline-variant outline-none',
            'flex flex-col max-h-[90vh] overflow-y-auto',
            panelClassName ?? 'w-[min(32rem,calc(100vw-3rem))]',
          ].join(' ')}
        >
          <div className="flex items-center justify-between px-lg py-md border-b border-outline-variant">
            <Dialog.Title className="text-title-md text-primary font-semibold">
              {title}
            </Dialog.Title>
            <Dialog.Close
              aria-label={a11yLabel}
              className="text-on-surface-variant hover:text-on-surface transition-colors p-xs rounded-full hover:bg-surface-container focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            >
              <Icon name="close" size={20} />
            </Dialog.Close>
          </div>

          {description ? (
            <Dialog.Description className="px-lg pt-sm text-body-sm text-on-surface-variant">
              {description}
            </Dialog.Description>
          ) : (
            <VisuallyHidden>
              <Dialog.Description>{title}</Dialog.Description>
            </VisuallyHidden>
          )}

          <div className={bodyClassName ?? 'px-lg py-md flex-1'}>{children}</div>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
