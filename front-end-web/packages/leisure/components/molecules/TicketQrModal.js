'use client';

import { useRef, useCallback } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import Modal from '@modelcity/core/components/molecules/Modal';
import Button from '@modelcity/core/components/atoms/Button';
import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: TicketQrModal
 *
 * Displays a ticket's access QR code in a centred modal. Includes a download
 * button that serialises the SVG and triggers a browser download.
 *
 * @param {{
 *   open: boolean,
 *   ticket: { id: number, eventName?: string } | null,
 *   labels: {
 *     qrModalTitle: string,
 *     qrHint: string,
 *     qrDownload: string,
 *     close: string,
 *   },
 *   onClose: () => void,
 * }} props
 */
export default function TicketQrModal({ open, ticket, labels, onClose }) {
  const svgWrapRef = useRef(null);

  const handleDownload = useCallback(() => {
    const svg = svgWrapRef.current?.querySelector('svg');
    if (!svg) return;

    const serializer = new XMLSerializer();
    const source = serializer.serializeToString(svg);
    const blob = new Blob([source], { type: 'image/svg+xml;charset=utf-8' });
    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = `ticket-${ticket.id}-qr.svg`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }, [ticket]);

  if (!ticket) return null;

  return (
    <Modal open={open} onClose={onClose} title={labels.qrModalTitle} closeLabel={labels.close}>
      <div className="flex flex-col items-center gap-md py-md">
        <p className="text-body-sm text-on-surface-variant text-center">
          {labels.qrHint}
        </p>
        <div ref={svgWrapRef} className="p-md bg-white rounded-md">
          <QRCodeSVG value={String(ticket.id)} size={256} />
        </div>
        {ticket.eventName && (
          <p className="text-caption text-on-surface-variant text-center">
            {ticket.eventName}
          </p>
        )}
        <Button variant="outline" onClick={handleDownload}>
          <Icon name="download" size={16} />
          {labels.qrDownload}
        </Button>
      </div>
    </Modal>
  );
}
