'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Button from '@modelcity/core/components/atoms/Button';
import Icon from '@modelcity/core/components/atoms/Icon';
import CreateAgentModal from '@modelcity/core/components/molecules/CreateAgentModal';

/**
 * Organism: AddWorkerButton (client)
 *
 * "Add municipal operator" action for the Administration → Workers subsection.
 * Opens the shared {@link CreateAgentModal} and refreshes the worker list after
 * a successful invitation so the new agent appears once the backend has
 * processed it.
 *
 * @param {{
 *   onCreateAgent: (payload: { role: string, name: string, email: string }) => Promise<{ ok: true } | { error: string, status?: number }>,
 *   buttonLabel: string,
 *   modalLabels: object,   // admin.createAgent
 * }} props
 */
export default function AddWorkerButton({ onCreateAgent, buttonLabel, modalLabels }) {
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [created, setCreated] = useState(false);

  async function handleSubmit(payload) {
    const res = await onCreateAgent(payload);
    if (res && 'ok' in res) setCreated(true);
    return res;
  }

  function handleClose() {
    setOpen(false);
    if (created) {
      setCreated(false);
      router.refresh();
    }
  }

  return (
    <>
      <Button type="button" variant="primary" size="md" onClick={() => setOpen(true)}>
        <Icon name="person_add" size={18} />
        <span>{buttonLabel}</span>
      </Button>

      <CreateAgentModal
        open={open}
        onSubmit={handleSubmit}
        onClose={handleClose}
        labels={modalLabels}
      />
    </>
  );
}
