'use client';

import { useState, useRef, useEffect } from 'react';
import Avatar from '@modelcity/core/components/atoms/Avatar';
import Icon from '@modelcity/core/components/atoms/Icon';
import LocalizedLink from '@modelcity/core/lib/i18n/LocalizedLink';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';

/**
 * Molecule: UserMenu (client)
 * Avatar with a dropdown of user options.
 * Receives the Auth0 `user` object as a prop from the parent Server Component
 * (TopNavBar async → auth0.getSession()).
 *
 * @param {{ user: { name?: string, email?: string, picture?: string, given_name?: string } | null }} props
 */
export default function UserMenu({ user }) {
  const { dict } = useTranslations();
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  // Close when clicking outside
  useEffect(() => {
    function handleClick(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  if (!user) return null;

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => setOpen((v) => !v)}
        aria-expanded={open}
        aria-haspopup="menu"
        aria-label={dict.nav.userMenu}
        className="flex items-center gap-sm rounded-md hover:bg-surface-container p-xs transition-colors"
      >
        <Avatar name={user.name ?? ''} src={user.picture ?? null} size="sm" />
        <span className="hidden md:block text-label-md text-on-surface max-w-[140px] truncate">
          {user.given_name ?? user.name}
        </span>
        <Icon
          name={open ? 'expand_less' : 'expand_more'}
          size={18}
          className="text-on-surface-variant hidden md:block"
        />
      </button>

      {open && (
        <div
          role="menu"
          className="absolute right-0 top-full mt-base w-56 bg-surface-container-lowest border border-outline-variant rounded-md shadow-lg z-50 py-xs"
        >
          {/* User info */}
          <div className="px-md py-sm border-b border-outline-variant">
            <p className="text-label-md text-on-surface truncate">{user.name}</p>
            {user.email && (
              <p className="text-caption text-on-surface-variant truncate">{user.email}</p>
            )}
          </div>

          {/* My profile */}
          <LocalizedLink
            href="/profile"
            role="menuitem"
            onClick={() => setOpen(false)}
            className="flex items-center gap-sm px-md py-sm text-body-md text-on-surface-variant hover:bg-surface-container transition-colors"
          >
            <Icon name="manage_accounts" size={18} />
            {dict.auth.myProfile}
          </LocalizedLink>

          {/* Sign out → SDK's /auth/logout */}
          <a
            href="/auth/logout"
            role="menuitem"
            className="flex items-center gap-sm px-md py-sm text-body-md text-error hover:bg-error-container/30 transition-colors w-full"
          >
            <Icon name="logout" size={18} />
            {dict.auth.logout}
          </a>
        </div>
      )}
    </div>
  );
}
