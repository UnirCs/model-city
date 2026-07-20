'use client';

import { useState, useRef, useEffect } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { useTranslations } from '@modelcity/core/lib/i18n/TranslationsProvider';
import { SUPPORTED_LANGS } from '@modelcity/core/lib/i18n/dictionaries';
import { relocalizePath } from '@modelcity/core/lib/i18n/localizePath';
import Icon from '@modelcity/core/components/atoms/Icon';

/** Cookie name that persists the user's language preference. */
const LANG_COOKIE = 'NEXT_LANG';

/**
 * Molecule: LanguageSwitcher (client)
 * Dropdown that lets the user switch between supported languages.
 * Writes the chosen language to a cookie so the proxy remembers it.
 *
 * @param {{ variant?: 'default' | 'landing' }} props
 *   variant='landing'  → white/translucent styling for the banner header
 *   variant='default'  → standard surface styling for the app shell
 */
export default function LanguageSwitcher({ variant = 'default' }) {
  const { lang, dict } = useTranslations();
  const pathname = usePathname();
  const router = useRouter();
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

  function switchLang(newLang) {
    // Persist preference
    document.cookie = `${LANG_COOKIE}=${newLang};path=/;max-age=31536000;SameSite=Lax`;
    // Re-localise the current page in place. `replace` (not `push`) avoids
    // leaving a stale duplicate in the old language behind it in the history.
    router.replace(relocalizePath(pathname, newLang));
    setOpen(false);
  }

  const isLanding = variant === 'landing';

  const triggerClass = isLanding
    ? 'flex items-center gap-xs p-base rounded-md text-white/70 hover:text-white hover:bg-white/10 transition-colors'
    : 'flex items-center gap-xs p-base rounded-md text-on-surface-variant hover:bg-surface-container transition-colors';

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => setOpen((v) => !v)}
        aria-label={dict.lang.label}
        aria-expanded={open}
        className={triggerClass}
      >
        <Icon name="language" size={20} />
        <span className="text-label-md hidden sm:block">{lang.toUpperCase()}</span>
        <Icon name={open ? 'expand_less' : 'expand_more'} size={16} />
      </button>

      {open && (
        <div
          role="listbox"
          aria-label={dict.lang.label}
          className="absolute right-0 top-full mt-base bg-surface-container-lowest border border-outline-variant rounded-md shadow-lg z-50 py-xs min-w-[130px]"
        >
          {SUPPORTED_LANGS.map((l) => (
            <button
              key={l}
              role="option"
              aria-selected={l === lang}
              onClick={() => switchLang(l)}
              className={[
                'w-full text-left px-md py-sm text-body-md transition-colors',
                l === lang
                  ? 'text-primary font-semibold bg-primary/5'
                  : 'text-on-surface-variant hover:bg-surface-container',
              ].join(' ')}
            >
              {dict.lang[l]}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

