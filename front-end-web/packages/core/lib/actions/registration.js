'use server';

import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { registerUser as registerUserInCore } from '@modelcity/core/lib/api/client';

/**
 * Server Action: registers a new citizen in the core microservice.
 *
 * @param {{ name: string, neighbourhoodName: string }} data
 * @returns {Promise<{ ok: true } | { error: string }>}
 */
export async function registerUser({ name, neighbourhoodName }) {
  const session = await auth0.getSession();

  if (!session) {
    return { error: 'No autenticado. Vuelve a iniciar sesión.' };
  }

  if (!name?.trim() || !neighbourhoodName?.trim()) {
    return { error: 'Los campos nombre y barrio son obligatorios.' };
  }

  const accessToken = session.tokenSet?.accessToken;
  const payload = {
    sub:              session.user.sub,
    email:            session.user.email,
    name:             name.trim(),
    neighbourhoodName: neighbourhoodName.trim(),
  };

  const result = await registerUserInCore(payload, accessToken);

  if (!result.ok) {
    if (result.error === 'network_error') {
      return { error: 'El servicio de registro no está disponible. Inténtalo en unos minutos.' };
    }
    return { error: result.error ?? 'Error en el registro. Inténtalo de nuevo.' };
  }

  return { ok: true };
}
