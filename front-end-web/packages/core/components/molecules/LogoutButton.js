import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: LogoutButton
 * Link that closes the Auth0 session (invalidates Auth0 session + local cookie).
 * Redirects to /auth/logout → Auth0 → app landing.
 *
 * @param {{ label?: string }} props
 */
export default function LogoutButton({ label = 'Cerrar Sesión' }) {
  return (
    <a
      href="/auth/logout"
      className="flex items-center gap-md px-sm py-sm text-error hover:bg-error-container/40 rounded-md transition-colors duration-150 w-full text-body-md"
    >
      <Icon name="logout" size={22} />
      <span>{label}</span>
    </a>
  );
}
