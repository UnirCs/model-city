    import Icon from '@modelcity/core/components/atoms/Icon';

/**
 * Molecule: LoginButton
 * Link that initiates the OAuth 2.0 flow with Auth0.
 * In production redirects to /auth/login → Auth0 Universal Login.
 * returnTo specifies the destination route after login.
 *
 * No longer a Client Component: no loading state needed
 * since Auth0 controls the entire OAuth navigation.
 */
export default function LoginButton({
  className = '',
  label = 'Acceder',
  iconName = 'login',
  returnTo = '/home',
}) {
  return (
    <a
      href={`/auth/login?returnTo=${encodeURIComponent(returnTo)}`}
      className={[
        'inline-flex items-center gap-xs font-semibold transition-all duration-200 cursor-pointer',
        className,
      ].join(' ')}
    >
      <Icon name={iconName} size={16} />
      {label}
    </a>
  );
}
