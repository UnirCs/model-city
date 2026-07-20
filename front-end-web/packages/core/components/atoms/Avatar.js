/**
 * Atom: Avatar
 * Displays a profile picture or the user's initials as a fallback.
 */

/**
 * Generates initials from a full name.
 * @param {string} name
 * @returns {string}
 */
function getInitials(name = '') {
  return name
    .split(' ')
    .slice(0, 2)
    .map((w) => w[0]?.toUpperCase() ?? '')
    .join('');
}

const sizeMap = {
  sm: 'w-8 h-8 text-caption',
  md: 'w-10 h-10 text-label-md',
  lg: 'w-14 h-14 text-h3',
  xl: 'w-20 h-20 text-h2',
};

/**
 * @param {{ name?: string, src?: string|null, size?: 'sm'|'md'|'lg'|'xl', className?: string }} props
 */
export default function Avatar({ name = '', src = null, size = 'md', className = '' }) {
  const initials = getInitials(name);
  const sizeClass = sizeMap[size] ?? sizeMap.md;

  if (src) {
    return (
      // eslint-disable-next-line @next/next/no-img-element
      <img
        src={src}
        alt="photo"
        className={[
          'rounded-full object-cover border border-outline-variant',
          sizeClass,
          className,
        ].join(' ')}
      />
    );
  }

  return (
    <span
      aria-label={name}
      className={[
        'inline-flex items-center justify-center rounded-full bg-primary-container text-on-primary-container font-semibold select-none border border-outline-variant',
        sizeClass,
        className,
      ].join(' ')}
    >
      {initials}
    </span>
  );
}
